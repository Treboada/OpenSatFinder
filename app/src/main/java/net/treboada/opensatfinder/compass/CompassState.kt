/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

/** How much the platform trusts the current heading. */
enum class HeadingAccuracy {
    Unreliable,
    Low,
    Medium,
    High,
    ;

    /** Whether the heading is good enough to trust without asking for calibration. */
    val isReliable: Boolean get() = this == Medium || this == High
}

/** What the compass screen has to show, as seen by the UI. */
sealed interface CompassState {
    /** No sensor combination on this device can find north. */
    data object Unavailable : CompassState

    /** Sensors are registered but the first sample has not arrived yet. */
    data object Waiting : CompassState

    /** A live reading, already relative to the top of the screen. */
    data class Active(
        val heading: Heading,
        val accuracy: HeadingAccuracy,
        val tilt: Tilt,
    ) : CompassState
}
