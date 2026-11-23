/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.settings

/**
 * The satellites starred before the reader stars any: one per neighbourhood, the slots most
 * dishes in the world are pointed at, grouped roughly by where their audience is.
 *
 * Names, like the stored favourites, and they have to be names the catalogue carries. A JVM
 * test walks this list against `SatelliteCatalog`, so a catalogue regeneration that loses one
 * fails the build instead of leaving a star that points at nothing. Where a slot carries
 * several satellites the best known one stands for it - the app's own default, ASTRA 1P, is
 * the one at 19.2 degrees east.
 */
internal val DEFAULT_FAVOURITES: Set<String> =
    setOf(
        // Europe: Astra, Hot Bird, Hispasat.
        "ASTRA 1P (SES-24)", // 19.2E
        "EUTELSAT HOTBIRD 13F", // 13E
        "ASTRA 2F", // 28.2E, the British and Irish neighbourhood
        "HISPASAT 30W-6", // 30W
        // North America: the two Dish slots, Galaxy 19, DirecTV.
        "ECHOSTAR 25", // 110W
        "DIRECTV 8", // 119W
        "GALAXY 19 (G-19)", // 97W, where the free channels are
        "DIRECTV 11", // 99W
        "SES-20", // 103W
        // Latin America: the two Eutelsat beams.
        "EUTELSAT 117 WEST B", // 117W
        "EUTELSAT 65 WEST A", // 65W
        // The Middle East and Africa.
        "NILESAT 301", // 7W
        "BADR-7 (ARABSAT-6B)", // 26E
        "INTELSAT 20 (IS-20)", // 68.5E
        // Asia and the Pacific.
        "TURKSAT 4A", // 42E
        "ASIASAT 5", // 100.5E
        "INTELSAT 19 (IS-19)", // 166E
    )
