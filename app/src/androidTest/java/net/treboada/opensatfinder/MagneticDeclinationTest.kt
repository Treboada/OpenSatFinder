/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import androidx.test.ext.junit.runners.AndroidJUnit4
import net.treboada.opensatfinder.location.GeoPoint
import net.treboada.opensatfinder.location.magneticDeclinationAt
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The declination comes from the platform's magnetic model, so only a device can say
 * whether it works. The signs are geography, not implementation: Lisbon is west of
 * true north, Vancouver is east of it.
 */
@RunWith(AndroidJUnit4::class)
class MagneticDeclinationTest {
    private val now = 1_700_000_000_000L

    @Test
    fun lisbonReadsWestOfTrueNorth() {
        val declination = magneticDeclinationAt(checkNotNull(GeoPoint.of(38.7223, -9.1393)), 0f, now)

        assertTrue("expected a westerly declination, got $declination", declination < 0f)
        assertTrue("declination out of any plausible range: $declination", declination > -30f)
    }

    @Test
    fun vancouverReadsEastOfTrueNorth() {
        val declination = magneticDeclinationAt(checkNotNull(GeoPoint.of(49.2827, -123.1207)), 0f, now)

        assertTrue("expected an easterly declination, got $declination", declination > 0f)
        assertTrue("declination out of any plausible range: $declination", declination < 30f)
    }
}
