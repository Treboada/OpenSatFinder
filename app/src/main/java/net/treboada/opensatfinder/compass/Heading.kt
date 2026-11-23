/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import kotlin.math.pow
import kotlin.math.roundToLong

/** One full turn in degrees. Everything heading-related is expressed against it. */
const val FULL_TURN_DEGREES = 360f

/** Maps any angle onto `[0, 360)`, so callers never have to think about wrap-around. */
fun normalizeDegrees(degrees: Float): Float {
    val wrapped = degrees % FULL_TURN_DEGREES
    return if (wrapped < 0f) wrapped + FULL_TURN_DEGREES else wrapped
}

/**
 * The eight points of the compass, each owning the 45 degree sector centred on its
 * bearing. Sectors are half-open, so 22.5 degrees already reads as north-east. The
 * letters themselves are translated, so they live in the string resources.
 */
enum class CardinalDirection(
    val degrees: Float,
) {
    North(0f),
    NorthEast(45f),
    East(90f),
    SouthEast(135f),
    South(180f),
    SouthWest(225f),
    West(270f),
    NorthWest(315f),
    ;

    companion object {
        /** The point of the compass a bearing falls in. */
        fun of(degrees: Float): CardinalDirection {
            val sector = ((normalizeDegrees(degrees) + 45f / 2f) / 45f).toInt() % entries.size
            return entries[sector]
        }
    }
}

/**
 * A direction the device is pointing at, in degrees clockwise from north.
 *
 * The factory normalises, so a [Heading] always carries a value in `[0, 360)`
 * and equal bearings compare equal no matter how they were computed.
 */
class Heading private constructor(
    val degrees: Float,
) {
    val cardinal: CardinalDirection get() = CardinalDirection.of(degrees)

    /** Rotation of a compass rose that puts north at [degrees] left of the screen top. */
    val dialRotationDegrees: Float get() = -degrees

    /**
     * The bearing rounded to [decimals] places, ready to be shown: a full turn folds
     * back to zero, so 359.97 reads 0.0 and never 360.0.
     */
    fun roundedDegrees(decimals: Int): Float {
        require(decimals >= 0) { "decimals must not be negative, got $decimals" }
        val scale = 10.0.pow(decimals)
        val rounded = (degrees.toDouble() * scale).roundToLong() / scale
        return if (rounded >= FULL_TURN_DEGREES) 0f else rounded.toFloat()
    }

    override fun equals(other: Any?): Boolean = other is Heading && other.degrees == degrees

    override fun hashCode(): Int = degrees.hashCode()

    override fun toString(): String = "Heading(${degrees}deg)"

    companion object {
        fun of(degrees: Float): Heading = Heading(normalizeDegrees(degrees))
    }
}
