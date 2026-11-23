/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeoPointTest {
    @Test
    fun `a position keeps the coordinates it was given`() {
        val point = checkNotNull(GeoPoint.of(latitudeDegrees = 43.3623, longitudeDegrees = 8.4115))

        assertEquals(43.3623, point.latitudeDegrees, 0.0)
        assertEquals(8.4115, point.longitudeDegrees, 0.0)
    }

    @Test
    fun `hemispheres follow the sign of each coordinate`() {
        val northEast = checkNotNull(GeoPoint.of(43.3623, 8.4115))
        assertEquals(Hemisphere.North, northEast.latitudeHemisphere)
        assertEquals(Hemisphere.East, northEast.longitudeHemisphere)

        val southWest = checkNotNull(GeoPoint.of(-33.9, -70.6))
        assertEquals(Hemisphere.South, southWest.latitudeHemisphere)
        assertEquals(Hemisphere.West, southWest.longitudeHemisphere)
    }

    @Test
    fun `the equator and the prime meridian count as north and east`() {
        val origin = checkNotNull(GeoPoint.of(0.0, 0.0))

        assertEquals(Hemisphere.North, origin.latitudeHemisphere)
        assertEquals(Hemisphere.East, origin.longitudeHemisphere)
    }

    @Test
    fun `the corners of the world are valid`() {
        assertEquals(Hemisphere.North, checkNotNull(GeoPoint.of(90.0, 180.0)).latitudeHemisphere)
        assertEquals(Hemisphere.East, checkNotNull(GeoPoint.of(90.0, 180.0)).longitudeHemisphere)
        assertEquals(Hemisphere.South, checkNotNull(GeoPoint.of(-90.0, -180.0)).latitudeHemisphere)
        assertEquals(Hemisphere.West, checkNotNull(GeoPoint.of(-90.0, -180.0)).longitudeHemisphere)
    }

    @Test
    fun `a broken fix is refused instead of being clamped`() {
        assertNull(GeoPoint.of(latitudeDegrees = 91.0, longitudeDegrees = 0.0))
        assertNull(GeoPoint.of(latitudeDegrees = -91.0, longitudeDegrees = 0.0))
        assertNull(GeoPoint.of(latitudeDegrees = 0.0, longitudeDegrees = 181.0))
        assertNull(GeoPoint.of(latitudeDegrees = 0.0, longitudeDegrees = -181.0))
    }

    @Test
    fun `positions compare by their coordinates`() {
        assertEquals(GeoPoint.of(43.3623, 8.4115), GeoPoint.of(43.3623, 8.4115))
        assertEquals(GeoPoint.of(43.3623, 8.4115).hashCode(), GeoPoint.of(43.3623, 8.4115).hashCode())
        assertNotEquals(GeoPoint.of(43.3623, 8.4115), GeoPoint.of(43.3623, 8.4116))
    }
}
