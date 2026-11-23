/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.location

/** Which provider a device can use to find a position. */
enum class LocationProvider {
    /** Usually GPS or its GNSS equivalent: slow to first fix, accurate outdoors. */
    Gps,

    /** Wi-Fi and cell based: instant indoors, accurate to a kilometre or so. */
    Network,

    /** Location services are off, or the device has no provider at all. */
    None,
}

fun locationProvider(
    hasGps: Boolean,
    hasNetwork: Boolean,
): LocationProvider =
    when {
        hasGps -> LocationProvider.Gps
        hasNetwork -> LocationProvider.Network
        else -> LocationProvider.None
    }
