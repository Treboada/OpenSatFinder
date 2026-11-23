/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.settings

/** Which scheme the app wears: the phone's choice, or one pinned by hand. */
enum class ThemeMode {
    System,
    Light,
    Dark,
    ;

    /** Whether this mode means a dark scheme, given what the phone says. */
    fun isDark(systemInDarkTheme: Boolean): Boolean =
        when (this) {
            System -> systemInDarkTheme
            Light -> false
            Dark -> true
        }

    companion object {
        /** The mode a stored name stands for; anything unrecognised falls back to the phone's. */
        fun stored(name: String?): ThemeMode = entries.firstOrNull { entry -> entry.name == name } ?: System
    }
}
