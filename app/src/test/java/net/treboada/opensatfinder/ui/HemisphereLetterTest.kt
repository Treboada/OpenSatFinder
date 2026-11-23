/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import net.treboada.opensatfinder.location.Hemisphere
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class HemisphereLetterTest {
    @Test
    fun `longitudes keep the letters every catalogue uses`() {
        assertEquals("E", hemisphereLetter(Hemisphere.East))
        assertEquals("W", hemisphereLetter(Hemisphere.West))
    }

    @Test
    fun `latitudes keep the letters every map uses`() {
        assertEquals("N", hemisphereLetter(Hemisphere.North))
        assertEquals("S", hemisphereLetter(Hemisphere.South))
    }

    @Test
    fun `the letters do not follow the language of the phone`() {
        val original = Locale.getDefault()
        try {
            for (language in listOf("es-ES", "eu-ES", "pt-PT", "gl-ES", "ca-ES", "fr-FR", "de-DE")) {
                Locale.setDefault(Locale.forLanguageTag(language))
                assertEquals(language, "E", hemisphereLetter(Hemisphere.East))
                assertEquals(language, "W", hemisphereLetter(Hemisphere.West))
            }
        } finally {
            Locale.setDefault(original)
        }
    }
}
