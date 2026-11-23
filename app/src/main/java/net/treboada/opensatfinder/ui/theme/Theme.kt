/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui.theme

import android.graphics.drawable.ColorDrawable
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Vivid on purpose: this is the colour that says the phone is on the satellite, and it
 * has to be readable over the rose in both schemes without being confused with the red
 * the compass keeps for north.
 */
val AlignedColor = Color(0xFF00C853)

/**
 * Single place where the app's colour scheme is chosen, and the canvas it is drawn on.
 *
 * The [Surface] is not decoration: without something painting the background, every screen
 * shows whatever the window has behind it and every text falls back to the default content
 * colour, which is black whatever the scheme says. One surface gives all of them the right
 * ground and the right ink, in either theme.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(colorScheme = colorScheme) {
        WindowBehindTheCanvas(dark = darkTheme, background = colorScheme.background)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
            content = content,
        )
    }
}

/**
 * The window under the canvas. Its background and the status bar icon contrast have to follow
 * the theme the reader chose; the phone's setting is only one of the three answers, and the bar
 * icons would otherwise be white on a light canvas or black on a dark one.
 */
@Composable
private fun WindowBehindTheCanvas(
    dark: Boolean,
    background: Color,
) {
    val activity = LocalActivity.current ?: return
    val view = LocalView.current
    SideEffect {
        activity.window.setBackgroundDrawable(ColorDrawable(background.toArgb()))
        WindowCompat.getInsetsController(activity.window, view).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
}
