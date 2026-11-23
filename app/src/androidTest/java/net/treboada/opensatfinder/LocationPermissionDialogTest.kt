/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.treboada.opensatfinder.ui.LocationPermissionDialog
import net.treboada.opensatfinder.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationPermissionDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun text(
        @StringRes id: Int,
    ): String = context.getString(id)

    private fun show(onRequest: () -> Unit = {}) {
        composeRule.setContent {
            AppTheme {
                LocationPermissionDialog(onRequest = onRequest)
            }
        }
    }

    @Test
    fun theDialogSaysWhyThePositionIsWanted() {
        show()

        composeRule.onNodeWithText(text(R.string.location_permission_title)).assertExists()
        composeRule.onNodeWithText(text(R.string.location_permission_message)).assertExists()
    }

    @Test
    fun allowingAsksForThePermission() {
        var requests = 0
        show(onRequest = { requests++ })

        composeRule.onNodeWithTag("location_permission_allow").performClick()

        assertEquals(1, requests)
    }

    @Test
    fun thereIsNoWayPastItButGranting() {
        show()

        composeRule.onNodeWithTag("location_permission_later").assertDoesNotExist()
        composeRule.onNodeWithTag("location_permission_allow").assertExists()
    }
}
