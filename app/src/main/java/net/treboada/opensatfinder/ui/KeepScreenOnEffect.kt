/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

/**
 * Holds the window flag that stops the screen timing out, for as long as the setting
 * says so and the activity is visible. The platform drops the flag when the app leaves
 * the foreground, which is exactly the scope the setting is about.
 */
@Composable
fun KeepScreenOnEffect(enabled: Boolean) {
    val activity = LocalActivity.current
    DisposableEffect(activity, enabled) {
        val window = activity?.window
        if (enabled) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose { }
    }
}
