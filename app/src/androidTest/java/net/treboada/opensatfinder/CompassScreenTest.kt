/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.treboada.opensatfinder.compass.CompassState
import net.treboada.opensatfinder.compass.Heading
import net.treboada.opensatfinder.compass.HeadingAccuracy
import net.treboada.opensatfinder.compass.Tilt
import net.treboada.opensatfinder.location.Hemisphere
import net.treboada.opensatfinder.location.LocationState
import net.treboada.opensatfinder.pointing.GeostationarySatellite
import net.treboada.opensatfinder.pointing.Pointing
import net.treboada.opensatfinder.pointing.SatelliteCatalog
import net.treboada.opensatfinder.ui.CompassScreen
import net.treboada.opensatfinder.ui.formatDecimal
import net.treboada.opensatfinder.ui.hemisphereLetter
import net.treboada.opensatfinder.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class CompassScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * Expectations are read from the resources and formatted in the device's locale,
     * so these tests hold in every language the app ships with. The arithmetic behind
     * the numbers is covered by the JVM tests.
     */
    private fun text(
        @StringRes id: Int,
        vararg args: Any,
    ): String = context.getString(id, *args)

    private fun longitudeText(longitude: Float): String {
        val locale = context.resources.configuration.locales[0]
        val hemisphere = hemisphereLetter(if (longitude < 0f) Hemisphere.West else Hemisphere.East)
        return text(R.string.pointed_longitude, formatDecimal(abs(longitude).toDouble(), locale, 1), hemisphere)
    }

    private val astra =
        SatelliteCatalog.at(19.2).first { it.name.startsWith("ASTRA 1P") }

    private fun pointing(
        degrees: Float = 90f,
        usesTrueNorth: Boolean = true,
        targetAzimuth: Float? = null,
        difference: Float = 0f,
        satelliteElevation: Float = 0f,
        elevationDifference: Float = 0f,
        pointedLongitude: Float? = 19.2f,
    ) = Pointing(
        azimuth = Heading.of(degrees),
        usesTrueNorth = usesTrueNorth,
        pointedLongitudeDegrees = pointedLongitude,
        target =
            targetAzimuth?.let {
                Pointing.Target(
                    satellite = astra,
                    azimuth = Heading.of(it),
                    differenceDegrees = difference,
                    elevationDegrees = satelliteElevation,
                    elevationDifferenceDegrees = elevationDifference,
                )
            },
    )

    private fun show(
        state: CompassState,
        pointing: Pointing? = pointing(),
        satellite: GeostationarySatellite? = astra,
        onChooseSatellite: () -> Unit = {},
        onOpenSettings: () -> Unit = {},
        locationState: LocationState = LocationState.PermissionRequired,
        onRequestLocationPermission: () -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme {
                CompassScreen(
                    state = state,
                    pointing = pointing,
                    satellite = satellite,
                    onChooseSatellite = onChooseSatellite,
                    onOpenSettings = onOpenSettings,
                    locationState = locationState,
                    onRequestLocationPermission = onRequestLocationPermission,
                )
            }
        }
    }

    private fun active(
        degrees: Float = 90f,
        accuracy: HeadingAccuracy = HeadingAccuracy.High,
        pitch: Float = 0f,
        roll: Float = 0f,
    ) = CompassState.Active(
        heading = Heading.of(degrees),
        accuracy = accuracy,
        tilt = Tilt.of(pitchDegrees = pitch, rollDegrees = roll),
    )

    @Test
    fun theMiddleOfTheDialShowsThePointedLongitude() {
        show(active())

        composeRule.onNodeWithTag("compass_dial").assertExists()
        composeRule.onNodeWithTag("pointed_longitude").assertTextEquals(longitudeText(19.2f))
    }

    @Test
    fun theHeaderNamesTheApp() {
        show(active())

        composeRule.onNodeWithTag("app_header").assertExists()
        composeRule.onNodeWithText(text(R.string.app_name)).assertExists()
    }

    @Test
    fun aWesternLongitudeIsWrittenWithTheWesternLetter() {
        show(state = active(), pointing = pointing(pointedLongitude = -9.1f))

        composeRule.onNodeWithTag("pointed_longitude").assertTextEquals(longitudeText(-9.1f))
    }

    @Test
    fun withoutALongitudeTheDialCentreSaysNothingRatherThanZero() {
        show(state = active(), pointing = pointing(pointedLongitude = null))

        composeRule.onNodeWithTag("pointed_longitude").assertTextEquals("—")
    }

    @Test
    fun theSelectorNamesTheSatelliteAndNothingElse() {
        show(
            state = active(),
            pointing = pointing(degrees = 60f, targetAzimuth = 139.2f, difference = 79.2f),
        )

        composeRule.onNodeWithTag("satellite_selector").assertTextEquals(astra.name)
    }

    @Test
    fun underTheSelectorAreTheTargets() {
        val locale = context.resources.configuration.locales[0]
        show(
            state = active(),
            pointing = pointing(targetAzimuth = 139.2f, satelliteElevation = 33f),
        )

        composeRule
            .onNodeWithTag("satellite_targets")
            .assertTextEquals(
                text(
                    R.string.satellite_targets,
                    formatDecimal(astra.longitudeDegrees, locale, 1) +
                        hemisphereLetter(astra.longitudeHemisphere),
                    text(R.string.degrees, formatDecimal(33.0, locale, 0)),
                ),
            )
    }

    @Test
    fun withoutAPositionTheTargetsShowTheSlotAndNoHeight() {
        show(state = active(), pointing = null)

        composeRule
            .onNodeWithTag("satellite_targets")
            .assertTextEquals(
                formatDecimal(
                    astra.longitudeDegrees,
                    context.resources.configuration.locales[0],
                    1,
                ) + hemisphereLetter(astra.longitudeHemisphere),
            )
    }

    @Test
    fun withoutAChosenSatelliteTheSelectorInvitesOne() {
        show(state = active(), pointing = null, satellite = null)

        composeRule
            .onNodeWithTag("satellite_selector")
            .assertTextEquals(text(R.string.satellite_choose))
    }

    @Test
    fun choosingASatelliteIsOneTapAway() {
        var openings = 0
        show(state = active(), onChooseSatellite = { openings++ })

        composeRule.onNodeWithTag("satellite_selector").performClick()

        assertEquals(1, openings)
    }

    @Test
    fun turningClockwiseShowsTheClockwiseArrow() {
        show(
            state = active(),
            pointing = pointing(degrees = 60f, targetAzimuth = 139.2f, difference = 79.4f),
        )

        composeRule.onNodeWithTag("turn_arrow_clockwise").assertExists()
        composeRule.onNodeWithTag("turn_arrow_anticlockwise").assertDoesNotExist()
    }

    @Test
    fun turningAnticlockwiseShowsTheAnticlockwiseArrow() {
        show(
            state = active(),
            pointing = pointing(degrees = 200f, targetAzimuth = 139.2f, difference = -60.8f),
        )

        composeRule.onNodeWithTag("turn_arrow_anticlockwise").assertExists()
        composeRule.onNodeWithTag("turn_arrow_clockwise").assertDoesNotExist()
    }

    @Test
    fun insideTheToleranceNoArrowIsShown() {
        show(
            state = active(),
            pointing = pointing(degrees = 88f, targetAzimuth = 90f, difference = 2f),
        )

        composeRule.onNodeWithTag("turn_arrow_clockwise").assertDoesNotExist()
        composeRule.onNodeWithTag("turn_arrow_anticlockwise").assertDoesNotExist()
    }

    @Test
    fun withoutATargetNoArrowIsShown() {
        show(state = active(), pointing = pointing(degrees = 60f))

        composeRule.onNodeWithTag("turn_arrow_clockwise").assertDoesNotExist()
        composeRule.onNodeWithTag("turn_arrow_anticlockwise").assertDoesNotExist()
    }

    private fun tenths(value: Float): String = formatDecimal(value.toDouble(), context.resources.configuration.locales[0], 1)

    @Test
    fun settingsAreOneTapAway() {
        var openings = 0
        show(state = active(), onOpenSettings = { openings++ })

        composeRule
            .onNodeWithTag("settings_button")
            .assertContentDescriptionEquals(text(R.string.settings))
            .performClick()

        assertEquals(1, openings)
    }

    @Test
    fun theElevationGaugeAppearsWithAHeading() {
        show(state = active())

        composeRule.onNodeWithTag("elevation_gauge").assertExists()
    }

    @Test
    fun withoutAHeadingThereIsNoGauge() {
        show(state = CompassState.Waiting, pointing = null)

        composeRule.onNodeWithTag("elevation_gauge").assertDoesNotExist()
    }

    @Test
    fun theDialSaysItsNumberIsTheAzimuth() {
        show(state = active())

        composeRule.onNodeWithText(text(R.string.label_azimuth)).assertExists()
    }

    @Test
    fun theGaugeSaysItsNumberIsTheElevation() {
        show(state = active(pitch = 12f))

        composeRule.onNodeWithText(text(R.string.label_elevation)).assertExists()
    }

    @Test
    fun anArrowPointsUpWhileTheSatelliteSitsHigher() {
        show(
            state = active(pitch = 10f),
            pointing = pointing(targetAzimuth = 90f, elevationDifference = 20f),
        )

        composeRule.onNodeWithTag("elevation_arrow_up").assertExists()
        composeRule.onNodeWithTag("elevation_arrow_down").assertDoesNotExist()
    }

    @Test
    fun anArrowPointsDownWhileTheSatelliteSitsLower() {
        show(
            state = active(pitch = 60f),
            pointing = pointing(targetAzimuth = 90f, elevationDifference = -20f),
        )

        composeRule.onNodeWithTag("elevation_arrow_down").assertExists()
        composeRule.onNodeWithTag("elevation_arrow_up").assertDoesNotExist()
    }

    @Test
    fun noArrowWhileTheElevationIsAimed() {
        show(
            state = active(pitch = 38f),
            pointing = pointing(targetAzimuth = 90f, elevationDifference = 1f),
        )

        for (tag in listOf(
            "elevation_arrow_up",
            "elevation_arrow_down",
            "elevation_arrow_up_dim",
            "elevation_arrow_down_dim",
        )) {
            composeRule.onNodeWithTag(tag).assertDoesNotExist()
        }
    }

    @Test
    fun theLocationPermissionIsOfferedWhenMissing() {
        var requests = 0
        show(
            state = active(),
            locationState = LocationState.PermissionRequired,
            onRequestLocationPermission = { requests++ },
        )

        composeRule.onNodeWithTag("location_permission_button").performClick()

        assertEquals(1, requests)
    }

    @Test
    fun aDeniedPermissionStillOffersTheWayBack() {
        show(state = active(), locationState = LocationState.PermissionDenied)

        composeRule.onNodeWithTag("location_permission_button").assertExists()
    }

    @Test
    fun onceThePositionIsAllowedTheButtonGoesAway() {
        show(state = active(), locationState = LocationState.Waiting)

        composeRule.onNodeWithTag("location_permission_button").assertDoesNotExist()
    }

    @Test
    fun aPhoneWithTheSystemLocationOffIsTold() {
        show(state = active(), locationState = LocationState.Unavailable)

        composeRule
            .onNodeWithTag("location_note")
            .assertTextEquals(text(R.string.location_off))
    }

    @Test
    fun aGrantedPositionThatHasNotArrivedYetSaysNothing() {
        show(state = active(), locationState = LocationState.Waiting)

        composeRule.onNodeWithTag("location_note").assertDoesNotExist()
    }

    @Test
    fun thePhoneElevationIsShownInDegrees() {
        show(active(pitch = 12f, roll = 0f))

        composeRule
            .onNodeWithTag("elevation_phone")
            .assertTextEquals(text(R.string.degrees, tenths(12f)))
    }

    @Test
    fun aPhonePointingBelowTheHorizonShowsANegativeElevation() {
        show(active(pitch = -4f))

        composeRule
            .onNodeWithTag("elevation_phone")
            .assertTextEquals(text(R.string.degrees, tenths(-4f)))
    }

    @Test
    fun aFlatPhoneReadsZeroElevation() {
        show(active(pitch = 0.4f, roll = -0.3f))

        composeRule
            .onNodeWithTag("elevation_phone")
            .assertTextEquals(text(R.string.degrees, tenths(0.4f)))
    }

    @Test
    fun missingSensorsAreReportedInsteadOfAFakeHeading() {
        show(state = CompassState.Unavailable, pointing = null)

        composeRule.onNodeWithText(text(R.string.compass_unavailable)).assertExists()
        composeRule.onNodeWithTag("pointed_longitude").assertDoesNotExist()
        composeRule.onNodeWithTag("elevation_phone").assertDoesNotExist()
    }

    @Test
    fun waitingForTheFirstSampleIsNotPresentedAsZero() {
        show(state = CompassState.Waiting, pointing = null)

        composeRule.onNodeWithText(text(R.string.compass_waiting)).assertExists()
        composeRule.onNodeWithTag("pointed_longitude").assertDoesNotExist()
        composeRule.onNodeWithTag("elevation_phone").assertDoesNotExist()
    }
}
