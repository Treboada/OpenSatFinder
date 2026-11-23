/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class TiltTest {
    private val tolerance = 0.001f

    @Test
    fun `a tilt keeps the angles it was given`() {
        val tilt = Tilt.of(pitchDegrees = 12f, rollDegrees = -3f)

        assertEquals(12f, tilt.pitchDegrees, tolerance)
        assertEquals(-3f, tilt.rollDegrees, tolerance)
    }

    @Test
    fun `angles outside their range are clamped`() {
        val tilt = Tilt.of(pitchDegrees = 120f, rollDegrees = 400f)

        assertEquals(90f, tilt.pitchDegrees, tolerance)
        assertEquals(180f, tilt.rollDegrees, tolerance)
    }

    @Test
    fun `level is decided per axis and only then overall`() {
        assertTrue(Tilt.of(0.4f, -0.9f).isLevel)
        assertTrue(Tilt.of(0.4f, -0.9f).isPitchLevel)
        assertTrue(Tilt.of(0.4f, -0.9f).isRollLevel)

        assertFalse(Tilt.of(1.2f, 0f).isPitchLevel)
        assertTrue(Tilt.of(1.2f, 0f).isRollLevel)
        assertFalse(Tilt.of(1.2f, 0f).isLevel)

        assertFalse(Tilt.of(0f, 1.2f).isRollLevel)
        assertFalse(Tilt.of(0f, 1.2f).isLevel)
    }

    @Test
    fun `equal tilts compare equal`() {
        assertEquals(Tilt.of(10f, 5f), Tilt.of(10f, 5f))
        assertEquals(Tilt.of(10f, 5f).hashCode(), Tilt.of(10f, 5f).hashCode())
    }

    @Test
    fun `the first tilt sample is taken as it is`() {
        val smoother = TiltSmoother(factor = 0.5f)
        val sample = Tilt.of(10f, -20f)

        assertTilt(pitch = 10f, roll = -20f, tilt = smoother.next(sample))
    }

    @Test
    fun `later samples move a fixed fraction towards the target`() {
        val smoother = TiltSmoother(factor = 0.5f)
        smoother.next(Tilt.of(0f, 0f))

        assertTilt(pitch = 5f, roll = 10f, tilt = smoother.next(Tilt.of(10f, 20f)))
        assertTilt(pitch = 7.5f, roll = 15f, tilt = smoother.next(Tilt.of(10f, 20f)))
    }

    @Test
    fun `samples converge on the target`() {
        val smoother = TiltSmoother(factor = 0.5f)
        smoother.next(Tilt.of(0f, 0f))

        var tilt = Tilt.of(0f, 0f)
        repeat(20) { tilt = smoother.next(Tilt.of(-45f, 30f)) }

        assertTilt(pitch = -45f, roll = 30f, tilt = tilt)
    }

    @Test
    fun `reset makes the next sample jump instead of gliding`() {
        val smoother = TiltSmoother(factor = 0.5f)
        smoother.next(Tilt.of(0f, 0f))

        smoother.reset()

        assertTilt(pitch = 30f, roll = 0f, tilt = smoother.next(Tilt.of(30f, 0f)))
    }

    @Test
    fun `a useless factor is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { TiltSmoother(factor = 0f) }
        assertThrows(IllegalArgumentException::class.java) { TiltSmoother(factor = 1.5f) }
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
