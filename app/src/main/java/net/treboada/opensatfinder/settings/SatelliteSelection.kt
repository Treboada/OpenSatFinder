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
import net.treboada.opensatfinder.pointing.DEFAULT_SATELLITE
import net.treboada.opensatfinder.pointing.GeostationarySatellite

/** The chosen satellite, and the way to change it; a change is written straight away. */
class SatelliteSelection(
    val satellite: GeostationarySatellite?,
    val select: (GeostationarySatellite) -> Unit,
)

/**
 * Reads the stored choice once, starts from the default when there is none, and writes
 * every new choice back, so the next launch opens on the same satellite.
 */
@Composable
fun rememberSatelliteSelection(): SatelliteSelection {
    val context = LocalContext.current
    val preferences = remember(context) { SatellitePreferences(context) }
    var satellite by remember(context) {
        mutableStateOf(preferences.selected() ?: DEFAULT_SATELLITE)
    }
    return SatelliteSelection(satellite = satellite) { chosen ->
        satellite = chosen
        preferences.save(chosen)
    }
}
