/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

/**
 * The satellites whose name contains [query], ignoring case and surrounding spaces.
 * An empty query returns the whole list, in the order it was given.
 */
fun searchSatellites(
    query: String,
    satellites: List<GeostationarySatellite> = SatelliteCatalog.all,
): List<GeostationarySatellite> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return satellites
    return satellites.filter { it.name.contains(trimmed, ignoreCase = true) }
}
