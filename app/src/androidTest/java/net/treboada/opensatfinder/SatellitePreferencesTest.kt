/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.treboada.opensatfinder.pointing.GeostationarySatellite
import net.treboada.opensatfinder.pointing.SatelliteCatalog
import net.treboada.opensatfinder.settings.SatellitePreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SatellitePreferencesTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences = SatellitePreferences(context)

    @Before
    fun clearBefore() {
        preferences.clear()
    }

    @After
    fun clearAfter() {
        preferences.clear()
    }

    @Test
    fun nothingStoredMeansNoChoice() {
        assertNull(SatellitePreferences(context).selected())
    }

    @Test
    fun aChoiceIsReadBackByANewInstance() {
        val astra = SatelliteCatalog.at(19.2).first { it.name.startsWith("ASTRA 1P") }

        preferences.save(astra)

        assertEquals(astra, SatellitePreferences(context).selected())
    }

    @Test
    fun aNameTheCatalogueNoLongerCarriesIsIgnored() {
        preferences.save(GeostationarySatellite(name = "GONE 1", longitudeDegrees = 12.3))

        assertNull(SatellitePreferences(context).selected())
    }
}
