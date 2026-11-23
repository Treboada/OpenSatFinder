/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import java.text.NumberFormat
import java.util.Locale

/**
 * A number the way the user's locale writes it: exactly [decimals] fraction digits,
 * the locale's decimal separator, and no thousands separator, which would only make
 * a bearing or a coordinate harder to read.
 */
fun formatDecimal(
    value: Double,
    locale: Locale,
    decimals: Int,
): String {
    val format =
        NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = decimals
            maximumFractionDigits = decimals
            isGroupingUsed = false
        }
    return format.format(value)
}

/** The primary locale the screen is being drawn in, tracked by Compose. */
@Composable
fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

/** A number for display: [decimals] places in the locale's own separator. */
@Composable
fun decimalText(
    value: Float,
    decimals: Int,
): String =
    formatDecimal(
        value = value.toDouble(),
        locale = currentLocale(),
        decimals = decimals,
    )
