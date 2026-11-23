/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.compass.normalizeDegrees
import net.treboada.opensatfinder.location.GeoPoint
import net.treboada.opensatfinder.location.Hemisphere
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Mean Earth radius; the sphere is good to a fraction of a degree at GEO distances. */
internal const val EARTH_RADIUS_KM = 6378.137

/** Geostationary orbit radius: 35 786 km above the equator plus the Earth radius. */
private const val GEOSTATIONARY_RADIUS_KM = 42164.17

/**
 * A satellite that sits still over one point of the equator. The name is a proper
 * noun, not UI copy, so it lives with the satellite rather than in the resources.
 */
data class GeostationarySatellite(
    val name: String,
    val longitudeDegrees: Double,
) {
    /** Which half of the equator the satellite sits over. */
    val longitudeHemisphere: Hemisphere
        get() = if (longitudeDegrees < 0) Hemisphere.West else Hemisphere.East
}

/** Where a satellite sits in the sky of one observer. */
data class SatelliteLook(
    /** Degrees clockwise from true north. */
    val azimuthDegrees: Float,
    /** Degrees above the horizon; negative when the satellite is below it. */
    val elevationDegrees: Float,
)

/**
 * The direction from an observer to a geostationary satellite, as azimuth and
 * elevation.
 *
 * The sight line is taken as a vector from the observer to the satellite and resolved
 * in the observer's horizon frame, which is right in both hemispheres: the
 * `atan(tan(dLon) / sin(lat))` shortcut for azimuth only holds north of the equator
 * and gives the angle from the meridian, and the elevation shortcut
 * `atan((cos R - k) / sin R)` breaks at the sub-satellite point where R, the angular
 * distance to it, is zero. Both are useful as cross-checks, and the tests do exactly
 * that, but neither is the way it is computed here.
 */
fun satelliteLook(
    observer: GeoPoint,
    satellite: GeostationarySatellite,
): SatelliteLook {
    val latitude = Math.toRadians(observer.latitudeDegrees)
    val observerLongitude = Math.toRadians(observer.longitudeDegrees)
    val satelliteLongitude = Math.toRadians(satellite.longitudeDegrees)

    // Earth-centred, earth-fixed coordinates of both ends of the sight line.
    val observerX = EARTH_RADIUS_KM * cos(latitude) * cos(observerLongitude)
    val observerY = EARTH_RADIUS_KM * cos(latitude) * sin(observerLongitude)
    val observerZ = EARTH_RADIUS_KM * sin(latitude)
    val satelliteX = GEOSTATIONARY_RADIUS_KM * cos(satelliteLongitude)
    val satelliteY = GEOSTATIONARY_RADIUS_KM * sin(satelliteLongitude)

    val deltaX = satelliteX - observerX
    val deltaY = satelliteY - observerY
    val deltaZ = -observerZ

    // East, north and up components in the observer's horizon frame.
    val east = -sin(observerLongitude) * deltaX + cos(observerLongitude) * deltaY
    val north =
        -sin(latitude) * cos(observerLongitude) * deltaX -
            sin(latitude) * sin(observerLongitude) * deltaY +
            cos(latitude) * deltaZ
    val up =
        cos(latitude) * cos(observerLongitude) * deltaX +
            cos(latitude) * sin(observerLongitude) * deltaY +
            sin(latitude) * deltaZ
    val distance = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

    return SatelliteLook(
        azimuthDegrees = normalizeDegrees(Math.toDegrees(atan2(east, north)).toFloat()),
        elevationDegrees = Math.toDegrees(asin(up / distance)).toFloat(),
    )
}
