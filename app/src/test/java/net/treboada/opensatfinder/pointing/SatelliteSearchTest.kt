/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SatelliteSearchTest {
    private val catalogue =
        listOf(
            GeostationarySatellite(name = "ASTRA 1P (SES-24)", longitudeDegrees = 19.2),
            GeostationarySatellite(name = "ASTRA 1N", longitudeDegrees = 19.2),
            GeostationarySatellite(name = "HOTBIRD 13E", longitudeDegrees = 13.0),
        )

    @Test
    fun `an empty query returns the whole list`() {
        assertEquals(catalogue, searchSatellites("", catalogue))
        assertEquals(catalogue, searchSatellites("   ", catalogue))
    }

    @Test
    fun `names are matched without regard to case`() {
        val matches = searchSatellites("astra", catalogue)

        assertEquals(listOf("ASTRA 1P (SES-24)", "ASTRA 1N"), matches.map { it.name })
    }

    @Test
    fun `the query is matched anywhere in the name`() {
        val matches = searchSatellites("hotbird", catalogue)

        assertEquals(listOf("HOTBIRD 13E"), matches.map { it.name })
    }

    @Test
    fun `surrounding spaces in the query are ignored`() {
        assertEquals(searchSatellites("astra", catalogue), searchSatellites("  astra  ", catalogue))
    }

    @Test
    fun `a name nobody has gives an empty list`() {
        assertTrue(searchSatellites("no such satellite", catalogue).isEmpty())
    }

    @Test
    fun `the default list is the catalogue`() {
        assertEquals(searchSatellites("ASTRA 1P"), searchSatellites("ASTRA 1P", SatelliteCatalog.all))
    }
}
