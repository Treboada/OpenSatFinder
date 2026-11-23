/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

/**
 * Point on a circle of [radius] around [center], at [bearing] degrees clockwise
 * from north (up on the screen).
 *
 * This is the whole orientation of the compass rose: bearing 0 is the top of the
 * dial, 90 the right, 180 the bottom, 270 the left.
 */
fun bearingPoint(
    center: Offset,
    radius: Float,
    bearing: Float,
): Offset {
    val radians = Math.toRadians((bearing - 90f).toDouble())
    return Offset(
        x = center.x + (radius * cos(radians)).toFloat(),
        y = center.y + (radius * sin(radians)).toFloat(),
    )
}
