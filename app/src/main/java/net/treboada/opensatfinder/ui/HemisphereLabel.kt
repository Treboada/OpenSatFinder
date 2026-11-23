/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import net.treboada.opensatfinder.location.Hemisphere

/**
 * How a hemisphere is written: `N`, `S`, `E`, `W`, in every language.
 *
 * These are not words to translate but the notation longitudes are quoted in - every
 * satellite catalogue files a slot as `19.2E` - so they stay put whatever the phone is set
 * to. A dial that answers `O` in Spanish while the satellite beside it reads `W` cannot be
 * compared at a glance, which is the whole point of the reading.
 */
fun hemisphereLetter(hemisphere: Hemisphere): String =
    when (hemisphere) {
        Hemisphere.North -> "N"
        Hemisphere.South -> "S"
        Hemisphere.East -> "E"
        Hemisphere.West -> "W"
    }
