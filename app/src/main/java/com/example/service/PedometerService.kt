package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.sqrt

/**
 * Real-time metrics computed from footstep sensor telemetry
 */
data class PedometerData(
    val isTracking: Boolean = false,
    val liveSteps: Int = 0,
    val distanceMeters: Double = 0.0,
    val caloriesBurntKcal: Int = 0,
    val co2SavedKg: Double = 0.0,
    val co2SavedGrams: Int = 0,
    val cadenceStepsPerMin: Int = 0,
    val lastStepTimestampMs: Long = 0L
)

/**
 * Background & Foreground Pedometer Service using Android SensorManager.
 *
 * Tracks hardware footstep detectors, step counter registers, and dynamic accelerometer peaks,
 * computing real-time physical steps, calories burned, and environmental CO2 emissions saved.
 */
class PedometerService : Service(), SensorEventListener {

    private val binder = PedometerBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private var sensorManager: SensorManager? = null
    private var stepDetectorSensor: Sensor? = null
    private var stepCounterSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private var previousHardwareCounter: Float? = null
    private var lastStepTimeMs = 0L
    private val minStepIntervalMs = 280L
    private var previousMagnitude = 9.81f
    private var isPeakDetected = false
    private var notificationManager: NotificationManager? = null

    inner class PedometerBinder : Binder() {
        fun getService(): PedometerService = this@PedometerService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        
        stepDetectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForegroundTracking()
                startSensorListening()
            }
            ACTION_STOP -> {
                stopSensorListening()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_RESET -> {
                resetMetrics()
            }
            ACTION_ADD_STEPS -> {
                val delta = intent.getIntExtra(EXTRA_STEP_DELTA, 1)
                onStepEvent(delta)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun startSensorListening() {
        val sm = sensorManager ?: return
        var registered = false

        try {
            stepDetectorSensor?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
                registered = true
            }
            stepCounterSensor?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                registered = true
            }
            if (!registered) {
                accelerometerSensor?.let {
                    sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
                }
            }
            _pedometerState.update { it.copy(isTracking = true) }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register step sensors", e)
        }
    }

    private fun stopSensorListening() {
        try {
            sensorManager?.unregisterListener(this)
            _pedometerState.update { it.copy(isTracking = false) }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unregister sensors", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                if (event.values.isNotEmpty() && event.values[0] > 0f) {
                    onStepEvent(1)
                }
            }

            Sensor.TYPE_STEP_COUNTER -> {
                val currentCount = event.values.firstOrNull() ?: return
                val prev = previousHardwareCounter
                if (prev != null && currentCount > prev) {
                    val diff = (currentCount - prev).toInt()
                    if (diff in 1..15) {
                        onStepEvent(diff)
                    }
                }
                previousHardwareCounter = currentCount
            }

            Sensor.TYPE_ACCELEROMETER -> {
                // Ignore accelerometer if dedicated hardware step detector is present
                if (stepDetectorSensor != null || stepCounterSensor != null) return

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val currentMagnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val now = System.currentTimeMillis()
                val peakThreshold = 11.4f

                if (currentMagnitude > peakThreshold && previousMagnitude <= peakThreshold) {
                    if (!isPeakDetected && (now - lastStepTimeMs) > minStepIntervalMs) {
                        isPeakDetected = true
                        lastStepTimeMs = now
                        onStepEvent(1)
                    }
                } else if (currentMagnitude < 10.2f) {
                    isPeakDetected = false
                }
                previousMagnitude = currentMagnitude
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Process step events and compute active metrics:
     * - Distance: steps * 0.75m
     * - Calories: steps * 0.042 kcal
     * - CO2 Avoided: distance * 0.154 g CO2 (compared to a standard scooter)
     */
    fun onStepEvent(stepDelta: Int) {
        if (stepDelta <= 0) return
        val now = System.currentTimeMillis()

        _pedometerState.update { current ->
            val updatedSteps = current.liveSteps + stepDelta
            val updatedDist = updatedSteps * 0.75
            val updatedKcal = (updatedSteps * 0.042).toInt().coerceAtLeast(if (updatedSteps > 10) 1 else 0)
            val updatedCo2Kg = updatedDist * 0.000154
            val updatedCo2Grams = (updatedDist * 0.154).toInt()

            current.copy(
                liveSteps = updatedSteps,
                distanceMeters = updatedDist,
                caloriesBurntKcal = updatedKcal,
                co2SavedKg = updatedCo2Kg,
                co2SavedGrams = updatedCo2Grams,
                lastStepTimestampMs = now
            )
        }

        updateNotification()
    }

    fun resetMetrics() {
        previousHardwareCounter = null
        _pedometerState.update {
            PedometerData(isTracking = it.isTracking)
        }
        updateNotification()
    }

    private fun startForegroundTracking() {
        val notification = buildTrackingNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start foreground service", e)
        }
    }

    private fun buildTrackingNotification(): Notification {
        val data = _pedometerState.value
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val distText = if (data.distanceMeters >= 1000.0) {
            String.format(java.util.Locale.US, "%.2f km", data.distanceMeters / 1000.0)
        } else {
            "${data.distanceMeters.toInt()}m"
        }

        val co2Text = if (data.co2SavedKg < 1.0) {
            "${data.co2SavedGrams}g CO₂"
        } else {
            String.format(java.util.Locale.US, "%.2f kg CO₂", data.co2SavedKg)
        }

        val content = "👟 ${data.liveSteps} bước • $distText • 🔥 ${data.caloriesBurntKcal} kcal • 🌱 -$co2Text"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("HẻmQuest Pedometer Tracker")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Đang theo dõi bước chân & chỉ số sống xanh:\n$content"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification() {
        try {
            notificationManager?.notify(NOTIFICATION_ID, buildTrackingNotification())
        } catch (e: Exception) {
            // Ignore notification updates if permissions are missing
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pedometer & Eco Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Theo dõi bước chân, năng lượng đốt cháy và lượng CO2 tiết kiệm khi đi bộ ngõ hẻm."
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSensorListening()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "PedometerService"
        const val CHANNEL_ID = "pedometer_tracking_channel"
        const val NOTIFICATION_ID = 2026

        const val ACTION_START = "com.example.action.START_PEDOMETER"
        const val ACTION_STOP = "com.example.action.STOP_PEDOMETER"
        const val ACTION_RESET = "com.example.action.RESET_PEDOMETER"
        const val ACTION_ADD_STEPS = "com.example.action.ADD_STEPS"
        const val EXTRA_STEP_DELTA = "extra_step_delta"

        private val _pedometerState = MutableStateFlow(PedometerData())
        val pedometerState: StateFlow<PedometerData> = _pedometerState.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, PedometerService::class.java).apply {
                action = ACTION_START
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start pedometer service", e)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, PedometerService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to stop pedometer service", e)
            }
        }

        fun resetService(context: Context) {
            val intent = Intent(context, PedometerService::class.java).apply {
                action = ACTION_RESET
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to reset pedometer service", e)
            }
        }
    }
}
