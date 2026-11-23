/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

/**
 * A satellite, drawn rather than imported for the same reason as the gear: a body, two
 * solar panels and a mast, which is all a glyph at this size can carry anyway.
 */
@Composable
fun SatelliteIcon(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        drawRect(
            color = color,
            topLeft = Offset(width * 0.02f, height * 0.36f),
            size = Size(width * 0.28f, height * 0.28f),
        )
        drawRect(
            color = color,
            topLeft = Offset(width * 0.70f, height * 0.36f),
            size = Size(width * 0.28f, height * 0.28f),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(width * 0.34f, height * 0.24f),
            size = Size(width * 0.32f, height * 0.52f),
            cornerRadius = CornerRadius(width * 0.08f),
        )
        drawLine(
            color = color,
            start = Offset(width * 0.5f, height * 0.24f),
            end = Offset(width * 0.5f, height * 0.1f),
            strokeWidth = width * 0.07f,
            cap = StrokeCap.Round,
        )
        drawCircle(color = color, radius = width * 0.08f, center = Offset(width * 0.5f, height * 0.08f))
    }
}
