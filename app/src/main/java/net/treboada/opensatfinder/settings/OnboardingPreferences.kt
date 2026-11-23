/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.settings

import android.content.Context
import androidx.core.content.edit

/** Whether the welcome has been walked through, kept with the rest of the app's settings. */
class OnboardingPreferences(
    context: Context,
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    /**
     * False until the welcome is finished. Dismissing it by any other route - the system's
     * back gesture out of the app - is not finishing it, so it comes back.
     */
    var seen: Boolean
        get() = preferences.getBoolean(KEY_SEEN, false)
        set(value) {
            preferences.edit { putBoolean(KEY_SEEN, value) }
        }

    fun clear() {
        preferences.edit { remove(KEY_SEEN) }
    }

    private companion object {
        const val KEY_SEEN = "onboarding_seen"
    }
}
