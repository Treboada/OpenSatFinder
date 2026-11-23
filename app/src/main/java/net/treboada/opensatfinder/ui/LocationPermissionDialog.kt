/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import net.treboada.opensatfinder.R

/**
 * Why the position is asked for, and the way to grant it.
 *
 * The instrument gets the satellite's elevation, and the correction from magnetic to true
 * north, out of the observer's position: without it there is a heading and nothing to aim
 * at. The dialog explains that before the system's own prompt, which on its own says
 * nothing about why, and the request is fired from here rather than at launch so the two
 * arrive together.
 *
 * There is no way past it but granting: no dismiss button, no back, no tap outside. An
 * instrument that cannot compute an elevation is not a version of this app worth opening.
 */
@Composable
fun LocationPermissionDialog(onRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(R.string.location_permission_title)) },
        text = { Text(stringResource(R.string.location_permission_message)) },
        confirmButton = {
            TextButton(
                onClick = onRequest,
                modifier = Modifier.testTag("location_permission_allow"),
            ) {
                Text(stringResource(R.string.location_allow))
            }
        },
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
    )
}
