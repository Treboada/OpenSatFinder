/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class CompassMathTest {
    private val tolerance = 0.001f

    /**
     * Rotation matrix whose horizontal-plane entries are the ones the azimuth
     * reads: `atan2(R[1], R[4])`. Identity is a device pointing north.
     */
    private fun rotationMatrix(
        across: Float = 0f,
        forward: Float = 1f,
    ): FloatArray = floatArrayOf(1f, across, 0f, 0f, forward, 0f, 0f, 0f, 1f)

    @Test
    fun `an unrotated device points north`() {
        assertEquals(0f, azimuthFromRotationMatrix(rotationMatrix()), tolerance)
    }

    @Test
    fun `a device turned to the east reads ninety degrees`() {
        assertEquals(90f, azimuthFromRotationMatrix(rotationMatrix(across = 1f, forward = 0f)), tolerance)
    }

    @Test
    fun `a device turned to the west reads two hundred and seventy degrees`() {
        assertEquals(270f, azimuthFromRotationMatrix(rotationMatrix(across = -1f, forward = 0f)), tolerance)
    }

    @Test
    fun `a matrix that is too short is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            azimuthFromRotationMatrix(FloatArray(4))
        }
    }

    @Test
    fun `no screen rotation leaves the natural azimuth alone`() {
        assertEquals(10f, azimuthForScreen(naturalAzimuth = 10f, screenRotationDegrees = 0), tolerance)
    }

    @Test
    fun `a device turned quarter clockwise shifts the screen top back by ninety`() {
        assertEquals(280f, azimuthForScreen(naturalAzimuth = 10f, screenRotationDegrees = 90), tolerance)
    }

    @Test
    fun `screen rotation wraps across north`() {
        assertEquals(260f, azimuthForScreen(naturalAzimuth = 350f, screenRotationDegrees = 90), tolerance)
        assertEquals(0f, azimuthForScreen(naturalAzimuth = 270f, screenRotationDegrees = 270), tolerance)
        assertEquals(135f, azimuthForScreen(naturalAzimuth = 315f, screenRotationDegrees = 180), tolerance)
    }

    @Test
    fun `a rotation vector sensor is preferred over raw sensors`() {
        assertEquals(
            CompassStrategy.RotationVector,
            compassStrategy(hasRotationVector = true, hasAccelerometer = true, hasMagnetometer = true),
        )
    }

    @Test
    fun `raw sensors are the fallback when no rotation vector exists`() {
        assertEquals(
            CompassStrategy.AccelerometerAndMagnetometer,
            compassStrategy(hasRotationVector = false, hasAccelerometer = true, hasMagnetometer = true),
        )
    }

    @Test
    fun `half a compass is no compass`() {
        assertEquals(
            CompassStrategy.Unavailable,
            compassStrategy(hasRotationVector = false, hasAccelerometer = true, hasMagnetometer = false),
        )
        assertEquals(
            CompassStrategy.Unavailable,
            compassStrategy(hasRotationVector = false, hasAccelerometer = false, hasMagnetometer = false),
        )
    }
}
