package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

/**
 * Real-time Hardware & Motion Step Sensor Tracker.
 *
 * Interacts directly with physical device hardware sensors:
 * 1. Primary: Sensor.TYPE_STEP_DETECTOR (triggers precisely on every physical footstep)
 * 2. Secondary: Sensor.TYPE_STEP_COUNTER (cumulative hardware step counter register)
 * 3. Dynamic Motion Fallback: Sensor.TYPE_ACCELEROMETER (high-performance dynamic vector
 *    magnitude peak detector to count walking steps reliably when dedicated chip is absent or in emulator).
 */
class StepSensorTracker(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepDetectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    fun getStepFlow(): Flow<Int> = callbackFlow {
        val sm = sensorManager
        if (sm == null) {
            close()
            return@callbackFlow
        }

        var previousHardwareCounter: Float? = null
        var lastStepTimestamp = 0L
        val minStepIntervalMs = 300L
        var previousMagnitude = 9.81f
        var isPeakDetected = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                when (event.sensor.type) {
                    Sensor.TYPE_STEP_DETECTOR -> {
                        // Triggers 1.0f on each individual physical step
                        if (event.values.isNotEmpty() && event.values[0] > 0f) {
                            trySend(1)
                        }
                    }

                    Sensor.TYPE_STEP_COUNTER -> {
                        // Cumulative count from device boot
                        val currentCount = event.values.firstOrNull() ?: return
                        val prev = previousHardwareCounter
                        if (prev != null && currentCount > prev) {
                            val diff = (currentCount - prev).toInt()
                            if (diff in 1..10) {
                                trySend(diff)
                            }
                        }
                        previousHardwareCounter = currentCount
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        // If dedicated step detector or counter is active, avoid duplicate counts
                        if (stepDetectorSensor != null || stepCounterSensor != null) return

                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        val currentMagnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                        val now = System.currentTimeMillis()
                        // Human walking cadence typically creates an acceleration peak between 11.4 and 16.5 m/s^2
                        val peakThreshold = 11.5f

                        if (currentMagnitude > peakThreshold && previousMagnitude <= peakThreshold) {
                            if (!isPeakDetected && (now - lastStepTimestamp) > minStepIntervalMs) {
                                isPeakDetected = true
                                lastStepTimestamp = now
                                trySend(1)
                            }
                        } else if (currentMagnitude < 10.2f) {
                            isPeakDetected = false
                        }
                        previousMagnitude = currentMagnitude
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        try {
            var registeredAny = false
            stepDetectorSensor?.let {
                sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_FASTEST)
                registeredAny = true
            }
            stepCounterSensor?.let {
                sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
                registeredAny = true
            }
            if (!registeredAny) {
                accelerometerSensor?.let {
                    sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_GAME)
                }
            }
        } catch (e: Exception) {
            Log.w("StepSensorTracker", "Failed to register step listeners", e)
        }

        awaitClose {
            try {
                sm.unregisterListener(listener)
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}
