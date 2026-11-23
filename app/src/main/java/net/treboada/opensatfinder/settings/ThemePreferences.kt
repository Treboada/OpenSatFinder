/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.settings

import android.content.Context
import androidx.core.content.edit

/** The theme the reader chose, kept with the rest of the app's settings. */
class ThemePreferences(
    context: Context,
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    var mode: ThemeMode
        get() = ThemeMode.stored(preferences.getString(KEY_THEME_MODE, null))
        set(value) {
            preferences.edit { putString(KEY_THEME_MODE, value.name) }
        }

    fun clear() {
        preferences.edit { remove(KEY_THEME_MODE) }
    }

    private companion object {
        const val KEY_THEME_MODE = "theme_mode"
    }
}
