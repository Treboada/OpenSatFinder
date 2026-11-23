/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/**
 * A five-pointed star, drawn rather than loaded: filled when the satellite is a favourite,
 * outlined when it is not, so the two states differ in shape as well as in colour.
 */
@Composable
fun StarIcon(
    filled: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val star =
            starPath(
                center = center,
                outerRadius = size.minDimension * 0.46f,
                innerRadius = size.minDimension * 0.19f,
            )
        if (filled) {
            drawPath(path = star, color = tint)
        } else {
            drawPath(
                path = star,
                color = tint,
                style = Stroke(width = size.minDimension * 0.1f, join = StrokeJoin.Round),
            )
        }
    }
}

/** Ten alternating points, starting at the top, closed into a star. */
private fun DrawScope.starPath(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
): Path {
    val path = Path()
    repeat(POINTS * 2) { index ->
        val radius = if (index % 2 == 0) outerRadius else innerRadius
        val radians = Math.toRadians(-90.0 + index * 180.0 / POINTS)
        val point =
            Offset(
                x = center.x + (radius * cos(radians)).toFloat(),
                y = center.y + (radius * sin(radians)).toFloat(),
            )
        if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
    }
    path.close()
    return path
}

private const val POINTS = 5
