/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.location

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationProviderTest {
    @Test
    fun `a device with both providers uses the accurate one`() {
        assertEquals(
            LocationProvider.Gps,
            locationProvider(hasGps = true, hasNetwork = true),
        )
    }

    @Test
    fun `without gps the network provider is the fallback`() {
        assertEquals(
            LocationProvider.Network,
            locationProvider(hasGps = false, hasNetwork = true),
        )
    }

    @Test
    fun `no provider means no position`() {
        assertEquals(
            LocationProvider.None,
            locationProvider(hasGps = false, hasNetwork = false),
        )
    }
}
