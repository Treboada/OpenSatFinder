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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * The magnifier in the search chip, drawn rather than imported for the same reason as the
 * gear and the star: a lens and a handle is all a glyph this size can carry.
 */
@Composable
fun SearchIcon(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.11f
        drawCircle(
            color = color,
            radius = size.minDimension * 0.31f,
            center = Offset(x = size.width * 0.43f, y = size.height * 0.43f),
            style = Stroke(width = stroke),
        )
        drawLine(
            color = color,
            start = Offset(x = size.width * 0.65f, y = size.height * 0.65f),
            end = Offset(x = size.width * 0.87f, y = size.height * 0.87f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
    }
}
