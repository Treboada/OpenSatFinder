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
import net.treboada.opensatfinder.pointing.GeostationarySatellite

/** The starred satellites, and the way to star or unstar one; a change is written at once. */
class FavouriteSatellites(
    val names: Set<String>,
    val toggle: (GeostationarySatellite) -> Unit,
)

/** Reads the stored favourites once and writes every change back. */
@Composable
fun rememberFavouriteSatellites(): FavouriteSatellites {
    val context = LocalContext.current
    val preferences = remember(context) { FavouritePreferences(context) }
    var names by remember(context) { mutableStateOf(preferences.names()) }
    return FavouriteSatellites(names = names) { satellite ->
        val updated =
            if (satellite.name in names) {
                names - satellite.name
            } else {
                names + satellite.name
            }
        names = updated
        preferences.save(updated)
    }
}
