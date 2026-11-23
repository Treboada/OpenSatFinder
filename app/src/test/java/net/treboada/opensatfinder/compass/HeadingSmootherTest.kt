/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HeadingSmootherTest {
    private val tolerance = 0.001f

    @Test
    fun `the first sample is taken as it is`() {
        assertEquals(42f, HeadingSmoother(factor = 0.5f).next(42f), tolerance)
    }

    @Test
    fun `later samples move a fixed fraction towards the target`() {
        val smoother = HeadingSmoother(factor = 0.5f)

        smoother.next(0f)
        assertEquals(45f, smoother.next(90f), tolerance)
        assertEquals(67.5f, smoother.next(90f), tolerance)
    }

    @Test
    fun `samples converge on the target`() {
        val smoother = HeadingSmoother(factor = 0.5f)
        smoother.next(0f)

        var heading = 0f
        repeat(20) { heading = smoother.next(270f) }

        assertEquals(270f, heading, 0.1f)
    }

    @Test
    fun `crossing north takes the short way round`() {
        val smoother = HeadingSmoother(factor = 0.5f)
        smoother.next(350f)

        assertEquals(0f, smoother.next(10f), tolerance)
    }

    @Test
    fun `reset makes the next sample jump instead of gliding`() {
        val smoother = HeadingSmoother(factor = 0.5f)
        smoother.next(0f)

        smoother.reset()

        assertEquals(90f, smoother.next(90f), tolerance)
    }

    @Test
    fun `a useless factor is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { HeadingSmoother(factor = 0f) }
        assertThrows(IllegalArgumentException::class.java) { HeadingSmoother(factor = 1.5f) }
    }

    @Test
    fun `shortest turn is signed and bounded by half a turn`() {
        assertEquals(20f, shortestTurn(from = 350f, to = 10f), tolerance)
        assertEquals(-20f, shortestTurn(from = 10f, to = 350f), tolerance)
        assertEquals(-179f, shortestTurn(from = 0f, to = 181f), tolerance)
        assertEquals(0f, shortestTurn(from = 90f, to = 90f), tolerance)
    }
}
