/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.treboada.opensatfinder.R
import net.treboada.opensatfinder.location.Hemisphere
import net.treboada.opensatfinder.pointing.GeostationarySatellite
import kotlin.math.abs

/** Nominal longitudes are quoted to a tenth of a degree, the way operators do. */
internal const val LONGITUDE_DECIMALS = 1

/** Elevations and the turns towards them keep the bearing's tenth of a degree. */
internal const val ELEVATION_DECIMALS = 1

/**
 * `19.2E`: a slot written as notation - the number and the hemisphere letter, no degree sign
 * and no space. Built here rather than in a resource because it is the same in every language.
 */
@Composable
fun longitudeSlot(longitudeDegrees: Double): String {
    val longitude = longitudeDegrees.toFloat()
    val hemisphere = if (longitude < 0f) Hemisphere.West else Hemisphere.East
    return decimalText(abs(longitude), LONGITUDE_DECIMALS) + hemisphereLetter(hemisphere)
}

/**
 * `19.2E · 33°`: what the aim has to reach - the slot the dial's reading should show and the
 * height the gauge's ring sits at. The height needs a position, so without one the slot stands
 * alone rather than lying.
 */
@Composable
fun satelliteTargets(
    satellite: GeostationarySatellite,
    elevationDegrees: Float?,
): String {
    val slot = longitudeSlot(satellite.longitudeDegrees)
    if (elevationDegrees == null) return slot
    return stringResource(
        R.string.satellite_targets,
        slot,
        stringResource(R.string.degrees, decimalText(elevationDegrees, decimals = 0)),
    )
}
