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
import kotlin.math.cos
import kotlin.math.sin

private const val TEETH = 8

/**
 * A cog, drawn rather than imported: the Material icon set is not on the classpath and a
 * whole dependency for one glyph is not a trade worth making in this project.
 */
@Composable
fun GearIcon(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val bodyRadius = radius * 0.62f
        val toothWidth = radius * 0.30f

        repeat(TEETH) { index ->
            val angle = Math.toRadians(index * (360.0 / TEETH))
            val inner =
                Offset(
                    x = center.x + (bodyRadius * cos(angle)).toFloat(),
                    y = center.y + (bodyRadius * sin(angle)).toFloat(),
                )
            val outer =
                Offset(
                    x = center.x + (radius * cos(angle)).toFloat(),
                    y = center.y + (radius * sin(angle)).toFloat(),
                )
            drawLine(color = color, start = inner, end = outer, strokeWidth = toothWidth, cap = StrokeCap.Round)
        }
        drawCircle(color = color, radius = bodyRadius, style = Stroke(width = toothWidth))
    }
}
