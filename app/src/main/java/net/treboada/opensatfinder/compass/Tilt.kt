/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import kotlin.math.abs

/**
 * How far the device is from lying flat on the horizontal plane.
 *
 * [pitchDegrees] is how much the top edge is raised: 0 lies flat, +90 points
 * straight at the sky, -90 straight at the ground. [rollDegrees] is how much the
 * right edge is raised, so a phone held level in landscape reads plus or minus 90.
 *
 * Both values are in the frame of the screen, not of the plastic: a phone held in
 * landscape reports the tilt of the edge that looks like the top of the screen.
 */
class Tilt private constructor(
    val pitchDegrees: Float,
    val rollDegrees: Float,
) {
    val isPitchLevel: Boolean get() = abs(pitchDegrees) < LEVEL_TOLERANCE_DEGREES

    val isRollLevel: Boolean get() = abs(rollDegrees) < LEVEL_TOLERANCE_DEGREES

    /** Flat on the table, within the tolerance on both axes. */
    val isLevel: Boolean get() = isPitchLevel && isRollLevel

    override fun equals(other: Any?): Boolean = other is Tilt && other.pitchDegrees == pitchDegrees && other.rollDegrees == rollDegrees

    override fun hashCode(): Int = 31 * pitchDegrees.hashCode() + rollDegrees.hashCode()

    override fun toString(): String = "Tilt(pitch=${pitchDegrees}deg, roll=${rollDegrees}deg)"

    companion object {
        /** Under one degree nothing is visible to the eye, so the device is level. */
        const val LEVEL_TOLERANCE_DEGREES = 1f

        fun of(
            pitchDegrees: Float,
            rollDegrees: Float,
        ): Tilt =
            Tilt(
                pitchDegrees = pitchDegrees.coerceIn(-90f, 90f),
                rollDegrees = rollDegrees.coerceIn(-180f, 180f),
            )
    }
}

/**
 * Low-pass filter for [Tilt]. A tilt does not wrap the way a heading does, so a
 * plain blend of both axes is enough: no shortest-arc arithmetic needed.
 */
class TiltSmoother(
    private val factor: Float = DEFAULT_FACTOR,
) {
    private var current: Tilt? = null

    init {
        require(factor > 0f && factor <= 1f) { "factor must be in (0, 1], got $factor" }
    }

    /** The first sample is taken as-is; later ones move [factor] of the way. */
    fun next(target: Tilt): Tilt {
        val previous = current
        val smoothed =
            if (previous == null) {
                target
            } else {
                Tilt.of(
                    pitchDegrees =
                        previous.pitchDegrees + (target.pitchDegrees - previous.pitchDegrees) * factor,
                    rollDegrees =
                        previous.rollDegrees + (target.rollDegrees - previous.rollDegrees) * factor,
                )
            }
        current = smoothed
        return smoothed
    }

    fun reset() {
        current = null
    }

    private companion object {
        /** The same quarter-step per sample the heading uses. */
        const val DEFAULT_FACTOR = 0.25f
    }
}
