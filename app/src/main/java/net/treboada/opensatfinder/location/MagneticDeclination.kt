/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.location

import android.hardware.GeomagneticField

/**
 * Magnetic declination at a place and time, in degrees: positive when magnetic north
 * sits east of true north, which is the sign the true-azimuth correction needs.
 *
 * [android.hardware.GeomagneticField] carries the platform's world magnetic model, so
 * this has to live on the Android side and cannot be unit tested on the JVM.
 */
fun magneticDeclinationAt(
    point: GeoPoint,
    altitudeMetres: Float,
    atMillis: Long,
): Float =
    GeomagneticField(
        point.latitudeDegrees.toFloat(),
        point.longitudeDegrees.toFloat(),
        altitudeMetres,
        atMillis,
    ).declination
