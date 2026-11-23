/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.location.GeoPoint

/**
 * What the welcome offers on its first page: the satellites named, highest first, cut down to
 * the ones above the horizon once there is a position to judge by.
 *
 * Without one nothing can be shown to be visible - the picker says so and hides everything -
 * but a welcome with an empty list would be a dead end, so the whole list stands, ordered by
 * name. The same list stands when the position rules every one of them out: a satellite behind
 * a hill is still a better suggestion than nothing at all.
 */
fun welcomeSatellites(
    catalogue: List<GeostationarySatellite>,
    names: Set<String>,
    observer: GeoPoint?,
): List<GeostationarySatellite> {
    val named = orderByElevation(satellites = catalogue.filter { it.name in names }, observer = observer)
    return satellitesAbove(satellites = named, observer = observer).ifEmpty { named }
}
