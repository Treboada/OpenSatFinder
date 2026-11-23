/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.location.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class SatelliteOrderTest {
    private val equator = checkNotNull(GeoPoint.of(0.0, 0.0))
    private val lisbon = checkNotNull(GeoPoint.of(38.7223, -9.1393))

    private val astra = GeostationarySatellite(name = "ASTRA 1P (SES-24)", longitudeDegrees = 19.2)
    private val hotbird = GeostationarySatellite(name = "HOTBIRD 13E", longitudeDegrees = 13.0)
    private val far = GeostationarySatellite(name = "FAR WEST", longitudeDegrees = -170.0)
    private val coLocated = GeostationarySatellite(name = "ASTRA 1N", longitudeDegrees = 19.2)

    private val catalogue = listOf(astra, far, hotbird, coLocated)

    @Test
    fun `the highest satellite comes first`() {
        // From the equator HOTBIRD at 13 east is higher than ASTRA at 19.2 east.
        val ordered = orderByElevation(catalogue, equator)

        assertEquals(
            listOf("HOTBIRD 13E", "ASTRA 1N", "ASTRA 1P (SES-24)", "FAR WEST"),
            ordered.map { it.name },
        )
    }

    @Test
    fun `satellites below the horizon come last`() {
        val ordered = orderByElevation(catalogue, lisbon)

        assertEquals("FAR WEST", ordered.last().name)
    }

    @Test
    fun `equal elevations go by name`() {
        // The two ASTRA satellites share 19.2 east and therefore the same elevation.
        val ordered = orderByElevation(listOf(astra, coLocated), equator)

        assertEquals(listOf("ASTRA 1N", "ASTRA 1P (SES-24)"), ordered.map { it.name })
    }

    @Test
    fun `without a position the order is alphabetical`() {
        val ordered = orderByElevation(catalogue, observer = null)

        assertEquals(
            listOf("ASTRA 1N", "ASTRA 1P (SES-24)", "FAR WEST", "HOTBIRD 13E"),
            ordered.map { it.name },
        )
    }

    @Test
    fun `ordering keeps every satellite`() {
        assertEquals(catalogue.toSet(), orderByElevation(catalogue, equator).toSet())
    }

    @Test
    fun `favourites come before the rest`() {
        val favourites = favouritesFirst(catalogue, setOf("FAR WEST"))

        assertEquals("FAR WEST", favourites.first().name)
        assertEquals(catalogue.size, favourites.size)
    }

    @Test
    fun `each group keeps the order it arrived in`() {
        val ordered = orderByElevation(catalogue, equator)

        val favourites = favouritesFirst(ordered, setOf("ASTRA 1N", "FAR WEST"))

        assertEquals(
            listOf("ASTRA 1N", "FAR WEST", "HOTBIRD 13E", "ASTRA 1P (SES-24)"),
            favourites.map { it.name },
        )
    }

    @Test
    fun `nothing starred leaves the list exactly as it was`() {
        assertEquals(catalogue, favouritesFirst(catalogue, emptySet()))
    }

    @Test
    fun `a name no longer in the catalogue simply does not match`() {
        assertEquals(catalogue, favouritesFirst(catalogue, setOf("GONE 1E")))
    }
}
