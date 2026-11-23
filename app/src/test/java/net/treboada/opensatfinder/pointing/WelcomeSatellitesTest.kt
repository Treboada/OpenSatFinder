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

class WelcomeSatellitesTest {
    /** Galicia: 19.2E well up, the American slots far below the horizon. */
    private val galicia = checkNotNull(GeoPoint.of(latitudeDegrees = 43.3623, longitudeDegrees = -8.4115))

    private val astra = GeostationarySatellite(name = "ASTRA", longitudeDegrees = 19.2)
    private val hotbird = GeostationarySatellite(name = "HOT BIRD", longitudeDegrees = 13.0)
    private val directv = GeostationarySatellite(name = "DIRECTV", longitudeDegrees = -99.1)
    private val catalogue = listOf(astra, hotbird, directv)

    @Test
    fun `only the names asked for are offered`() {
        val offered = welcomeSatellites(catalogue = catalogue, names = setOf("ASTRA"), observer = galicia)

        assertEquals(listOf(astra), offered)
    }

    @Test
    fun `the ones behind the horizon are left out`() {
        val offered = welcomeSatellites(catalogue, names = setOf("ASTRA", "HOT BIRD", "DIRECTV"), observer = galicia)

        assertEquals(setOf("ASTRA", "HOT BIRD"), offered.map { it.name }.toSet())
    }

    @Test
    fun `the highest comes first`() {
        // From Galicia, 13E stands a little higher than 19.2E: the slot nearer the meridian wins.
        val offered = welcomeSatellites(catalogue, names = setOf("ASTRA", "HOT BIRD"), observer = galicia)

        assertEquals(listOf("HOT BIRD", "ASTRA"), offered.map { it.name })
    }

    @Test
    fun `without a position the whole list stands, by name`() {
        val offered = welcomeSatellites(catalogue, names = setOf("HOT BIRD", "ASTRA"), observer = null)

        assertEquals(listOf("ASTRA", "HOT BIRD"), offered.map { it.name })
    }

    @Test
    fun `a position that rules everything out still leaves the list`() {
        // From the pole the belt is on the horizon: a welcome with nothing to pick is a dead end.
        val arctic = checkNotNull(GeoPoint.of(latitudeDegrees = 85.0, longitudeDegrees = 0.0))

        val offered = welcomeSatellites(catalogue, names = setOf("ASTRA", "HOT BIRD"), observer = arctic)

        assertTrue("offered $offered", offered.isNotEmpty())
    }

    @Test
    fun `no names, nothing offered`() {
        val offered = welcomeSatellites(catalogue, names = emptySet(), observer = galicia)

        assertTrue(offered.isEmpty())
    }
}
