/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.location.GeoPoint
import net.treboada.opensatfinder.location.Hemisphere
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin

/**
 * A geostationary satellite is a fixed point over the equator, so the expected
 * directions can be reasoned out: due south from a northern observer on the same
 * meridian, due north from a southern one, steeply up from the equator.
 */
class SatelliteLookTest {
    private val tolerance = 0.5f

    private val astra = GeostationarySatellite(name = "ASTRA 1P (SES-24)", longitudeDegrees = 19.2)

    private fun point(
        latitude: Double,
        longitude: Double,
    ): GeoPoint = checkNotNull(GeoPoint.of(latitude, longitude))

    @Test
    fun `from the equator the satellite due east is at ninety degrees`() {
        val look = satelliteLook(point(0.0, 0.0), GeostationarySatellite(name = "east", longitudeDegrees = 19.2))

        assertEquals(90f, look.azimuthDegrees, tolerance)
    }

    @Test
    fun `from the equator the satellite due west is at two hundred and seventy`() {
        val look = satelliteLook(point(0.0, 0.0), GeostationarySatellite(name = "west", longitudeDegrees = -19.2))

        assertEquals(270f, look.azimuthDegrees, tolerance)
    }

    @Test
    fun `north of the equator a satellite on the meridian is due south`() {
        val look = satelliteLook(point(45.0, 0.0), GeostationarySatellite(name = "meridian", longitudeDegrees = 0.0))

        assertEquals(180f, look.azimuthDegrees, tolerance)
    }

    @Test
    fun `south of the equator the same satellite is due north`() {
        val look = satelliteLook(point(-45.0, 0.0), GeostationarySatellite(name = "meridian", longitudeDegrees = 0.0))

        assertEquals(0f, look.azimuthDegrees, tolerance)
    }

    @Test
    fun `ASTRA 1P from Lisbon sits in the south-east`() {
        assertEquals(139.2f, satelliteLook(point(38.7223, -9.1393), astra).azimuthDegrees, tolerance)
    }

    @Test
    fun `a satellite straight overhead is ninety degrees up`() {
        val look = satelliteLook(point(0.0, 19.2), astra)

        assertEquals(90f, look.elevationDegrees, tolerance)
    }

    @Test
    fun `at the equator a satellite twenty degrees ahead is steeply up`() {
        assertEquals(67.5f, satelliteLook(point(0.0, 0.0), astra).elevationDegrees, tolerance)
    }

    @Test
    fun `ASTRA 1P from Lisbon is thirty-six degrees above the horizon`() {
        assertEquals(36.4f, satelliteLook(point(38.7223, -9.1393), astra).elevationDegrees, tolerance)
    }

    @Test
    fun `south of the equator the elevation follows the same geometry`() {
        val look = satelliteLook(point(-45.0, 0.0), GeostationarySatellite(name = "meridian", longitudeDegrees = 0.0))

        assertEquals(38.2f, look.elevationDegrees, tolerance)
    }

    @Test
    fun `a satellite on the far side of the world is below the horizon`() {
        val far = GeostationarySatellite(name = "far", longitudeDegrees = -170.0)

        assertTrue(satelliteLook(point(38.7223, -9.1393), far).elevationDegrees < 0f)
    }

    @Test
    fun `the vector geometry agrees with the classic elevation formula`() {
        // elevation = atan((cos arc - k) / sin arc), k = earth radius / orbit radius,
        // and arc from the spherical cosine rule cos(arc) = cos(lat) * cos(dLon).
        val cases =
            listOf(
                Triple(0.0, 0.0, 19.2),
                Triple(38.7223, -9.1393, 19.2),
                Triple(-45.0, 0.0, 0.0),
                Triple(60.0, 10.0, 19.2),
            )

        cases.forEach { (latitude, longitude, satelliteLongitude) ->
            val observer = point(latitude, longitude)
            val satellite = GeostationarySatellite(name = "case", longitudeDegrees = satelliteLongitude)
            val expected = formulaElevation(latitude, satelliteLongitude - longitude)

            assertEquals(
                "elevation from $latitude/$longitude",
                expected,
                satelliteLook(observer, satellite).elevationDegrees,
                0.1f,
            )
        }
    }

    @Test
    fun `ASTRA 1P is in the catalogue at 19 point 2 east`() {
        val catalogued = SatelliteCatalog.at(19.2).first { it.name.startsWith("ASTRA 1P") }

        assertEquals("ASTRA 1P (SES-24)", catalogued.name)
        assertEquals(19.2, catalogued.longitudeDegrees, 0.0001)
        assertEquals(Hemisphere.East, catalogued.longitudeHemisphere)
    }

    private fun formulaElevation(
        latitudeDegrees: Double,
        longitudeDifferenceDegrees: Double,
    ): Float {
        val latitude = Math.toRadians(latitudeDegrees)
        val difference = Math.toRadians(longitudeDifferenceDegrees)
        val arc = acos(cos(latitude) * cos(difference))
        val earthToOrbit = 6378.137 / 42164.17
        return Math.toDegrees(atan((cos(arc) - earthToOrbit) / sin(arc))).toFloat()
    }
}
