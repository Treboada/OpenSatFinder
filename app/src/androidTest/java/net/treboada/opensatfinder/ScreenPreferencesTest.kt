/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.treboada.opensatfinder.settings.ScreenPreferences
import net.treboada.opensatfinder.sound.SoundLevel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenPreferencesTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences = ScreenPreferences(context)

    @Before
    fun clearBefore() {
        preferences.clear()
    }

    @After
    fun clearAfter() {
        preferences.clear()
    }

    @Test
    fun keepingTheScreenOnIsTheDefault() {
        assertTrue(ScreenPreferences(context).keepScreenOn)
    }

    @Test
    fun aStoredOffIsReadBack() {
        preferences.keepScreenOn = false

        assertFalse(ScreenPreferences(context).keepScreenOn)
    }

    @Test
    fun hidingTheInvisibleIsOnByDefault() {
        assertTrue(ScreenPreferences(context).hideInvisibleSatellites)
    }

    @Test
    fun aStoredShowingIsReadBack() {
        preferences.hideInvisibleSatellites = false

        assertFalse(ScreenPreferences(context).hideInvisibleSatellites)
    }

    @Test
    fun theSoundStartsAtFullVolume() {
        assertEquals(SoundLevel.FULL, ScreenPreferences(context).soundLevel)
    }

    @Test
    fun aStoredLevelIsReadBack() {
        preferences.soundLevel = 1

        assertEquals(1, ScreenPreferences(context).soundLevel)
    }

    @Test
    fun aLevelOffTheScaleIsStoredClamped() {
        preferences.soundLevel = 9

        assertEquals(SoundLevel.FULL, ScreenPreferences(context).soundLevel)
    }

    @Test
    fun clearingGoesBackToTheDefault() {
        preferences.keepScreenOn = false
        preferences.hideInvisibleSatellites = false
        preferences.soundLevel = 0

        preferences.clear()

        assertTrue(ScreenPreferences(context).keepScreenOn)
        assertTrue(ScreenPreferences(context).hideInvisibleSatellites)
        assertEquals(SoundLevel.DEFAULT, ScreenPreferences(context).soundLevel)
    }
}
