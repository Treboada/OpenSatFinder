/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.pointing

import net.treboada.opensatfinder.compass.CompassState
import net.treboada.opensatfinder.compass.Heading
import net.treboada.opensatfinder.compass.HeadingAccuracy
import net.treboada.opensatfinder.compass.Tilt
import net.treboada.opensatfinder.location.GeoPoint
import net.treboada.opensatfinder.location.LocationState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PointingTest {
    private val tolerance = 0.5f

    private val flat = Tilt.of(pitchDegrees = 0f, rollDegrees = 0f)

    private val astra = GeostationarySatellite(name = "ASTRA 1P (SES-24)", longitudeDegrees = 19.2)

    /** From here, ASTRA 1P is due east: azimuth 90. */
    private val equator: GeoPoint = checkNotNull(GeoPoint.of(0.0, 0.0))

    private fun compass(
        degrees: Float,
        pitch: Float = 0f,
    ): CompassState =
        CompassState.Active(
            heading = Heading.of(degrees),
            accuracy = HeadingAccuracy.High,
            tilt = Tilt.of(pitchDegrees = pitch, rollDegrees = 0f),
        )

    private fun fix(declinationDegrees: Float): LocationState = fixAt(point = equator, declinationDegrees = declinationDegrees)

    private fun fixAt(
        point: GeoPoint,
        declinationDegrees: Float = 0f,
    ): LocationState =
        LocationState.Available(
            point = point,
            accuracyMetres = 5f,
            declinationDegrees = declinationDegrees,
        )

    private fun pointing(
        degrees: Float,
        declinationDegrees: Float,
    ): Pointing =
        checkNotNull(
            pointingTo(
                compass = compass(degrees),
                location = fix(declinationDegrees),
                satellite = astra,
            ),
        )

    @Test
    fun `without a heading there is nothing to point`() {
        assertNull(pointingTo(CompassState.Waiting, fix(0f), astra))
        assertNull(pointingTo(CompassState.Unavailable, LocationState.Unavailable, astra))
    }

    @Test
    fun `the pointed longitude is worked out from the position and the phone's pitch`() {
        // From Lisbon, aimed at ASTRA 1P: the reading is the satellite's longitude.
        val lisbon = checkNotNull(GeoPoint.of(38.7223, -9.1393))
        val look = satelliteLook(lisbon, astra)
        val aimed =
            checkNotNull(
                pointingTo(
                    compass = compass(degrees = look.azimuthDegrees, pitch = look.elevationDegrees),
                    location = fixAt(point = lisbon),
                    satellite = astra,
                ),
            )

        assertEquals(19.2f, checkNotNull(aimed.pointedLongitudeDegrees), 0.3f)
    }

    @Test
    fun `without a position there is no longitude to read`() {
        val pointing = checkNotNull(pointingTo(compass(37f), LocationState.Waiting, astra))

        assertNull(pointing.pointedLongitudeDegrees)
    }

    @Test
    fun `without a chosen satellite there is nothing to point at`() {
        val pointing = checkNotNull(pointingTo(compass(37f), fix(0f), satellite = null))

        assertEquals(37f, pointing.azimuth.degrees, tolerance)
        assertTrue(pointing.usesTrueNorth)
        assertNull(pointing.target)
    }

    @Test
    fun `without a position the azimuth stays magnetic and no target is shown`() {
        val pointing = checkNotNull(pointingTo(compass(37f), LocationState.Waiting, astra))

        assertEquals(37f, pointing.azimuth.degrees, tolerance)
        assertFalse(pointing.usesTrueNorth)
        assertNull(pointing.target)
    }

    @Test
    fun `east declination turns a magnetic bearing into a larger true azimuth`() {
        val pointing = pointing(degrees = 37f, declinationDegrees = 3f)

        assertEquals(40f, pointing.azimuth.degrees, tolerance)
        assertTrue(pointing.usesTrueNorth)
    }

    @Test
    fun `west declination turns it into a smaller true azimuth`() {
        val pointing = pointing(degrees = 37f, declinationDegrees = -3f)

        assertEquals(34f, pointing.azimuth.degrees, tolerance)
    }

    @Test
    fun `the correction wraps across north`() {
        assertEquals(357f, pointing(degrees = 358f, declinationDegrees = -1f).azimuth.degrees, tolerance)
        assertEquals(3f, pointing(degrees = 359f, declinationDegrees = 4f).azimuth.degrees, tolerance)
    }

    @Test
    fun `the target is the satellite azimuth in the same true frame`() {
        val pointing = pointing(degrees = 87f, declinationDegrees = 3f)

        val target = checkNotNull(pointing.target)
        assertEquals(90f, target.azimuth.degrees, tolerance)
    }

    @Test
    fun `turning clockwise towards the target is a positive difference`() {
        val target = checkNotNull(pointing(degrees = 60f, declinationDegrees = 0f).target)

        assertEquals(30f, target.differenceDegrees, tolerance)
        assertFalse(target.isOnTarget)
    }

    @Test
    fun `turning anticlockwise towards the target is a negative difference`() {
        val target = checkNotNull(pointing(degrees = 120f, declinationDegrees = 0f).target)

        assertEquals(-30f, target.differenceDegrees, tolerance)
        assertFalse(target.isOnTarget)
    }

    @Test
    fun `the difference takes the short way round`() {
        // True azimuth 350 against a target at 90: 100 degrees clockwise, not 260.
        val target = checkNotNull(pointing(degrees = 350f, declinationDegrees = 0f).target)

        assertEquals(100f, target.differenceDegrees, tolerance)
    }

    @Test
    fun `a couple of degrees away still counts as on target`() {
        val close = checkNotNull(pointing(degrees = 88f, declinationDegrees = 0f).target)
        val off = checkNotNull(pointing(degrees = 87f, declinationDegrees = 0f).target)

        assertTrue(close.isOnTarget)
        assertFalse(off.isOnTarget)
    }

    @Test
    fun `the elevation gap is the satellite's elevation minus the phone's`() {
        // From the equator ASTRA 1P is 67.5 degrees up; the phone lies flat.
        val target = checkNotNull(pointing(degrees = 0f, declinationDegrees = 0f).target)

        assertEquals(67.5f, target.elevationDegrees, tolerance)
        assertEquals(67.5f, target.elevationDifferenceDegrees, tolerance)
        assertFalse(target.isOnElevation)
    }

    @Test
    fun `a phone already raised towards the satellite needs no change`() {
        val target =
            checkNotNull(
                pointingTo(
                    compass = compass(degrees = 0f, pitch = 66f),
                    location = fix(0f),
                    satellite = astra,
                )?.target,
            )

        assertEquals(1.5f, target.elevationDifferenceDegrees, 0.6f)
        assertTrue(target.isOnElevation)
    }

    @Test
    fun `a phone above the satellite is asked to come down`() {
        val target =
            checkNotNull(
                pointingTo(
                    compass = compass(degrees = 0f, pitch = 75f),
                    location = fix(0f),
                    satellite = astra,
                )?.target,
            )

        assertTrue(target.elevationDifferenceDegrees < 0f)
        assertFalse(target.isOnElevation)
    }

    @Test
    fun `a degree off the bearing is aligned, a degree and a half is only on target`() {
        // From the equator ASTRA 1P is due east, so the difference is the turn.
        val aligned = checkNotNull(pointing(degrees = 89f, declinationDegrees = 0f).target)
        val nearly = checkNotNull(pointing(degrees = 88.5f, declinationDegrees = 0f).target)

        assertTrue(aligned.isAligned)
        assertFalse(nearly.isAligned)
        assertTrue(nearly.isOnTarget)
    }

    @Test
    fun `the aligned window is one degree`() {
        assertEquals(1f, ALIGNED_TOLERANCE_DEGREES, 0.0001f)
    }

    @Test
    fun `the tolerance is the width of the on-target window`() {
        assertEquals(2f, ON_TARGET_TOLERANCE_DEGREES, 0.0001f)
    }
}
