/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.location

/** What the screen knows about the user's position. */
sealed interface LocationState {
    /** The app may not read the position yet: it has never asked. */
    data object PermissionRequired : LocationState

    /** The user said no. The system may not show the dialog again. */
    data object PermissionDenied : LocationState

    /** Location services are off, or the device has no usable provider. */
    data object Unavailable : LocationState

    /** Allowed and listening, but no fix has arrived yet. */
    data object Waiting : LocationState

    /**
     * A position, the radius the platform claims for it in metres, and the magnetic
     * declination there: positive when magnetic north sits east of true north.
     */
    data class Available(
        val point: GeoPoint,
        val accuracyMetres: Float?,
        val declinationDegrees: Float,
    ) : LocationState
}
