/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.settings

import android.content.Context
import androidx.core.content.edit
import net.treboada.opensatfinder.pointing.GeostationarySatellite
import net.treboada.opensatfinder.pointing.SatelliteCatalog

/**
 * Where the chosen satellite is kept between launches.
 *
 * The name is stored, not the whole record, and resolved through the catalogue when it
 * is read back: a name a newer catalogue no longer carries reads as no choice rather
 * than as a satellite that no longer exists.
 */
class SatellitePreferences(
    context: Context,
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    /** The stored satellite, or null when nothing is stored or the name is gone. */
    fun selected(): GeostationarySatellite? = preferences.getString(KEY_SATELLITE, null)?.let(SatelliteCatalog::named)

    fun save(satellite: GeostationarySatellite) {
        preferences.edit { putString(KEY_SATELLITE, satellite.name) }
    }

    /** Forgets the choice, so the app falls back to its default. */
    fun clear() {
        preferences.edit { clear() }
    }

    private companion object {
        const val KEY_SATELLITE = "selected_satellite"
    }
}
