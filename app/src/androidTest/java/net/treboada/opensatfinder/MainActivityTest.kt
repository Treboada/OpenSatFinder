/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import android.Manifest
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.treboada.opensatfinder.settings.OnboardingPreferences
import net.treboada.opensatfinder.settings.SatellitePreferences
import net.treboada.opensatfinder.settings.ScreenPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    /** Each test starts from the shipped defaults, however the previous one ended. */
    @Before
    fun startFromDefaults() {
        grantLocationPermission()
        SatellitePreferences(composeRule.activity).clear()
        ScreenPreferences(composeRule.activity).clear()
        // The welcome has been walked through in these tests, their subject is what comes after.
        OnboardingPreferences(composeRule.activity).seen = true
        composeRule.activityRule.scenario.recreate()
    }

    /**
     * The app will not go past its permission dialog until the position is granted - it has
     * no "not now" - so the tests grant it the way the shell can and then recreate, which is
     * also what a user does, one tap later.
     */
    private fun grantLocationPermission() {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val packageName = composeRule.activity.packageName
        for (permission in LOCATION_PERMISSIONS) {
            automation.executeShellCommand("pm grant $packageName $permission").close()
        }
    }

    @Test
    fun theAppOpensOnTheCompass() {
        composeRule.onNodeWithTag("compass_dial").assertExists()
    }

    @Test
    fun theWelcomeIsShownOnceAndLeadsToTheCompass() {
        OnboardingPreferences(composeRule.activity).clear()
        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithTag("compass_dial").assertDoesNotExist()

        // The first page asks which satellite, so it does not turn until one is picked.
        composeRule.onNodeWithTag("onboarding_satellite").performClick()
        composeRule.onAllNodesWithTag("onboarding_option")[0].performClick()

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.onNodeWithTag("onboarding_next").performClick()

        composeRule.onNodeWithTag("compass_dial").assertExists()
        assertTrue(OnboardingPreferences(composeRule.activity).seen)
    }

    @Test
    fun theSettingsOpenTheWelcomeAgainAndItLandsOnTheCompass() {
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.onNodeWithTag("settings_help").performClick()

        composeRule.onNodeWithTag("onboarding_title").assertExists()

        composeRule.onNodeWithTag("onboarding_next").performClick()
        composeRule.onNodeWithTag("onboarding_next").performClick()

        composeRule.onNodeWithTag("compass_dial").assertExists()
    }

    @Test
    fun theWelcomeOpenedFromTheSettingsComesBackToThem() {
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.onNodeWithTag("settings_help").performClick()

        composeRule.onNodeWithTag("onboarding_back").performClick()

        composeRule.onNodeWithTag("settings_section_screen").assertExists()
    }

    @Test
    fun theScreenIsKeptOnOnlyWhileTheCompassIsOnShow() {
        assertTrue(screenIsKeptOn())

        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.waitForIdle()
        assertFalse(screenIsKeptOn())

        composeRule.onNodeWithTag("settings_back").performClick()
        composeRule.waitForIdle()
        assertTrue(screenIsKeptOn())
    }

    private fun screenIsKeptOn(): Boolean =
        (composeRule.activity.window.attributes.flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0

    @Test
    fun theAppStartsOnAstra1P() {
        composeRule.onNodeWithTag("satellite_selector").assertTextContains("ASTRA 1P")
    }

    @Test
    fun theChosenSatelliteSurvivesAnActivityRecreation() {
        composeRule.onNodeWithTag("satellite_selector").performClick()
        composeRule.onNodeWithTag("satellite_search").performTextInput("HOTBIRD 13E")
        composeRule.onNodeWithText("HOTBIRD 13E").performClick()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithTag("satellite_selector").assertTextContains("HOTBIRD 13E")
    }

    @Test
    fun choosingASatelliteBringsItBackToTheCompass() {
        composeRule.onNodeWithTag("satellite_selector").performClick()

        composeRule.onNodeWithTag("satellite_search").performTextInput("HOTBIRD 13E")
        composeRule.onNodeWithText("HOTBIRD 13E").performClick()

        composeRule.onNodeWithTag("satellite_selector").assertTextContains("HOTBIRD 13E")
        composeRule.onNodeWithTag("compass_dial").assertExists()
    }

    @Test
    fun turningTheSettingOffClearsTheWindowFlag() {
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.onNodeWithTag("settings_keep_screen_on").performClick()
        composeRule.onNodeWithTag("settings_back").performClick()

        val flags = composeRule.activity.window.attributes.flags
        assertTrue(
            "expected the flag to be cleared, window flags were $flags",
            flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON == 0,
        )
    }

    @Test
    fun theAboutScreenOpensFromTheSettingsAndComesBackToThem() {
        composeRule.onNodeWithTag("settings_button").performClick()
        composeRule.onNodeWithTag("settings_about").performClick()

        composeRule.onNodeWithTag("about_back").performClick()

        // Back from the about screen lands on the settings, not on the compass.
        composeRule.onNodeWithTag("settings_keep_screen_on").assertExists()
    }

    @Test
    fun theAppIsLockedToPortrait() {
        assertEquals(
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
            composeRule.activity.requestedOrientation,
        )
    }

    @Test
    fun theScreenStaysOnWhileTheAppIsVisible() {
        val flags = composeRule.activity.window.attributes.flags

        assertTrue(
            "expected FLAG_KEEP_SCREEN_ON, window flags were $flags",
            flags and WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON != 0,
        )
    }
}

private val LOCATION_PERMISSIONS =
    listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
