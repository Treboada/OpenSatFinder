/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/** Whether the welcome is still owed, and the one action that settles the question. */
class Onboarding(
    val seen: Boolean,
    val markSeen: () -> Unit,
)

/**
 * Reads the welcome once and writes the answer back when it is finished. A welcome that
 * returns after being dismissed is worse than no welcome at all, so the write happens on
 * the button, not on the way in.
 */
@Composable
fun rememberOnboarding(): Onboarding {
    val context = LocalContext.current
    val preferences = remember(context) { OnboardingPreferences(context) }
    var seen by remember(context) { mutableStateOf(preferences.seen) }
    return Onboarding(seen = seen) {
        seen = true
        preferences.seen = true
    }
}
