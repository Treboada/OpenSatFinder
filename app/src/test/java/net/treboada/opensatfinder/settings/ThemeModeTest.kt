/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeModeTest {
    @Test
    fun `following the phone takes whatever it says`() {
        assertTrue(ThemeMode.System.isDark(systemInDarkTheme = true))
        assertFalse(ThemeMode.System.isDark(systemInDarkTheme = false))
    }

    @Test
    fun `a pinned theme ignores the phone`() {
        assertFalse(ThemeMode.Light.isDark(systemInDarkTheme = true))
        assertTrue(ThemeMode.Dark.isDark(systemInDarkTheme = false))
    }

    @Test
    fun `a stored name comes back, and anything else is the phone's choice`() {
        assertEquals(ThemeMode.Dark, ThemeMode.stored("Dark"))
        assertEquals(ThemeMode.Light, ThemeMode.stored("Light"))
        assertEquals(ThemeMode.System, ThemeMode.stored("System"))
        assertEquals(ThemeMode.System, ThemeMode.stored(null))
        assertEquals(ThemeMode.System, ThemeMode.stored("Purple"))
    }
}
