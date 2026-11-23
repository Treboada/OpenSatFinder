/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HeadingTest {
    private val tolerance = 0.001f

    @Test
    fun `normalize keeps values inside one turn`() {
        assertEquals(0f, normalizeDegrees(0f), tolerance)
        assertEquals(359f, normalizeDegrees(359f), tolerance)
    }

    @Test
    fun `normalize wraps values past one turn`() {
        assertEquals(0f, normalizeDegrees(360f), tolerance)
        assertEquals(5f, normalizeDegrees(725f), tolerance)
    }

    @Test
    fun `normalize wraps negative values`() {
        assertEquals(350f, normalizeDegrees(-10f), tolerance)
        assertEquals(180f, normalizeDegrees(-180f), tolerance)
    }

    @Test
    fun `cards cover the eight points with half-open sectors`() {
        assertEquals(CardinalDirection.North, CardinalDirection.of(0f))
        assertEquals(CardinalDirection.North, CardinalDirection.of(22.4f))
        assertEquals(CardinalDirection.NorthEast, CardinalDirection.of(22.5f))
        assertEquals(CardinalDirection.NorthEast, CardinalDirection.of(45f))
        assertEquals(CardinalDirection.NorthEast, CardinalDirection.of(67.4f))
        assertEquals(CardinalDirection.East, CardinalDirection.of(67.5f))
        assertEquals(CardinalDirection.East, CardinalDirection.of(90f))
        assertEquals(CardinalDirection.South, CardinalDirection.of(180f))
        assertEquals(CardinalDirection.West, CardinalDirection.of(270f))
        assertEquals(CardinalDirection.NorthWest, CardinalDirection.of(337.4f))
        assertEquals(CardinalDirection.North, CardinalDirection.of(337.5f))
        assertEquals(CardinalDirection.North, CardinalDirection.of(359.9f))
    }

    @Test
    fun `cards wrap past a full turn`() {
        assertEquals(CardinalDirection.NorthWest, CardinalDirection.of(-45f))
        assertEquals(CardinalDirection.East, CardinalDirection.of(450f))
    }

    @Test
    fun `a heading is normalised on construction`() {
        assertEquals(Heading.of(10f), Heading.of(370f))
        assertEquals(Heading.of(350f), Heading.of(-10f))
        assertEquals(0f, Heading.of(720f).degrees, tolerance)
    }

    @Test
    fun `a heading reports the point of the compass it falls in`() {
        assertEquals(CardinalDirection.East, Heading.of(90f).cardinal)
        assertEquals(CardinalDirection.SouthEast, Heading.of(135f).cardinal)
    }

    @Test
    fun `the shown bearing is rounded, not truncated`() {
        assertEquals(34.5f, Heading.of(34.46f).roundedDegrees(1), tolerance)
        assertEquals(34.4f, Heading.of(34.44f).roundedDegrees(1), tolerance)
    }

    @Test
    fun `a bearing just short of a full turn is shown as zero`() {
        assertEquals(0f, Heading.of(359.97f).roundedDegrees(1), tolerance)
        assertEquals(359.9f, Heading.of(359.94f).roundedDegrees(1), tolerance)
    }

    @Test
    fun `whole degrees wrap the same way`() {
        assertEquals(0f, Heading.of(359.6f).roundedDegrees(0), tolerance)
        assertEquals(90f, Heading.of(89.6f).roundedDegrees(0), tolerance)
    }

    @Test
    fun `a negative precision is refused`() {
        assertThrows(IllegalArgumentException::class.java) { Heading.of(10f).roundedDegrees(-1) }
    }

    @Test
    fun `the dial turns the other way from the bearing`() {
        assertEquals(-90f, Heading.of(90f).dialRotationDegrees, tolerance)
        assertEquals(-350f, Heading.of(350f).dialRotationDegrees, tolerance)
        assertEquals(0f, Heading.of(0f).dialRotationDegrees, tolerance)
    }
}
