/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.location.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VisibleSatellitesTest {
    private val equator = checkNotNull(GeoPoint.of(0.0, 0.0))
    private val lisbon = checkNotNull(GeoPoint.of(38.7223, -9.1393))

    private val astra = GeostationarySatellite(name = "ASTRA 1P (SES-24)", longitudeDegrees = 19.2)
    private val hotbird = GeostationarySatellite(name = "HOTBIRD 13E", longitudeDegrees = 13.0)
    private val far = GeostationarySatellite(name = "FAR WEST", longitudeDegrees = -170.0)

    private val catalogue = listOf(astra, hotbird, far)

    @Test
    fun `satellites below the horizon are left out`() {
        val visible = satellitesAbove(catalogue, lisbon)

        assertTrue(visible.none { it == far })
    }

    @Test
    fun `the ones high in the sky stay`() {
        val visible = satellitesAbove(catalogue, equator)

        assertEquals(listOf(astra, hotbird), visible)
    }

    @Test
    fun `the threshold is exclusive`() {
        // From 0N 0E ASTRA is 67.5 degrees up, so a threshold of 67.5 leaves it out.
        assertEquals(listOf(hotbird), satellitesAbove(catalogue, equator, minimumElevationDegrees = 67.5f))
    }

    @Test
    fun `below the threshold is not visible`() {
        assertEquals(emptyList<GeostationarySatellite>(), satellitesAbove(catalogue, equator, 80f))
    }

    @Test
    fun `without a position nothing can be called visible`() {
        assertEquals(emptyList<GeostationarySatellite>(), satellitesAbove(catalogue, observer = null))
    }

    @Test
    fun `the default threshold clears the horizon by five degrees`() {
        assertEquals(5f, VISIBLE_ELEVATION_DEGREES, 0.0001f)
    }
}
