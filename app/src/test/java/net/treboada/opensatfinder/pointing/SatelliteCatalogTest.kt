/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The catalogue is generated data, so these tests guard its shape: plausible count,
 * longitudes inside the valid range, an index that loses nothing, and lookups that
 * behave when a slot is shared or empty.
 */
class SatelliteCatalogTest {
    @Test
    fun `the catalogue carries the CelesTrak listing`() {
        assertTrue("catalogue looks truncated: ${SatelliteCatalog.all.size}", SatelliteCatalog.all.size > 500)
    }

    @Test
    fun `every name is filled in`() {
        val unnamed = SatelliteCatalog.all.filter { it.name.isBlank() }

        assertTrue("unnamed satellites: $unnamed", unnamed.isEmpty())
    }

    @Test
    fun `every longitude is inside the valid range`() {
        val broken = SatelliteCatalog.all.filter { it.longitudeDegrees !in -180.0..180.0 }

        assertTrue("longitudes out of range: $broken", broken.isEmpty())
    }

    @Test
    fun `the index accounts for every satellite`() {
        val slots = SatelliteCatalog.all.map { it.longitudeDegrees }.distinct()
        val indexed = slots.sumOf { SatelliteCatalog.at(it).size }

        assertEquals(SatelliteCatalog.all.size, indexed)
    }

    @Test
    fun `a slot can hold more than one satellite`() {
        // 19.2 east carries both ASTRA 1N and ASTRA 1P, which is why the index holds lists.
        val slot = SatelliteCatalog.at(19.2)

        assertTrue("expected co-located satellites at 19.2, got $slot", slot.size >= 2)
    }

    @Test
    fun `a longitude is matched to a tenth of a degree`() {
        assertEquals(SatelliteCatalog.at(19.2), SatelliteCatalog.at(19.20001))
        assertEquals(SatelliteCatalog.at(19.2), SatelliteCatalog.at(19.24))
    }

    @Test
    fun `a name finds its satellite`() {
        val astra = checkNotNull(SatelliteCatalog.named("ASTRA 1P (SES-24)"))

        assertEquals(19.2, astra.longitudeDegrees, 0.0001)
    }

    @Test
    fun `an unknown name finds nothing`() {
        assertNull(SatelliteCatalog.named("NOT A SATELLITE"))
    }

    @Test
    fun `the app starts on ASTRA 1P`() {
        val start = checkNotNull(DEFAULT_SATELLITE)

        assertEquals("ASTRA 1P (SES-24)", start.name)
        assertEquals(19.2, start.longitudeDegrees, 0.0001)
    }

    @Test
    fun `an empty slot returns nothing rather than failing`() {
        // 19.25 rounds to a slot nobody occupies unless the listing grows one.
        val loneliest = 0.05

        assertTrue(SatelliteCatalog.at(loneliest).isEmpty())
    }
}
