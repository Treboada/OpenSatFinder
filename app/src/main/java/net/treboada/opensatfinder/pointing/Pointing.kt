/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.compass.CompassState
import net.treboada.opensatfinder.compass.Heading
import net.treboada.opensatfinder.compass.shortestTurn
import net.treboada.opensatfinder.location.LocationState
import kotlin.math.abs

/** Within two degrees on either axis counts as aimed: no phone compass does better. */
const val ON_TARGET_TOLERANCE_DEGREES = 2f

/** Within one degree the aim is as good as it gets, and the screen says so louder. */
const val ALIGNED_TOLERANCE_DEGREES = 1f

/**
 * What the screen can say about where the phone points: the azimuth to show, whether
 * it is true or still magnetic, and how far it is from the satellite.
 */
data class Pointing(
    val azimuth: Heading,
    /** False while there is no position, and so no declination to apply. */
    val usesTrueNorth: Boolean,
    val target: Target?,
    /** The equatorial longitude the sight line crosses at, when it reaches it. */
    val pointedLongitudeDegrees: Float?,
) {
    /** The satellite, where it sits, and the turn that would point the phone at it. */
    data class Target(
        val satellite: GeostationarySatellite,
        val azimuth: Heading,
        /** Signed turn from where the phone points: positive is clockwise. */
        val differenceDegrees: Float,
        /** How high the satellite sits above the horizon. */
        val elevationDegrees: Float,
        /** Satellite elevation minus the phone's own: positive means raise the phone. */
        val elevationDifferenceDegrees: Float,
    ) {
        val isOnTarget: Boolean get() = abs(differenceDegrees) <= ON_TARGET_TOLERANCE_DEGREES

        /** Dead on the satellite's bearing, within a degree. */
        val isAligned: Boolean get() = abs(differenceDegrees) <= ALIGNED_TOLERANCE_DEGREES

        val isOnElevation: Boolean get() = abs(elevationDifferenceDegrees) <= ON_TARGET_TOLERANCE_DEGREES
    }
}

/**
 * Null when there is no heading yet: nothing to point, nothing to correct.
 *
 * The correction needs a position, because that is what the declination is computed
 * from, so without one the azimuth stays magnetic and no satellite direction is shown.
 * It also needs a chosen satellite: with none there is nothing to compute a direction to.
 */
fun pointingTo(
    compass: CompassState,
    location: LocationState,
    satellite: GeostationarySatellite?,
): Pointing? {
    val active = compass as? CompassState.Active ?: return null
    val fix = location as? LocationState.Available
    val azimuth =
        if (fix == null) {
            active.heading
        } else {
            trueAzimuth(magnetic = active.heading, declinationDegrees = fix.declinationDegrees)
        }
    val target =
        fix?.let { available ->
            val chosen = satellite ?: return@let null
            val look = satelliteLook(observer = available.point, satellite = chosen)
            val targetAzimuth = Heading.of(look.azimuthDegrees)
            Pointing.Target(
                satellite = chosen,
                azimuth = targetAzimuth,
                differenceDegrees = shortestTurn(from = azimuth.degrees, to = targetAzimuth.degrees),
                elevationDegrees = look.elevationDegrees,
                elevationDifferenceDegrees = look.elevationDegrees - active.tilt.pitchDegrees,
            )
        }
    val crossingLongitude =
        fix?.let { available ->
            pointedLongitude(
                observer = available.point,
                azimuth = azimuth,
                elevationDegrees = active.tilt.pitchDegrees,
            )
        }
    return Pointing(
        azimuth = azimuth,
        usesTrueNorth = fix != null,
        target = target,
        pointedLongitudeDegrees = crossingLongitude,
    )
}

/** Magnetic heading corrected to true north; the declination is positive to the east. */
fun trueAzimuth(
    magnetic: Heading,
    declinationDegrees: Float,
): Heading = Heading.of(magnetic.degrees + declinationDegrees)
