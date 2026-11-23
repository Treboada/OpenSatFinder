/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.compass.Heading
import net.treboada.opensatfinder.location.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * The equatorial longitude the sight line points at: where a ray leaving the observer
 * along [azimuth] and [elevationDegrees] crosses the plane of the equator.
 *
 * Aimed at a geostationary satellite the ray meets that plane exactly where the
 * satellite hangs, so the reading matches the satellite's nominal longitude and can be
 * compared with it at a glance.
 *
 * Looking high in the sky the ray stops reaching the equator ahead — from Barcelona that
 * happens above about 47 degrees — so when it does not, the meridian the heading alone
 * points at is used instead, and failing that the observer's own meridian. The reading
 * therefore never goes blank, and stays exact whenever the sight line does cross.
 */
fun pointedLongitude(
    observer: GeoPoint,
    azimuth: Heading,
    elevationDegrees: Float,
): Float =
    sightLineCrossing(observer, azimuth, elevationDegrees)
        ?: headingCrossing(observer, azimuth)
        ?: observer.longitudeDegrees.toFloat()

/** Where the ray actually aimed crosses the plane of the equator, if it reaches it ahead. */
private fun sightLineCrossing(
    observer: GeoPoint,
    azimuth: Heading,
    elevationDegrees: Float,
): Float? {
    val latitude = Math.toRadians(observer.latitudeDegrees)
    val longitude = Math.toRadians(observer.longitudeDegrees)
    val bearing = Math.toRadians(azimuth.degrees.toDouble())
    val elevation = Math.toRadians(elevationDegrees.toDouble())

    val sinLatitude = sin(latitude)
    val cosLatitude = cos(latitude)
    val sinLongitude = sin(longitude)
    val cosLongitude = cos(longitude)

    // The sight line as weights on the local east, north and up directions.
    val horizontal = cos(elevation)
    val eastWeight = horizontal * sin(bearing)
    val northWeight = horizontal * cos(bearing)
    val upWeight = sin(elevation)

    // The same direction in earth-centred coordinates, where up is the earth's axis.
    val directionX =
        -eastWeight * sinLongitude - northWeight * sinLatitude * cosLongitude + upWeight * cosLatitude * cosLongitude
    val directionY =
        eastWeight * cosLongitude - northWeight * sinLatitude * sinLongitude + upWeight * cosLatitude * sinLongitude
    val directionZ = northWeight * cosLatitude + upWeight * sinLatitude

    if (directionZ == 0.0) return null
    val travel = -(EARTH_RADIUS_KM * sinLatitude) / directionZ
    if (travel <= 0.0) return null

    val crossingX = EARTH_RADIUS_KM * cosLatitude * cosLongitude + travel * directionX
    val crossingY = EARTH_RADIUS_KM * cosLatitude * sinLongitude + travel * directionY
    return asLongitude(Math.toDegrees(atan2(crossingY, crossingX)).toFloat())
}

/**
 * Where the great circle leaving along [azimuth] crosses the equator: the meridian the
 * heading points at, whether or not the phone is raised. Null only when the heading runs
 * along the equator itself.
 */
private fun headingCrossing(
    observer: GeoPoint,
    azimuth: Heading,
): Float? {
    val latitude = Math.toRadians(observer.latitudeDegrees)
    val longitude = Math.toRadians(observer.longitudeDegrees)
    val bearing = Math.toRadians(azimuth.degrees.toDouble())

    val sinLatitude = sin(latitude)
    val cosLatitude = cos(latitude)
    val sinLongitude = sin(longitude)
    val cosLongitude = cos(longitude)

    // Point on the sphere, and the local north and east directions.
    val pointX = cosLatitude * cosLongitude
    val pointY = cosLatitude * sinLongitude
    val pointZ = sinLatitude
    val northX = -sinLatitude * cosLongitude
    val northY = -sinLatitude * sinLongitude
    val northZ = cosLatitude
    val eastX = -sinLongitude
    val eastY = cosLongitude

    val directionX = (northX * cos(bearing)) + (eastX * sin(bearing))
    val directionY = (northY * cos(bearing)) + (eastY * sin(bearing))
    val directionZ = northZ * cos(bearing)

    // Normal of the great circle's plane, then that plane's cut with the equator.
    val normalX = (pointY * directionZ) - (pointZ * directionY)
    val normalY = (pointZ * directionX) - (pointX * directionZ)
    val crossingX = normalY
    val crossingY = -normalX
    val length = sqrt((crossingX * crossingX) + (crossingY * crossingY))
    if (length < 1.0e-9) return null

    // The crossing ahead of us, not the one behind.
    val ahead = (crossingX * directionX) + (crossingY * directionY) >= 0.0
    val sign = if (ahead) 1.0 else -1.0
    return asLongitude(Math.toDegrees(atan2(sign * crossingY, sign * crossingX)).toFloat())
}

/** Longitudes read in (-180, 180], so the antimeridian is 180 and never -180. */
private fun asLongitude(degrees: Float): Float = if (degrees <= -180f) degrees + 360f else degrees
