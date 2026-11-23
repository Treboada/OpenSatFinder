/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import net.treboada.opensatfinder.R
import net.treboada.opensatfinder.location.GeoPoint
import net.treboada.opensatfinder.location.LocationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LocationNoteTest {
    private val fix =
        LocationState.Available(
            point = checkNotNull(GeoPoint.of(latitudeDegrees = 43.3623, longitudeDegrees = -8.4115)),
            accuracyMetres = 12f,
            declinationDegrees = -2.4f,
        )

    @Test
    fun `a phone with location turned off is told why the numbers are missing`() {
        assertEquals(
            R.string.location_off,
            locationNote(LocationState.Unavailable, waitingLongEnough = false),
        )
    }

    @Test
    fun `a fix on its way says nothing at first`() {
        assertNull(locationNote(LocationState.Waiting, waitingLongEnough = false))
    }

    @Test
    fun `a fix that takes a while is admitted`() {
        assertEquals(
            R.string.location_waiting,
            locationNote(LocationState.Waiting, waitingLongEnough = true),
        )
    }

    @Test
    fun `a position that arrived says nothing`() {
        assertNull(locationNote(fix, waitingLongEnough = true))
    }

    @Test
    fun `a missing permission is the dialog's business, not the note's`() {
        // The dialog and its button already say it; a second line under the chip would repeat
        // it, and would do so in one language under a dialog in another.
        assertNull(locationNote(LocationState.PermissionRequired, waitingLongEnough = true))
        assertNull(locationNote(LocationState.PermissionDenied, waitingLongEnough = true))
    }
}
