/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.compass.Heading
import net.treboada.opensatfinder.location.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PointedLongitudeTest {
    private val tolerance = 0.2f
    private val astra = GeostationarySatellite(name = "ASTRA 1P (SES-24)", longitudeDegrees = 19.2)

    private fun point(
        latitude: Double,
        longitude: Double,
    ): GeoPoint = checkNotNull(GeoPoint.of(latitude, longitude))

    @Test
    fun `aimed at the satellite the reading is its own longitude`() {
        // Everyone but the equator, where aiming east lies inside the equatorial plane.
        val observers =
            listOf(
                point(5.0, 0.0),
                point(38.7223, -9.1393),
                point(60.0, 10.0),
                point(-45.0, 0.0),
            )

        observers.forEach { observer ->
            val look = satelliteLook(observer, astra)
            val longitude = pointedLongitude(observer, Heading.of(look.azimuthDegrees), look.elevationDegrees)

            assertEquals("from ${observer.latitudeDegrees}", 19.2f, checkNotNull(longitude), tolerance)
        }
    }

    @Test
    fun `on the equator aiming along it falls back to your own meridian`() {
        // From 0N 0E the satellite is due east, and neither the sight line nor the great
        // circle leaves the equatorial plane; the only meridian with a claim is your own.
        val look = satelliteLook(point(0.0, 0.0), astra)

        assertEquals(
            0f,
            pointedLongitude(point(0.0, 0.0), Heading.of(look.azimuthDegrees), look.elevationDegrees),
            tolerance,
        )
    }

    @Test
    fun `pointing due south from the north reaches the equator on the same meridian`() {
        val longitude = pointedLongitude(point(45.0, 0.0), Heading.of(180f), elevationDegrees = 0f)

        assertEquals(0f, checkNotNull(longitude), tolerance)
    }

    @Test
    fun `meridians travel with the observer`() {
        val longitude = pointedLongitude(point(45.0, 100.0), Heading.of(180f), elevationDegrees = 0f)

        assertEquals(100f, checkNotNull(longitude), tolerance)
    }

    @Test
    fun `pointing north from the south reaches the equator on the same meridian`() {
        val longitude = pointedLongitude(point(-45.0, 30.0), Heading.of(0f), elevationDegrees = 0f)

        assertEquals(30f, checkNotNull(longitude), tolerance)
    }

    @Test
    fun `a southern observer aiming east with a downward tilt lands east of where they stand`() {
        val longitude = pointedLongitude(point(-30.0, 0.0), Heading.of(90f), elevationDegrees = -10f)

        assertEquals(true, checkNotNull(longitude) > 0f)
    }

    @Test
    fun `looking straight up falls back to the meridian the heading points at`() {
        // The sight line never reaches the equator, so the great circle due north decides:
        // it crosses at the meridian opposite the observer.
        assertEquals(180f, pointedLongitude(point(45.0, 0.0), Heading.of(0f), elevationDegrees = 90f), 90f)
    }

    @Test
    fun `level along a parallel still reads a meridian`() {
        // Heading due east from 45N, the great circle meets the equator ninety degrees on.
        assertEquals(90f, pointedLongitude(point(45.0, 0.0), Heading.of(90f), elevationDegrees = 0f), tolerance)
    }

    @Test
    fun `the reading survives looking high in the sky`() {
        // The reported bug: above about 47 degrees at this latitude the sight line stops
        // reaching the equator, and the reading used to disappear.
        val observer = point(38.7223, -9.1393)
        val readings =
            (0..90 step 5).map { elevation ->
                pointedLongitude(observer, Heading.of(150f), elevationDegrees = elevation.toFloat())
            }

        assertEquals(19, readings.size)
        assertTrue(readings.all { it in -180f..180f })
    }

    @Test
    fun `the reading wraps onto the western half`() {
        // Due south from 170 east crosses the equator east of the meridian... and wraps.
        val longitude = pointedLongitude(point(45.0, 170.0), Heading.of(180f), elevationDegrees = 0f)

        assertEquals(170f, checkNotNull(longitude), tolerance)
    }
}
