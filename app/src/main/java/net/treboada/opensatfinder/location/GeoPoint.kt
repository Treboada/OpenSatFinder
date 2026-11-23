/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.location

/** Which half of the world a coordinate falls in, so the screen can name it. */
enum class Hemisphere {
    North,
    South,
    East,
    West,
}

/**
 * A position on the surface of the Earth, in decimal degrees.
 *
 * [of] returns null for values outside the valid ranges instead of clamping them:
 * a fix of 200 degrees is a broken fix, not a position near the pole.
 */
class GeoPoint private constructor(
    val latitudeDegrees: Double,
    val longitudeDegrees: Double,
) {
    val latitudeHemisphere: Hemisphere
        get() = if (latitudeDegrees < 0) Hemisphere.South else Hemisphere.North

    val longitudeHemisphere: Hemisphere
        get() = if (longitudeDegrees < 0) Hemisphere.West else Hemisphere.East

    override fun equals(other: Any?): Boolean =
        other is GeoPoint &&
            other.latitudeDegrees == latitudeDegrees &&
            other.longitudeDegrees == longitudeDegrees

    override fun hashCode(): Int = 31 * latitudeDegrees.hashCode() + longitudeDegrees.hashCode()

    override fun toString(): String = "GeoPoint($latitudeDegrees, $longitudeDegrees)"

    companion object {
        fun of(
            latitudeDegrees: Double,
            longitudeDegrees: Double,
        ): GeoPoint? {
            val latitudeInRange = latitudeDegrees in -90.0..90.0
            val longitudeInRange = longitudeDegrees in -180.0..180.0
            if (!latitudeInRange || !longitudeInRange) return null
            return GeoPoint(latitudeDegrees = latitudeDegrees, longitudeDegrees = longitudeDegrees)
        }
    }
}
