/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.settings

import net.treboada.opensatfinder.pointing.SatelliteCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultFavouritesTest {
    @Test
    fun `every default satellite is one the catalogue carries`() {
        val catalogue = SatelliteCatalog.all.map { it.name }.toSet()

        assertEquals(
            "the catalogue no longer carries these defaults",
            emptySet<String>(),
            DEFAULT_FAVOURITES - catalogue,
        )
    }

    @Test
    fun `the defaults are spread over the sky, not clustered on one side`() {
        val longitudes = SatelliteCatalog.all.filter { it.name in DEFAULT_FAVOURITES }.map { it.longitudeDegrees }

        assertTrue("only ${longitudes.size} defaults resolve", longitudes.size >= 10)
        assertTrue("west of Greenwich: $longitudes", longitudes.any { it < -20f })
        assertTrue("east of Greenwich: $longitudes", longitudes.any { it > 20f })
    }
}
