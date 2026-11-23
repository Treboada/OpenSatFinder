/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import kotlin.math.asin
import kotlin.math.atan2

/**
 * Azimuth, in degrees clockwise from north, of the device's natural "up" edge for
 * a rotation matrix laid out as
 * [android.hardware.SensorManager.getRotationMatrixFromVector] produces it.
 *
 * This is [android.hardware.SensorManager.getOrientation]'s first element:
 * `atan2(R[1], R[4])`. Keeping it here makes the arithmetic testable on the JVM,
 * which the framework call is not.
 */
fun azimuthFromRotationMatrix(matrix: FloatArray): Float {
    require(matrix.size >= 9) { "A rotation matrix has 9 values, got ${matrix.size}" }
    val radians = atan2(matrix[1].toDouble(), matrix[4].toDouble())
    return normalizeDegrees(Math.toDegrees(radians).toFloat())
}

/**
 * Converts a heading from the device's natural orientation to the one the user
 * sees. [screenRotationDegrees] is the rotation of the drawn graphics, which runs
 * opposite to the physical rotation of the device: with the device turned 90
 * degrees clockwise the screen top points 90 degrees left of the natural top.
 */
fun azimuthForScreen(
    naturalAzimuth: Float,
    screenRotationDegrees: Int,
): Float = normalizeDegrees(naturalAzimuth - screenRotationDegrees)

/**
 * How far the device is from lying flat, for the same rotation matrix layout
 * [azimuthFromRotationMatrix] reads.
 *
 * `R[7]` is the up component of the device's top edge and `R[6]` the one of its
 * right edge; `R[8]` is the up component of the screen normal. The pitch is the
 * elevation of the top edge, the roll the one of the right edge, so both are 0
 * when the screen faces the sky.
 */
fun tiltFromRotationMatrix(matrix: FloatArray): Tilt {
    require(matrix.size >= 9) { "A rotation matrix has 9 values, got ${matrix.size}" }
    val pitch = Math.toDegrees(asin(matrix[7].coerceIn(-1f, 1f).toDouble())).toFloat()
    val roll = Math.toDegrees(atan2(matrix[6].toDouble(), matrix[8].toDouble())).toFloat()
    return Tilt.of(pitchDegrees = pitch, rollDegrees = roll)
}

/**
 * Tilt in the frame the user sees rather than the device's natural one. Turning
 * the phone into landscape does not change how the phone leans, but it does change
 * which edge looks like the top of the screen, and the numbers must follow that.
 */
fun tiltForScreen(
    matrix: FloatArray,
    screenRotationDegrees: Int,
): Tilt = tiltFromRotationMatrix(matrixForScreen(matrix, screenRotationDegrees))

/** Which sensors a device offers to determine north with. */
enum class CompassStrategy {
    /** Fused rotation vector: already magnetometer-corrected, best quality. */
    RotationVector,

    /** Raw accelerometer and magnetometer, combined into a rotation matrix. */
    AccelerometerAndMagnetometer,

    /** No sensor combination can find north: no compass on this device. */
    Unavailable,
}

fun compassStrategy(
    hasRotationVector: Boolean,
    hasAccelerometer: Boolean,
    hasMagnetometer: Boolean,
): CompassStrategy =
    when {
        hasRotationVector -> CompassStrategy.RotationVector
        hasAccelerometer && hasMagnetometer -> CompassStrategy.AccelerometerAndMagnetometer
        else -> CompassStrategy.Unavailable
    }

/**
 * Low-pass filter for a heading in degrees that follows the shorter arc, so
 * passing north (359 -> 1) does not swing the dial the long way around.
 */
class HeadingSmoother(
    private val factor: Float = DEFAULT_FACTOR,
) {
    private var current: Float? = null

    init {
        require(factor > 0f && factor <= 1f) { "factor must be in (0, 1], got $factor" }
    }

    /** The first sample is taken as-is; later ones move [factor] of the way. */
    fun next(target: Float): Float {
        val previous = current
        val smoothed =
            if (previous == null) {
                normalizeDegrees(target)
            } else {
                normalizeDegrees(previous + shortestTurn(previous, target) * factor)
            }
        current = smoothed
        return smoothed
    }

    fun reset() {
        current = null
    }

    private companion object {
        /** A quarter of the distance per sample: responsive, still calm at 50 Hz. */
        const val DEFAULT_FACTOR = 0.25f
    }
}

/** Signed difference from [from] to [to] in `(-180, 180]`. */
fun shortestTurn(
    from: Float,
    to: Float,
): Float {
    val delta = normalizeDegrees(to - from)
    return if (delta > 180f) delta - FULL_TURN_DEGREES else delta
}

/**
 * Re-expresses the rotation matrix in the screen's axes: after the change of
 * frame, `R[7]` and `R[6]` describe the edges the user sees as top and right.
 */
private fun matrixForScreen(
    matrix: FloatArray,
    screenRotationDegrees: Int,
): FloatArray {
    val change =
        when (normalizeDegrees(screenRotationDegrees.toFloat())) {
            90f -> SCREEN_TO_DEVICE_90
            180f -> SCREEN_TO_DEVICE_180
            270f -> SCREEN_TO_DEVICE_270
            else -> return matrix
        }
    return multiply(matrix, change)
}

/** Row-major 3x3 product: applies the change of frame to the device rotation. */
private fun multiply(
    matrix: FloatArray,
    change: FloatArray,
): FloatArray {
    val result = FloatArray(9)
    for (row in 0 until 3) {
        for (column in 0 until 3) {
            var sum = 0f
            for (axis in 0 until 3) {
                sum += matrix[row * 3 + axis] * change[axis * 3 + column]
            }
            result[row * 3 + column] = sum
        }
    }
    return result
}

// Columns are the screen axes - right, up, normal - in device axes.
private val SCREEN_TO_DEVICE_90 = floatArrayOf(0f, -1f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
private val SCREEN_TO_DEVICE_180 = floatArrayOf(-1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f)
private val SCREEN_TO_DEVICE_270 = floatArrayOf(0f, 1f, 0f, -1f, 0f, 0f, 0f, 0f, 1f)
