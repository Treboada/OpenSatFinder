/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.settings

import android.content.Context
import androidx.core.content.edit

/**
 * The settings that survive between launches: what the screen does and whether the
 * instrument makes a sound, which are the two things you reach for while holding the
 * phone up at the sky.
 *
 * Keeping the screen on and sounding the cues are both the default, because that is what
 * the app exists for.
 */
class ScreenPreferences(
    context: Context,
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    var keepScreenOn: Boolean
        get() = preferences.getBoolean(KEY_KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON)
        set(value) {
            preferences.edit { putBoolean(KEY_KEEP_SCREEN_ON, value) }
        }

    /** Leave out the satellites that sit below, or barely above, the horizon. */
    var hideInvisibleSatellites: Boolean
        get() = preferences.getBoolean(KEY_HIDE_INVISIBLE, DEFAULT_HIDE_INVISIBLE)
        set(value) {
            preferences.edit { putBoolean(KEY_HIDE_INVISIBLE, value) }
        }

    /**
     * How loud the cues are, as a position on the five-step slider: 0 is silent, 4 is full.
     * Anything stored outside the scale is clamped rather than trusted.
     */
    var soundLevel: Int
        get() = preferences.getInt(KEY_SOUND_LEVEL, DEFAULT_SOUND_LEVEL).coerceIn(0, SOUND_LEVEL_MAX)
        set(value) {
            preferences.edit { putInt(KEY_SOUND_LEVEL, value.coerceIn(0, SOUND_LEVEL_MAX)) }
        }

    fun clear() {
        preferences.edit {
            remove(KEY_KEEP_SCREEN_ON)
            remove(KEY_HIDE_INVISIBLE)
            remove(KEY_SOUND_LEVEL)
        }
    }

    companion object {
        const val DEFAULT_KEEP_SCREEN_ON = true

        /** On by default: a list of satellites that cannot be seen helps nobody. */
        const val DEFAULT_HIDE_INVISIBLE = true
        const val DEFAULT_SOUND_LEVEL = 4

        /** The top of the slider, spelled out here so the store does not import the UI's. */
        private const val SOUND_LEVEL_MAX = 4

        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_HIDE_INVISIBLE = "hide_invisible_satellites"
        private const val KEY_SOUND_LEVEL = "sound_level"
    }
}
