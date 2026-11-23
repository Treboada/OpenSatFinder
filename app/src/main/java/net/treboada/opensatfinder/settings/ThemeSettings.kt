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

/** The chosen theme and the way to change it; a change is written at once. */
class ThemeSettings(
    val mode: ThemeMode,
    val setMode: (ThemeMode) -> Unit,
)

/**
 * Reads the stored theme once and writes every change back. Read above the theme itself: the
 * scheme is decided by this value, so it cannot come from inside the screens it paints.
 */
@Composable
fun rememberThemeSettings(): ThemeSettings {
    val context = LocalContext.current
    val preferences = remember(context) { ThemePreferences(context) }
    var mode by remember(context) { mutableStateOf(preferences.mode) }
    return ThemeSettings(mode = mode) { chosen ->
        mode = chosen
        preferences.mode = chosen
    }
}
