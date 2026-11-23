/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The rose is only right if its bearings land where the screen puts them, so the
 * four cardinal points are pinned down here rather than left to the eye.
 */
class CompassGeometryTest {
    private val center = Offset(x = 100f, y = 100f)
    private val radius = 50f
    private val tolerance = 0.001f

    @Test
    fun `north is the top of the dial`() {
        assertPoint(expected = Offset(100f, 50f), actual = bearingPoint(center, radius, bearing = 0f))
    }

    @Test
    fun `east is the right of the dial`() {
        assertPoint(expected = Offset(150f, 100f), actual = bearingPoint(center, radius, bearing = 90f))
    }

    @Test
    fun `south is the bottom of the dial`() {
        assertPoint(expected = Offset(100f, 150f), actual = bearingPoint(center, radius, bearing = 180f))
    }

    @Test
    fun `west is the left of the dial`() {
        assertPoint(expected = Offset(50f, 100f), actual = bearingPoint(center, radius, bearing = 270f))
    }

    @Test
    fun `bearings wrap around a full turn`() {
        val north = bearingPoint(center, radius, bearing = 0f)
        val west = bearingPoint(center, radius, bearing = 270f)

        assertPoint(expected = north, actual = bearingPoint(center, radius, bearing = 360f))
        assertPoint(expected = west, actual = bearingPoint(center, radius, bearing = -90f))
    }

    private fun assertPoint(
        expected: Offset,
        actual: Offset,
    ) {
        assertEquals(expected.x, actual.x, tolerance)
        assertEquals(expected.y, actual.y, tolerance)
    }
}
