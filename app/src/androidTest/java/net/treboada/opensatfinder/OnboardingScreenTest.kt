/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.treboada.opensatfinder.pointing.GeostationarySatellite
import net.treboada.opensatfinder.ui.OnboardingScreen
import net.treboada.opensatfinder.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun text(
        @StringRes id: Int,
        vararg args: Any,
    ): String = context.getString(id, *args)

    private val astra = GeostationarySatellite(name = "ASTRA", longitudeDegrees = 19.2)
    private val hotbird = GeostationarySatellite(name = "HOT BIRD", longitudeDegrees = 13.0)

    private fun show(
        onFinish: () -> Unit = {},
        onExit: (() -> Unit)? = null,
        candidates: List<GeostationarySatellite> = emptyList(),
        selected: GeostationarySatellite? = null,
        onSelect: (GeostationarySatellite) -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme {
                OnboardingScreen(
                    onFinish = onFinish,
                    onExit = onExit,
                    candidates = candidates,
                    selected = selected,
                    onSelect = onSelect,
                )
            }
        }
    }

    @Test
    fun itOpensAskingForASatellite() {
        show()

        composeRule
            .onNodeWithTag("onboarding_title")
            .assertTextEquals(text(R.string.onboarding_choose_title))
        composeRule
            .onNodeWithTag("onboarding_next")
            .assertTextEquals(text(R.string.onboarding_next))
    }

    @Test
    fun theSecondStepExplainsTheArrows() {
        show()

        composeRule.onNodeWithTag("onboarding_next").performClick()

        composeRule
            .onNodeWithTag("onboarding_title")
            .assertTextEquals(text(R.string.onboarding_turn_title))
        composeRule
            .onNodeWithTag("onboarding_next")
            .assertTextEquals(text(R.string.onboarding_start))
    }

    @Test
    fun theLastStepFinishesIt() {
        var finished = 0
        show(onFinish = { finished++ })

        composeRule.onNodeWithTag("onboarding_next").performClick()
        assertEquals(0, finished)

        composeRule.onNodeWithTag("onboarding_next").performClick()
        assertEquals(1, finished)
    }

    @Test
    fun theFirstLaunchHasNoWayOutButForwards() {
        show()

        composeRule.onNodeWithTag("onboarding_back").assertDoesNotExist()
    }

    @Test
    fun openedFromTheSettingsItCanBeLeft() {
        var exits = 0
        show(onExit = { exits++ })

        composeRule.onNodeWithTag("onboarding_back").performClick()

        assertEquals(1, exits)
    }

    @Test
    fun theFirstPageWaitsUntilASatelliteIsPicked() {
        show(candidates = listOf(astra, hotbird))

        composeRule.onNodeWithTag("onboarding_next").assertIsNotEnabled()
    }

    @Test
    fun aPickedSatelliteLetsThePageTurn() {
        show(candidates = listOf(astra, hotbird), selected = astra)

        composeRule.onNodeWithTag("onboarding_next").assertIsEnabled()
        composeRule.onNodeWithTag("onboarding_satellite").assertTextEquals("ASTRA")
    }

    @Test
    fun theChooserOffersTheCandidatesAndHandsBackThePick() {
        val picks = mutableListOf<GeostationarySatellite>()
        show(candidates = listOf(astra, hotbird), onSelect = { picks += it })

        composeRule.onNodeWithTag("onboarding_satellite").performClick()
        composeRule.onNodeWithText("HOT BIRD").performClick()

        assertEquals(listOf(hotbird), picks)
    }

    @Test
    fun theDotsSayWhichStepThisIs() {
        show()

        composeRule
            .onNodeWithTag("onboarding_step")
            .assertContentDescriptionEquals(text(R.string.onboarding_step, 1, 2))
    }
}
