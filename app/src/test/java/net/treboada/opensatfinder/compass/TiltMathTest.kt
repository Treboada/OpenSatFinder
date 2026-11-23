/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Tilt is only useful if it says which way the phone leans, so every case here is
 * built from a hand-computed rotation matrix instead of from the code under test.
 *
 * Layout is the one `SensorManager.getRotationMatrixFromVector` produces: `R[7]` is
 * the up component of the top edge, `R[6]` the one of the right edge, `R[8]` the
 * up component of the screen normal.
 */
class TiltMathTest {
    private val tolerance = 0.01f
    private val cos30 = 0.8660254f
    private val sin30 = 0.5f

    /** Phone flat on the table, top edge pointing north. */
    private fun flatMatrix(): FloatArray = floatArrayOf(1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f)

    /** Top edge raised 30 degrees above the horizon, right edge still level. */
    private fun topRaisedMatrix(): FloatArray = floatArrayOf(1f, 0f, 0f, 0f, cos30, -sin30, 0f, sin30, cos30)

    /** Right edge raised 30 degrees above the horizon, top edge still level. */
    private fun rightRaisedMatrix(): FloatArray = floatArrayOf(cos30, 0f, -sin30, 0f, 1f, 0f, sin30, 0f, cos30)

    /** Right edge lowered 30 degrees below the horizon, top edge still level. */
    private fun rightLoweredMatrix(): FloatArray = floatArrayOf(cos30, 0f, sin30, 0f, 1f, 0f, -sin30, 0f, cos30)

    @Test
    fun `a phone lying flat reads no tilt`() {
        assertTilt(pitch = 0f, roll = 0f, tilt = tiltFromRotationMatrix(flatMatrix()))
    }

    @Test
    fun `raising the top edge reads as a positive pitch`() {
        assertTilt(pitch = 30f, roll = 0f, tilt = tiltFromRotationMatrix(topRaisedMatrix()))
    }

    @Test
    fun `raising the right edge reads as a positive roll`() {
        assertTilt(pitch = 0f, roll = 30f, tilt = tiltFromRotationMatrix(rightRaisedMatrix()))
    }

    @Test
    fun `lowering the right edge reads as a negative roll`() {
        assertTilt(pitch = 0f, roll = -30f, tilt = tiltFromRotationMatrix(rightLoweredMatrix()))
    }

    @Test
    fun `a top edge pointing at the sky reads ninety degrees up`() {
        // Device y axis (top) on world up, screen facing north.
        val straightUp = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 1f, 0f)

        assertTilt(pitch = 90f, roll = 0f, tilt = tiltFromRotationMatrix(straightUp))
    }

    @Test
    fun `an up component past the end of the arc sine is clamped`() {
        val overshoot = flatMatrix().also { it[7] = 1.01f }

        assertTilt(pitch = 90f, roll = 0f, tilt = tiltFromRotationMatrix(overshoot))
    }

    @Test
    fun `a matrix that is too short is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            tiltFromRotationMatrix(FloatArray(4))
        }
    }

    @Test
    fun `landscape turns the pitch of the natural frame into roll`() {
        // Same phone, same lean: only the edge that looks like the top has changed.
        val topRaised = topRaisedMatrix()

        assertTilt(pitch = 30f, roll = 0f, tilt = tiltForScreen(topRaised, screenRotationDegrees = 0))
        assertTilt(pitch = 0f, roll = 30f, tilt = tiltForScreen(topRaised, screenRotationDegrees = 90))
        assertTilt(pitch = -30f, roll = 0f, tilt = tiltForScreen(topRaised, screenRotationDegrees = 180))
        assertTilt(pitch = 0f, roll = -30f, tilt = tiltForScreen(topRaised, screenRotationDegrees = 270))
    }

    @Test
    fun `screen rotation is applied to a rolled phone too`() {
        val rightRaised = rightRaisedMatrix()

        assertTilt(pitch = 0f, roll = 30f, tilt = tiltForScreen(rightRaised, screenRotationDegrees = 0))
        assertTilt(pitch = -30f, roll = 0f, tilt = tiltForScreen(rightRaised, screenRotationDegrees = 90))
        assertTilt(pitch = 0f, roll = -30f, tilt = tiltForScreen(rightRaised, screenRotationDegrees = 180))
        assertTilt(pitch = 30f, roll = 0f, tilt = tiltForScreen(rightRaised, screenRotationDegrees = 270))
    }

    @Test
    fun `an unknown screen rotation is treated as upright`() {
        assertTilt(pitch = 30f, roll = 0f, tilt = tiltForScreen(topRaisedMatrix(), screenRotationDegrees = 45))
    }

    private fun assertTilt(
        pitch: Float,
        roll: Float,
        tilt: Tilt,
    ) {
        assertEquals(pitch, tilt.pitchDegrees, tolerance)
        assertEquals(roll, tilt.rollDegrees, tolerance)
    }
}
