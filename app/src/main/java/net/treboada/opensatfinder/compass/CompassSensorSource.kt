/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.view.Surface
import android.view.WindowManager

/**
 * Turns orientation sensor events into headings relative to the top of the screen.
 *
 * [Sensor.TYPE_ROTATION_VECTOR] is preferred: it is the fused sensor that already
 * combines accelerometer, gyroscope and magnetometer. Devices without it fall back
 * to the raw [Sensor.TYPE_ACCELEROMETER] and [Sensor.TYPE_MAGNETIC_FIELD] pair,
 * which reaches the same rotation matrix through the platform's own fusion.
 */
class CompassSensorSource(
    private val sensorManager: SensorManager,
    private val screenRotationDegrees: () -> Int,
    private val headingSmoother: HeadingSmoother = HeadingSmoother(),
    private val tiltSmoother: TiltSmoother = TiltSmoother(),
) {
    val strategy: CompassStrategy =
        compassStrategy(
            hasRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null,
            hasAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null,
            hasMagnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null,
        )

    private val rotationMatrix = FloatArray(9)
    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null
    private var magneticAccuracy: Int = SensorManager.SENSOR_STATUS_UNRELIABLE
    private var onReading: ((Heading, HeadingAccuracy, Tilt) -> Unit)? = null

    private val listener =
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        report(event.accuracy)
                    }

                    Sensor.TYPE_ACCELEROMETER -> {
                        gravity = event.values.copyOf(AXES)
                        reportFromRawSensors()
                    }

                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        geomagnetic = event.values.copyOf(AXES)
                        magneticAccuracy = event.accuracy
                        reportFromRawSensors()
                    }
                }
            }

            override fun onAccuracyChanged(
                sensor: Sensor?,
                accuracy: Int,
            ) {
                if (sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    magneticAccuracy = accuracy
                }
            }
        }

    /** Registers the sensors needed by [strategy]; false when the device has none. */
    fun start(onReading: (Heading, HeadingAccuracy, Tilt) -> Unit): Boolean {
        if (strategy == CompassStrategy.Unavailable) return false
        this.onReading = onReading

        val registered =
            when (strategy) {
                CompassStrategy.RotationVector ->
                    register(Sensor.TYPE_ROTATION_VECTOR)

                CompassStrategy.AccelerometerAndMagnetometer ->
                    register(Sensor.TYPE_ACCELEROMETER) && register(Sensor.TYPE_MAGNETIC_FIELD)

                CompassStrategy.Unavailable -> false
            }

        if (!registered) stop()
        return registered
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
        onReading = null
        gravity = null
        geomagnetic = null
        headingSmoother.reset()
        tiltSmoother.reset()
    }

    private fun register(type: Int): Boolean {
        val sensor = sensorManager.getDefaultSensor(type) ?: return false
        return sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun reportFromRawSensors() {
        val gravityValues = gravity ?: return
        val geomagneticValues = geomagnetic ?: return
        if (!SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, geomagneticValues)) return
        report(magneticAccuracy)
    }

    private fun report(sensorAccuracy: Int) {
        val reading = onReading ?: return
        val screenRotation = screenRotationDegrees()
        val heading =
            Heading.of(
                azimuthForScreen(azimuthFromRotationMatrix(rotationMatrix), screenRotation),
            )
        val tilt = tiltForScreen(rotationMatrix, screenRotation)
        reading(
            Heading.of(headingSmoother.next(heading.degrees)),
            accuracyOf(sensorAccuracy),
            tiltSmoother.next(tilt),
        )
    }

    private fun accuracyOf(sensorAccuracy: Int): HeadingAccuracy =
        when (sensorAccuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> HeadingAccuracy.High
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> HeadingAccuracy.Medium
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> HeadingAccuracy.Low
            else -> HeadingAccuracy.Unreliable
        }

    private companion object {
        const val AXES = 3
    }
}

/** Screen rotation in degrees, as the same value [azimuthForScreen] expects. */
internal fun Context.screenRotationDegrees(): Int =
    when (currentDisplayRotation()) {
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> 0
    }

private fun Context.currentDisplayRotation(): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // A context without a display (an application context, a Robolectric test)
        // has no rotation to report; the compass should not crash over it.
        return runCatching { display.rotation }.getOrDefault(Surface.ROTATION_0)
    }
    @Suppress("DEPRECATION")
    val legacyDisplay = getSystemService(WindowManager::class.java)?.defaultDisplay ?: return Surface.ROTATION_0
    @Suppress("DEPRECATION")
    return legacyDisplay.rotation
}
