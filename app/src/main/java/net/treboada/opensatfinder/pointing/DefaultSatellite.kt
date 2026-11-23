/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

/**
 * The satellite the app starts with: ASTRA 1P at 19.2° east. Null only if a future
 * listing stops carrying it, in which case the screen simply asks for a choice.
 */
val DEFAULT_SATELLITE: GeostationarySatellite? =
    SatelliteCatalog.at(19.2).firstOrNull { it.name.startsWith("ASTRA 1P") }
