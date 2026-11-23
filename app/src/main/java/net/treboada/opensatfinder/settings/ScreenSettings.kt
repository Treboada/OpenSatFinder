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

/** The display settings, and the way to change them; a change is written straight away. */
class ScreenSettings(
    val keepScreenOn: Boolean,
    val hideInvisibleSatellites: Boolean,
    val soundLevel: Int,
    val setKeepScreenOn: (Boolean) -> Unit,
    val setHideInvisibleSatellites: (Boolean) -> Unit,
    val setSoundLevel: (Int) -> Unit,
)

/** Reads the stored display settings once and writes every change back. */
@Composable
fun rememberScreenSettings(): ScreenSettings {
    val context = LocalContext.current
    val preferences = remember(context) { ScreenPreferences(context) }
    var keepScreenOn by remember(context) { mutableStateOf(preferences.keepScreenOn) }
    var hideInvisible by remember(context) { mutableStateOf(preferences.hideInvisibleSatellites) }
    var sound by remember(context) { mutableStateOf(preferences.soundLevel) }
    return ScreenSettings(
        keepScreenOn = keepScreenOn,
        hideInvisibleSatellites = hideInvisible,
        setKeepScreenOn = { enabled ->
            keepScreenOn = enabled
            preferences.keepScreenOn = enabled
        },
        setHideInvisibleSatellites = { enabled ->
            hideInvisible = enabled
            preferences.hideInvisibleSatellites = enabled
        },
        soundLevel = sound,
        setSoundLevel = { level ->
            sound = level
            preferences.soundLevel = level
        },
    )
}
