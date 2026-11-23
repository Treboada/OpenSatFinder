/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.treboada.opensatfinder.settings.DEFAULT_FAVOURITES
import net.treboada.opensatfinder.settings.FavouritePreferences
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouritePreferencesTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences = FavouritePreferences(context)

    @Before
    fun clearBefore() {
        preferences.clear()
    }

    @After
    fun clearAfter() {
        preferences.clear()
    }

    @Test
    fun theWellKnownNeighbourhoodsAreStarredUntilSomeoneStarsAnother() {
        assertEquals(DEFAULT_FAVOURITES, FavouritePreferences(context).names())
    }

    @Test
    fun theStoredStarsAreReadBack() {
        preferences.save(setOf("ASTRA 1P (SES-24)", "HOTBIRD 13E"))

        assertEquals(
            setOf("ASTRA 1P (SES-24)", "HOTBIRD 13E"),
            FavouritePreferences(context).names(),
        )
    }

    @Test
    fun unstarringIsReadBackToo() {
        preferences.save(setOf("ASTRA 1P (SES-24)", "HOTBIRD 13E"))
        preferences.save(setOf("HOTBIRD 13E"))

        assertEquals(setOf("HOTBIRD 13E"), FavouritePreferences(context).names())
    }

    @Test
    fun clearingForgetsTheChoiceAndBringsTheDefaultsBack() {
        preferences.save(setOf("ASTRA 1P (SES-24)"))

        preferences.clear()

        assertEquals(DEFAULT_FAVOURITES, FavouritePreferences(context).names())
    }

    @Test
    fun unstarringEveryDefaultLeavesNothingStarred() {
        preferences.save(emptySet())

        assertTrue(FavouritePreferences(context).names().isEmpty())
    }
}
