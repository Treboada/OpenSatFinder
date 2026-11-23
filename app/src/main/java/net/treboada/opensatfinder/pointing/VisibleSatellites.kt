/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.location.GeoPoint

/**
 * Above this a satellite clears the horizon enough to be worth pointing at. Five
 * degrees is roughly a hand's width above the skyline, well clear of buildings and trees.
 */
const val VISIBLE_ELEVATION_DEGREES = 5f

/**
 * The satellites more than [minimumElevationDegrees] above the horizon, in the order
 * they were given. Without a position nothing can be shown to be visible, so the answer
 * is nothing: guessing would put satellites on the list that may be behind a hill.
 */
fun satellitesAbove(
    satellites: List<GeostationarySatellite>,
    observer: GeoPoint?,
    minimumElevationDegrees: Float = VISIBLE_ELEVATION_DEGREES,
): List<GeostationarySatellite> {
    if (observer == null) return emptyList()
    return satellites.filter { satelliteLook(observer, it).elevationDegrees > minimumElevationDegrees }
}
