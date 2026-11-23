/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class NumberTextTest {
    private val english = Locale.forLanguageTag("en")
    private val spanish = Locale.forLanguageTag("es")
    private val german = Locale.forLanguageTag("de")
    private val french = Locale.forLanguageTag("fr")

    @Test
    fun `one decimal is kept and no other is shown`() {
        assertEquals("34.5", formatDecimal(34.456, english, decimals = 1))
        assertEquals("34.4", formatDecimal(34.44, english, decimals = 1))
    }

    @Test
    fun `a whole number still shows its decimal place`() {
        assertEquals("90.0", formatDecimal(90.0, english, decimals = 1))
        assertEquals("0.0", formatDecimal(0.0, english, decimals = 1))
    }

    @Test
    fun `no leading zeros are added`() {
        assertEquals("5.3", formatDecimal(5.3, english, decimals = 1))
        assertEquals("0.0", formatDecimal(0.04, english, decimals = 1))
    }

    @Test
    fun `the decimal separator follows the locale`() {
        assertEquals("34,5", formatDecimal(34.456, spanish, decimals = 1))
        assertEquals("34,5", formatDecimal(34.456, german, decimals = 1))
        assertEquals("34,5", formatDecimal(34.456, french, decimals = 1))
    }

    @Test
    fun `coordinates keep six decimals and no grouping`() {
        assertEquals("43.362300", formatDecimal(43.3623, english, decimals = 6))
        assertEquals("1234.500000", formatDecimal(1234.5, english, decimals = 6))
        assertEquals("43,362300", formatDecimal(43.3623, spanish, decimals = 6))
    }

    @Test
    fun `whole degrees drop the fraction when none is asked for`() {
        assertEquals("34", formatDecimal(34.456, english, decimals = 0))
    }
}
