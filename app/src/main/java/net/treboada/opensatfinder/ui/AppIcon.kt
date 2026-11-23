/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.treboada.opensatfinder.R

/**
 * The launcher icon, drawn from its two vector layers and cropped the way a launcher crops
 * it: an adaptive icon is a 108 dp canvas of which only the middle 72 dp is guaranteed to
 * show. The layers fill the box - the whole canvas squeezed into it - and the box is then
 * enlarged about its own centre, which leaves exactly that middle window on show.
 *
 * Scaling about the centre, rather than sizing the layers larger and nudging them, is the
 * point: an offset is a number that can be wrong, and a scaled layer cannot drift.
 */
@Composable
internal fun AppIcon(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(RoundedCornerShape(size * ICON_CORNER_FRACTION)),
    ) {
        Box(
            modifier =
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = ADAPTIVE_CROP
                        scaleY = ADAPTIVE_CROP
                    },
        ) {
            for (drawable in ICON_LAYERS) {
                Image(
                    painter = painterResource(drawable),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                )
            }
        }
    }
}

/** 108 dp of canvas over the 72 dp a launcher guarantees to show. */
private const val ADAPTIVE_CROP = 1.5f

/** Background first, then the artwork: the two layers of the adaptive icon. */
private val ICON_LAYERS =
    listOf(
        R.drawable.ic_launcher_background,
        R.drawable.ic_launcher_foreground,
    )

/** How round a launcher's mask usually is, as a fraction of the icon's side. */
private const val ICON_CORNER_FRACTION = 0.22f
