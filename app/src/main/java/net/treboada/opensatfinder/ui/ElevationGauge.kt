/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import net.treboada.opensatfinder.ui.theme.AlignedColor
import kotlin.math.cos
import kotlin.math.sin

// Shared with the top of the dial, so both fixed marks are the same size.
internal val TARGET_RING_RADIUS = 13.dp

/** The moving head, until it has a target to land in. */
private val PHONE_HEAD_RADIUS = 9.dp
internal val TARGET_RING_WIDTH = 3.dp

/**
 * The sky above the horizon, as the upper-right quarter of a circle: 0° at the horizon on
 * the right, 90° straight up at the top.
 *
 * The moving mark — the phone's elevation — is a heavy dashed radius ending in a dot on
 * the arc, sliding along the curve as the phone tilts and green within two degrees of the
 * satellite. The satellite's is the same fixed ring the top of the dial wears, sitting on
 * the arc. Both are pinned to the horizon when they point below it.
 */
@Composable
fun ElevationGauge(
    phoneElevationDegrees: Float?,
    satelliteElevationDegrees: Float?,
    aligned: Boolean,
    modifier: Modifier = Modifier,
) {
    val arcColor = MaterialTheme.colorScheme.outline
    val phoneColor = if (aligned) AlignedColor else MaterialTheme.colorScheme.primary
    val satelliteColor = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier.testTag("elevation_gauge")) {
        val radius = size.minDimension * 0.88f
        val origin = Offset(x = 0f, y = size.height)
        val ring =
            Size(width = radius * 2f, height = radius * 2f)
        val ringTopLeft = Offset(x = origin.x - radius, y = origin.y - radius)

        drawArc(
            color = arcColor,
            startAngle = 0f,
            sweepAngle = -90f,
            useCenter = false,
            topLeft = ringTopLeft,
            size = ring,
            style = Stroke(width = 2.dp.toPx()),
        )
        drawTick(origin, radius, elevationDegrees = 0f, color = arcColor)
        drawTick(origin, radius, elevationDegrees = 90f, color = arcColor)

        satelliteElevationDegrees?.let { elevation ->
            // A ring on the arc, a little larger than the moving mark's dot so the dot can
            // sit inside it once they meet instead of hiding under it.
            drawCircle(
                color = satelliteColor,
                radius = TARGET_RING_RADIUS.toPx(),
                center = polar(origin, radius, elevationAngle(elevation)),
                style = Stroke(width = TARGET_RING_WIDTH.toPx()),
            )
        }

        phoneElevationDegrees?.let { elevation ->
            val angle = elevationAngle(elevation)
            // The moving mark, in the shape of the rose's target: a dashed radius ending
            // in a dot on the arc, so it slides along the curve as the phone tilts.
            drawLine(
                color = phoneColor,
                start = polar(origin, radius * 0.34f, angle),
                end = polar(origin, radius, angle),
                strokeWidth = 6.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16.dp.toPx(), 10.dp.toPx())),
            )
            // Aimed, the head swells to the ring's size and fills it; otherwise it stays
            // the small dot it always was.
            drawCircle(
                color = phoneColor,
                radius = (if (aligned) TARGET_RING_RADIUS else PHONE_HEAD_RADIUS).toPx(),
                center = polar(origin, radius, angle),
            )
        }
    }
}

/** Elevations are drawn from the horizon up, so a negative one sits on the horizon. */
private fun elevationAngle(elevationDegrees: Float): Float = -elevationDegrees.coerceIn(0f, 90f)

private fun polar(
    origin: Offset,
    radius: Float,
    angleDegrees: Float,
): Offset {
    val radians = Math.toRadians(angleDegrees.toDouble())
    return Offset(
        x = origin.x + (radius * cos(radians)).toFloat(),
        y = origin.y + (radius * sin(radians)).toFloat(),
    )
}

private fun DrawScope.drawTick(
    origin: Offset,
    radius: Float,
    elevationDegrees: Float,
    color: Color,
) {
    val angle = elevationAngle(elevationDegrees)
    drawLine(
        color = color,
        start = polar(origin, radius * 0.92f, angle),
        end = polar(origin, radius, angle),
        strokeWidth = 2.dp.toPx(),
    )
}
