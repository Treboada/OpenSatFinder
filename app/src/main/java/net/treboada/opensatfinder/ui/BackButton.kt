/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import net.treboada.opensatfinder.R

/** The arrow's side, the size of a line of the title it sits beside. */
private val BACK_ICON_SIZE = 24.dp

/**
 * The way back to the screen before this one: an arrow, not the word, so the header keeps its
 * room for the title. The word stays as the accessibility label, which is where it belongs.
 */
@Composable
fun BackButton(
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.back)
    IconButton(
        onClick = onClick,
        modifier =
            modifier
                .testTag(tag)
                .semantics { contentDescription = label },
    ) {
        BackIcon(modifier = Modifier.size(BACK_ICON_SIZE))
    }
}

/**
 * A leftwards arrow, drawn rather than imported for the same reason as the gear, the star and
 * the lens: a shaft and a head is all the glyph needs to be.
 */
@Composable
private fun BackIcon(
    modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.1f
        drawLine(
            color = color,
            start = Offset(x = size.width * 0.78f, y = size.height * 0.5f),
            end = Offset(x = size.width * 0.24f, y = size.height * 0.5f),
            strokeWidth = stroke,
            cap = StrokeCap.Round,
        )
        val head =
            Path().apply {
                moveTo(size.width * 0.44f, size.height * 0.26f)
                lineTo(size.width * 0.22f, size.height * 0.5f)
                lineTo(size.width * 0.44f, size.height * 0.74f)
            }
        drawPath(
            path = head,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}
