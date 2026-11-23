/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.location

/**
 * The position to work with: the new fix when the state carries one, and the last one
 * seen otherwise. Screens that do not listen for positions can still compute look angles
 * from it, at the cost of the numbers ageing while the subscription is stopped.
 */
fun lastKnownPosition(
    previous: GeoPoint?,
    current: LocationState,
): GeoPoint? = (current as? LocationState.Available)?.point ?: previous
