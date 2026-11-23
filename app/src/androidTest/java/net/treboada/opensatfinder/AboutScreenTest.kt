/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.treboada.opensatfinder.ui.AUTHOR_EMAIL
import net.treboada.opensatfinder.ui.AUTHOR_NAME
import net.treboada.opensatfinder.ui.AboutScreen
import net.treboada.opensatfinder.ui.SOURCE_NAME
import net.treboada.opensatfinder.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun text(
        @StringRes id: Int,
        vararg args: Any,
    ): String = context.getString(id, *args)

    private fun show(onBack: () -> Unit = {}) {
        composeRule.setContent {
            AppTheme {
                AboutScreen(onBack = onBack)
            }
        }
    }

    @Test
    fun theScreenNamesTheApp() {
        show()

        composeRule.onNodeWithText(text(R.string.about)).assertExists()
        composeRule.onNodeWithText(text(R.string.app_name)).assertExists()
    }

    @Test
    @Suppress("DEPRECATION")
    fun theVersionIsTheOneInstalled() {
        val installed =
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                .orEmpty()
        show()

        composeRule
            .onNodeWithTag("about_version")
            .assertTextEquals(text(R.string.about_version, installed))
    }

    @Test
    fun theIconIsOnTheScreen() {
        show()

        composeRule.onNodeWithTag("about_icon").assertExists()
    }

    @Test
    fun thePromisesAreOnTheScreen() {
        show()

        composeRule.onNodeWithText(text(R.string.about_purpose)).assertExists()
        composeRule.onNodeWithText(text(R.string.about_location)).assertExists()
        composeRule.onNodeWithText(text(R.string.about_catalogue)).assertExists()
    }

    @Test
    fun theParagraphsAreHeaded() {
        show()

        composeRule.onNodeWithText(text(R.string.about_heading_how)).assertExists()
        composeRule.onNodeWithText(text(R.string.about_heading_privacy)).assertExists()
        composeRule.onNodeWithText(text(R.string.about_heading_thanks)).assertExists()
        composeRule.onNodeWithText(text(R.string.about_free)).assertExists()
    }

    @Test
    fun theAuthorIsNamed() {
        show()

        composeRule
            .onNodeWithTag("about_author")
            .assertTextEquals(text(R.string.about_author, AUTHOR_NAME, AUTHOR_EMAIL))
    }

    @Test
    fun theDonationIsInvited() {
        show()

        composeRule
            .onNodeWithTag("about_donate")
            .assertTextEquals(text(R.string.about_donate, text(R.string.about_donate_link)))
    }

    @Test
    fun theLicenceIsNamed() {
        show()

        composeRule
            .onNodeWithTag("about_licence")
            .assertTextEquals(text(R.string.about_licence, text(R.string.about_licence_name)))
    }

    @Test
    fun theSourceIsNamedAndLinked() {
        show()

        composeRule
            .onNodeWithTag("about_source")
            .assertTextEquals(text(R.string.about_source, SOURCE_NAME))
    }

    @Test
    fun backIsAnArrowThatStillSaysWhatItDoes() {
        show()

        composeRule
            .onNodeWithTag("about_back")
            .assertContentDescriptionEquals(text(R.string.back))
    }

    @Test
    fun backLeavesTheScreen() {
        var backs = 0
        show(onBack = { backs++ })

        composeRule.onNodeWithTag("about_back").performClick()

        assertEquals(1, backs)
    }
}
