package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Tracks device azimuth / heading orientation in degrees (0 - 360)
 * for real-time user POV radar cone and compass alignment.
 */
class CompassOrientationTracker(context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)

    fun getHeadingFlow(): Flow<Float> = callbackFlow {
        if (rotationSensor == null) {
            trySend(0f)
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientationAngles = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    // Azimuth in radians -> convert to degrees (0 to 360)
                    var azimuthDegrees = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                    if (azimuthDegrees < 0) azimuthDegrees += 360f
                    trySend(azimuthDegrees)
                } else if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                    val azimuth = event.values[0]
                    trySend(azimuth)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener,
            rotationSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
