/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LastKnownPositionTest {
    private val lisbon = checkNotNull(GeoPoint.of(38.7223, -9.1393))
    private val porto = checkNotNull(GeoPoint.of(41.1579, -8.6291))

    @Test
    fun `a new fix replaces the previous position`() {
        val fix = LocationState.Available(point = porto, accuracyMetres = 5f, declinationDegrees = -2f)

        assertEquals(porto, lastKnownPosition(previous = lisbon, current = fix))
    }

    @Test
    fun `waiting for a fix keeps the previous position`() {
        assertEquals(lisbon, lastKnownPosition(previous = lisbon, current = LocationState.Waiting))
    }

    @Test
    fun `no permission keeps the previous position`() {
        assertEquals(lisbon, lastKnownPosition(previous = lisbon, current = LocationState.PermissionRequired))
        assertEquals(lisbon, lastKnownPosition(previous = lisbon, current = LocationState.PermissionDenied))
        assertEquals(lisbon, lastKnownPosition(previous = lisbon, current = LocationState.Unavailable))
    }

    @Test
    fun `without a previous position and without a fix there is nothing`() {
        assertNull(lastKnownPosition(previous = null, current = LocationState.Waiting))
    }
}
