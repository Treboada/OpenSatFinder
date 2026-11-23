/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.treboada.opensatfinder.settings.ThemeMode
import net.treboada.opensatfinder.sound.SoundLevel
import net.treboada.opensatfinder.ui.SettingsScreen
import net.treboada.opensatfinder.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun text(
        @StringRes id: Int,
        vararg args: Any,
    ): String = context.getString(id, *args)

    private fun show(
        themeMode: ThemeMode = ThemeMode.System,
        keepScreenOn: Boolean = true,
        hideInvisibleSatellites: Boolean = false,
        soundLevel: Int = SoundLevel.FULL,
        onKeepScreenOnChange: (Boolean) -> Unit = {},
        onHideInvisibleSatellitesChange: (Boolean) -> Unit = {},
        onSoundLevelChange: (Int) -> Unit = {},
        onThemeModeChange: (ThemeMode) -> Unit = {},
        onOpenHelp: () -> Unit = {},
        onOpenAbout: () -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme(darkTheme = themeMode.isDark(systemInDarkTheme = false)) {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    keepScreenOn = keepScreenOn,
                    onKeepScreenOnChange = onKeepScreenOnChange,
                    hideInvisibleSatellites = hideInvisibleSatellites,
                    onHideInvisibleSatellitesChange = onHideInvisibleSatellitesChange,
                    soundLevel = soundLevel,
                    onSoundLevelChange = onSoundLevelChange,
                    onOpenHelp = onOpenHelp,
                    onOpenAbout = onOpenAbout,
                    onBack = onBack,
                )
            }
        }
    }

    @Test
    fun theThemeChosenIsTheChipThatIsSelected() {
        show(themeMode = ThemeMode.Dark)

        composeRule.onNodeWithTag("settings_theme_dark").assertIsSelected()
        composeRule.onNodeWithTag("settings_theme_light").assertIsNotSelected()
    }

    @Test
    fun pickingAThemeReportsIt() {
        var chosen: ThemeMode? = null
        show(onThemeModeChange = { chosen = it })

        composeRule.onNodeWithTag("settings_theme_light").performClick()

        assertEquals(ThemeMode.Light, chosen)
    }

    @Test
    fun theSwitchShowsTheStoredValue() {
        show(keepScreenOn = true)
        composeRule.onNodeWithTag("settings_keep_screen_on").assertIsOn()
    }

    @Test
    fun aStoredOffIsShownAsOff() {
        show(keepScreenOn = false)
        composeRule.onNodeWithTag("settings_keep_screen_on").assertIsOff()
    }

    @Test
    fun turningTheSwitchOffIsReported() {
        var reported: Boolean? = null
        show(keepScreenOn = true, onKeepScreenOnChange = { reported = it })

        composeRule.onNodeWithTag("settings_keep_screen_on").performClick()

        assertEquals(false, reported)
    }

    @Test
    fun turningTheSwitchOnIsReported() {
        var reported: Boolean? = null
        show(keepScreenOn = false, onKeepScreenOnChange = { reported = it })

        composeRule.onNodeWithTag("settings_keep_screen_on").performClick()

        assertEquals(true, reported)
    }

    @Test
    fun hidingTheInvisibleIsOffUnlessStoredOtherwise() {
        show()

        composeRule.onNodeWithTag("settings_hide_invisible").assertIsOff()
    }

    @Test
    fun aStoredOnIsShownAsOn() {
        show(hideInvisibleSatellites = true)

        composeRule.onNodeWithTag("settings_hide_invisible").assertIsOn()
    }

    @Test
    fun turningHidingTheInvisibleOnIsReported() {
        var reported: Boolean? = null
        show(onHideInvisibleSatellitesChange = { reported = it })

        composeRule.onNodeWithTag("settings_hide_invisible").performClick()

        assertEquals(true, reported)
    }

    @Test
    fun theSoundLevelIsWrittenBesideTheSlider() {
        show(soundLevel = SoundLevel.FULL)

        composeRule.onNodeWithText(text(R.string.settings_sound_percent, 100)).assertExists()
    }

    @Test
    fun theBottomOfTheSliderIsSilence() {
        show(soundLevel = SoundLevel.OFF)

        composeRule.onNodeWithText(text(R.string.settings_sound_off)).assertExists()
    }

    @Test
    fun movingTheSliderReportsThePosition() {
        var reported: Int? = null
        show(onSoundLevelChange = { reported = it })

        composeRule
            .onNodeWithTag("settings_sound")
            .performSemanticsAction(SemanticsActions.SetProgress) { set -> set(2f) }

        assertEquals(2, reported)
    }

    @Test
    fun thereIsNoTestToneRowAnyMore() {
        show()

        composeRule.onNodeWithTag("settings_test_tones").assertDoesNotExist()
    }

    @Test
    fun theSettingExplainsItself() {
        show()

        composeRule.onNodeWithText(text(R.string.settings_keep_screen_on)).assertExists()
        composeRule.onNodeWithText(text(R.string.settings_keep_screen_on_summary)).assertExists()
    }

    @Test
    fun theSettingsAreGrouped() {
        show()

        composeRule.onNodeWithTag("settings_section_screen").assertExists()
        composeRule.onNodeWithTag("settings_section_satellites").assertExists()
        composeRule.onNodeWithTag("settings_section_sound").assertExists()
    }

    @Test
    fun aboutComesLast() {
        show()

        val about = composeRule.onNodeWithTag("settings_about").fetchSemanticsNode().boundsInRoot
        val sound = composeRule.onNodeWithTag("settings_sound").fetchSemanticsNode().boundsInRoot

        assertTrue(
            "about sat at ${about.top}, the sound slider at ${sound.top}",
            about.top > sound.top,
        )
    }

    @Test
    fun theWelcomeIsOneTapAway() {
        var openings = 0
        show(onOpenHelp = { openings++ })

        composeRule.onNodeWithTag("settings_help").performClick()

        assertEquals(1, openings)
    }

    @Test
    fun theHelpComesJustBeforeAbout() {
        show()

        val help = composeRule.onNodeWithTag("settings_help").fetchSemanticsNode().boundsInRoot
        val about = composeRule.onNodeWithTag("settings_about").fetchSemanticsNode().boundsInRoot

        assertTrue(
            "help sat at ${help.top}, about at ${about.top}",
            help.top < about.top,
        )
    }

    @Test
    fun theAboutScreenIsOneTapAway() {
        var openings = 0
        show(onOpenAbout = { openings++ })

        composeRule.onNodeWithTag("settings_about").performClick()

        assertEquals(1, openings)
    }

    @Test
    fun theAboutRowSaysWhatItOpens() {
        show()

        composeRule.onNodeWithText(text(R.string.about)).assertExists()
    }

    @Test
    fun backLeavesTheScreen() {
        var backs = 0
        show(onBack = { backs++ })

        composeRule.onNodeWithTag("settings_back").performClick()

        assertEquals(1, backs)
    }
}
