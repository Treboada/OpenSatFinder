/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.sound

/** The two tones the instrument plays: one per axis, so the ear can tell them apart. */
enum class AudioCue {
    /** The bearing is on the satellite: the higher tone. */
    Bip,

    /** The elevation is on the satellite: the lower one. */
    Bop,
}
