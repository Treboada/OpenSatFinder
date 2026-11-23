/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * What an arrow is made of, as fractions of the box it is drawn in or of the radius its arc
 * spans. Every arrow on the screen - the two that say which way to turn and the one that says
 * whether to raise or drop the phone - is cut from these numbers, so they carry the same
 * weight whatever they point at.
 */
internal const val ARROW_RADIUS_FRACTION = 0.36f
internal const val ARROW_STROKE_FRACTION = 0.34f
private const val ARROW_HEAD_LENGTH_FRACTION = 0.75f
private const val ARROW_HEAD_HALF_WIDTH_FRACTION = 0.5f

/**
 * A curved arrow: an arc with a triangular head at its end, the head pointing the way the
 * arc travels. A positive sweep runs clockwise on screen, a negative one anticlockwise.
 */
internal fun DrawScope.drawCurvedArrow(
    color: Color,
    center: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
) {
    val strokeWidth = radius * ARROW_STROKE_FRACTION
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )

    val endAngle = startAngle + sweepAngle
    val endRadians = Math.toRadians(endAngle.toDouble())
    val end =
        Offset(
            x = center.x + (radius * cos(endRadians)).toFloat(),
            y = center.y + (radius * sin(endRadians)).toFloat(),
        )
    val tangentRadians = Math.toRadians((endAngle + if (sweepAngle > 0f) 90f else -90f).toDouble())
    val tangent = Offset(cos(tangentRadians).toFloat(), sin(tangentRadians).toFloat())
    val radial = Offset(cos(endRadians).toFloat(), sin(endRadians).toFloat())

    val headLength = radius * ARROW_HEAD_LENGTH_FRACTION
    val headHalfWidth = radius * ARROW_HEAD_HALF_WIDTH_FRACTION
    val head =
        Path().apply {
            moveTo(end.x + tangent.x * headLength, end.y + tangent.y * headLength)
            lineTo(end.x + radial.x * headHalfWidth, end.y + radial.y * headHalfWidth)
            lineTo(end.x - radial.x * headHalfWidth, end.y - radial.y * headHalfWidth)
            close()
        }
    drawPath(path = head, color = color)
}

/**
 * How heavy the tilt arrow's shaft is, as a fraction of the box it fills.
 *
 * The compass hangs that arrow beside a gauge that takes half the dial, inside a box four
 * tenths of the gauge, and gives it the stroke of a turn arrow drawn for a box 0.16 of the
 * dial: 0.16 x [ARROW_RADIUS_FRACTION] x [ARROW_STROKE_FRACTION] of the dial over 0.2 of it.
 * Every upright arrow in the app is drawn from this one number, so the welcome's picture and
 * the gauge beside the dial cannot end up with two different arrows.
 */
internal const val TILT_ARROW_STROKE_FRACTION: Float =
    0.16f * ARROW_RADIUS_FRACTION * ARROW_STROKE_FRACTION / 0.2f

/**
 * The upright arrow the compass hangs beside the elevation gauge, in a box of [size].
 *
 * A composable rather than a call to [drawUprightArrow] so the weight travels with the arrow
 * instead of being worked out again by each caller - which is how two arrows that were meant
 * to be one ended up drawn at different weights.
 */
@Composable
internal fun TiltArrow(
    color: Color,
    up: Boolean,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(size)) {
        drawUprightArrow(
            color = color,
            up = up,
            strokeWidth = (size * TILT_ARROW_STROKE_FRACTION).toPx(),
        )
    }
}

/**
 * An upright arrow, up or down: a shaft with a triangular head, the vertical cousin of
 * [drawCurvedArrow]. The shaft is the caller's to give, in the same width the turn arrows are
 * drawn with, so both carry the same weight however big the box it fills.
 */
internal fun DrawScope.drawUprightArrow(
    color: Color,
    up: Boolean,
    strokeWidth: Float,
) {
    val radius = size.minDimension * ARROW_RADIUS_FRACTION
    val headLength = radius * ARROW_HEAD_LENGTH_FRACTION
    val headHalfWidth = radius * ARROW_HEAD_HALF_WIDTH_FRACTION

    // The point reaches the edge of the box: a filled apex has nothing to keep in, so insetting
    // it by a stroke only made the arrow stop short of where it points. The tail does keep half
    // a stroke in, which is what its round cap needs; the shaft itself stops inside the head,
    // or its cap would bulge past the point.
    val tipY = if (up) 0f else size.height
    val tailY = if (up) size.height - strokeWidth / 2f else strokeWidth / 2f
    val shaftEndY = if (up) tipY + strokeWidth else tipY - strokeWidth
    drawLine(
        color = color,
        start = Offset(x = center.x, y = tailY),
        end = Offset(x = center.x, y = shaftEndY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round,
    )
    val baseY = if (up) tipY + headLength else tipY - headLength
    val head =
        Path().apply {
            moveTo(center.x, tipY)
            lineTo(center.x + headHalfWidth, baseY)
            lineTo(center.x - headHalfWidth, baseY)
            close()
        }
    drawPath(path = head, color = color)
}
