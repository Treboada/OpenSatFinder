/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.location.GeoPoint

/**
 * The satellites ordered by how high they sit in the sky, highest first, so the ones
 * worth pointing at come before the ones below the horizon. Equal elevations — the
 * usual case for satellites sharing a slot — go by name, and without a position there
 * is nothing to rank by, so the order is alphabetical.
 */
fun orderByElevation(
    satellites: List<GeostationarySatellite>,
    observer: GeoPoint?,
): List<GeostationarySatellite> {
    if (observer == null) return satellites.sortedBy { it.name }
    return satellites
        .map { satellite -> satellite to satelliteLook(observer, satellite).elevationDegrees }
        .sortedWith(
            compareByDescending<Pair<GeostationarySatellite, Float>> { it.second }
                .thenBy { it.first.name },
        ).map { (satellite, _) -> satellite }
}

/**
 * The favourites first, each group keeping the order it arrived in.
 *
 * A hand-picked satellite is what the list is opened for; leaving it among five hundred others
 * is a reason not to mark any. The ranking by elevation still decides inside each group.
 */
fun favouritesFirst(
    satellites: List<GeostationarySatellite>,
    favourites: Set<String>,
): List<GeostationarySatellite> {
    if (favourites.isEmpty()) return satellites
    val (starred, others) = satellites.partition { satellite -> satellite.name in favourites }
    return starred + others
}
