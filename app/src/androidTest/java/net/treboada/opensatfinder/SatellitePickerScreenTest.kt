/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.treboada.opensatfinder.location.GeoPoint
import net.treboada.opensatfinder.location.Hemisphere
import net.treboada.opensatfinder.pointing.GeostationarySatellite
import net.treboada.opensatfinder.pointing.SatelliteCatalog
import net.treboada.opensatfinder.ui.SatellitePickerScreen
import net.treboada.opensatfinder.ui.formatDecimal
import net.treboada.opensatfinder.ui.hemisphereLetter
import net.treboada.opensatfinder.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class SatellitePickerScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val astra = SatelliteCatalog.at(19.2).first { it.name.startsWith("ASTRA 1P") }

    private val catalogue =
        listOf(
            GeostationarySatellite(name = "ASTRA 1P (SES-24)", longitudeDegrees = 19.2),
            GeostationarySatellite(name = "HOTBIRD 13E", longitudeDegrees = 13.0),
            GeostationarySatellite(name = "FAR WEST", longitudeDegrees = -170.0),
        )

    private fun text(
        @StringRes id: Int,
        vararg args: Any,
    ): String = context.getString(id, *args)

    private fun show(
        satellites: List<GeostationarySatellite> = catalogue,
        selected: GeostationarySatellite? = null,
        observer: GeoPoint? = checkNotNull(GeoPoint.of(0.0, 0.0)),
        hideInvisible: Boolean = false,
        favourites: Set<String> = emptySet(),
        onToggleFavourite: (GeostationarySatellite) -> Unit = {},
        onSelect: (GeostationarySatellite) -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme {
                SatellitePickerScreen(
                    satellites = satellites,
                    selected = selected,
                    observer = observer,
                    hideInvisible = hideInvisible,
                    favourites = favourites,
                    onToggleFavourite = onToggleFavourite,
                    onSelect = onSelect,
                    onBack = onBack,
                )
            }
        }
    }

    /** `19.2E · 68°`: the slot and the height, in the notation the catalogues use. */
    private fun rowTargets(
        longitudeDegrees: Double,
        elevationDegrees: Double?,
    ): String {
        val locale = context.resources.configuration.locales[0]
        val hemisphere = if (longitudeDegrees < 0) Hemisphere.West else Hemisphere.East
        val slot =
            formatDecimal(abs(longitudeDegrees), locale, 1) + hemisphereLetter(hemisphere)
        if (elevationDegrees == null) return slot
        return text(
            R.string.satellite_targets,
            slot,
            text(R.string.degrees, formatDecimal(elevationDegrees, locale, 0)),
        )
    }

    @Test
    fun theCatalogueIsListedWithItsSlotAndHeight() {
        show()

        composeRule.onNodeWithText("ASTRA 1P (SES-24)").assertExists()
        composeRule.onNodeWithText("HOTBIRD 13E").assertExists()
        composeRule.onNodeWithText(rowTargets(longitudeDegrees = 19.2, elevationDegrees = 67.5)).assertExists()
    }

    @Test
    fun theListExplainsItsColumns() {
        show()

        composeRule.onNodeWithText(text(R.string.satellite_column_name)).assertExists()
        composeRule.onNodeWithText(text(R.string.satellite_column_angles)).assertExists()
        val header = composeRule.onNodeWithTag("satellite_list_header").fetchSemanticsNode().boundsInRoot
        val firstRow = composeRule.onNodeWithText("HOTBIRD 13E").fetchSemanticsNode().boundsInRoot

        assertTrue("the header sat at ${header.top}, the first row at ${firstRow.top}", header.top < firstRow.top)
    }

    @Test
    fun theCurrentChoiceIsMarked() {
        show(selected = astra)

        composeRule.onNodeWithText(astra.name).assertIsSelected()
        composeRule.onNodeWithText("HOTBIRD 13E").assertIsNotSelected()
    }

    @Test
    fun typingFiltersTheList() {
        show()

        composeRule.onNodeWithTag("satellite_search").performTextInput("hotbird")

        composeRule.onNodeWithText("HOTBIRD 13E").assertExists()
        composeRule.onNodeWithText("ASTRA 1P (SES-24)").assertDoesNotExist()
    }

    @Test
    fun aQueryNobodyMatchesSaysSo() {
        show()

        composeRule.onNodeWithTag("satellite_search").performTextInput("zzzz")

        composeRule.onNodeWithText(text(R.string.satellite_none_found)).assertExists()
    }

    @Test
    fun eachSatelliteShowsItsCurrentElevation() {
        // Seen from 0N 0E: ASTRA at 19.2 east is 67.5 degrees up, HOTBIRD at 13 east 74.7.
        show()

        composeRule.onNodeWithText(rowTargets(19.2, 67.5)).assertExists()
        composeRule.onNodeWithText(rowTargets(13.0, 74.7)).assertExists()
    }

    @Test
    fun theHighestSatelliteComesFirst() {
        show()

        composeRule
            .onAllNodesWithTag("satellite_row_targets")[0]
            .assertTextEquals(rowTargets(13.0, 74.7))
    }

    @Test
    fun withoutAPositionTheRowsShowTheSlotAndNoHeight() {
        show(observer = null)

        composeRule.onNodeWithTag("elevation_needs_position").assertExists()
        composeRule
            .onAllNodesWithTag("satellite_row_targets")[0]
            .assertTextEquals(rowTargets(19.2, null))
    }

    @Test
    fun hidingTheInvisibleLeavesOnlyWhatIsUpThere() {
        show(hideInvisible = true)

        composeRule.onNodeWithText("ASTRA 1P (SES-24)").assertExists()
        composeRule.onNodeWithText("HOTBIRD 13E").assertExists()
        composeRule.onNodeWithText("FAR WEST").assertDoesNotExist()
    }

    @Test
    fun hidingTheInvisibleWithoutAPositionLeavesNothingToBotherWith() {
        show(observer = null, hideInvisible = true)

        composeRule.onNodeWithText(text(R.string.satellite_none_found)).assertExists()
        composeRule.onNodeWithTag("elevation_needs_position").assertExists()
    }

    @Test
    fun everyRowCarriesAStar() {
        show()

        composeRule.onAllNodesWithTag("satellite_favourite").assertCountEquals(3)
    }

    @Test
    fun theStarMarksTheRowItBelongsTo() {
        var toggled: GeostationarySatellite? = null
        show(onToggleFavourite = { toggled = it })

        // Highest first: the star of the first row belongs to HOTBIRD.
        composeRule.onAllNodesWithTag("satellite_favourite")[0].performClick()

        assertEquals("HOTBIRD 13E", toggled?.name)
    }

    @Test
    fun theStarredSatellitesComeFirst() {
        show(favourites = setOf("FAR WEST"))

        composeRule.onAllNodesWithTag("satellite_name")[0].assertTextEquals("FAR WEST")
    }

    @Test
    fun starringARowLeavesTheOrderForTheNextVisit() {
        val starred = mutableStateOf(emptySet<String>())
        composeRule.setContent {
            AppTheme {
                SatellitePickerScreen(
                    satellites = catalogue,
                    selected = null,
                    observer = GeoPoint.of(0.0, 0.0),
                    hideInvisible = false,
                    favourites = starred.value,
                    onToggleFavourite = { satellite ->
                        starred.value =
                            if (satellite.name in starred.value) {
                                starred.value - satellite.name
                            } else {
                                starred.value + satellite.name
                            }
                    },
                    onSelect = {},
                    onBack = {},
                )
            }
        }

        // The last row gains a star: the star fills, and the row keeps its place until the
        // list is opened again.
        composeRule.onAllNodesWithTag("satellite_favourite")[2].performClick()

        assertEquals(setOf("FAR WEST"), starred.value)
        composeRule.onAllNodesWithTag("satellite_name")[2].assertTextEquals("FAR WEST")
    }

    @Test
    fun aLongPressOpensTheSatelliteItself() {
        show()

        // Highest first: the first row is HOTBIRD.
        composeRule.onAllNodesWithTag("satellite_name")[0].performTouchInput { longClick() }

        composeRule.onNodeWithTag("satellite_details_title").assertTextEquals("HOTBIRD 13E")
    }

    @Test
    fun theDetailsNameEveryFieldTheyHold() {
        show(favourites = setOf("HOTBIRD 13E"))

        composeRule.onAllNodesWithTag("satellite_name")[0].performTouchInput { longClick() }

        composeRule.onNodeWithText(text(R.string.satellite_detail_slot)).assertExists()
        composeRule.onNodeWithText(text(R.string.label_azimuth)).assertExists()
        composeRule.onNodeWithText(text(R.string.label_elevation)).assertExists()
        composeRule.onNodeWithText(text(R.string.satellite_detail_favourite)).assertExists()
        composeRule.onNodeWithText(text(R.string.satellite_detail_yes)).assertExists()
    }

    @Test
    fun theDetailsCloseButtonClosesThem() {
        show()

        composeRule.onAllNodesWithTag("satellite_name")[0].performTouchInput { longClick() }
        composeRule.onNodeWithTag("satellite_details_close").performClick()

        composeRule.onNodeWithTag("satellite_details").assertDoesNotExist()
    }

    @Test
    fun theListOpensAtTheTopWhateverIsChosen() {
        // Forty names, so the chosen one starts well below the fold: it can only be on screen
        // if the list put it there, and the first row can only be there if it did not.
        val many =
            (1..40).map { index ->
                GeostationarySatellite(
                    name = "SAT %02d".format(index),
                    longitudeDegrees = index.toDouble(),
                )
            }

        show(satellites = many, selected = many.last(), observer = null)

        composeRule.onNodeWithText(many.first().name).assertExists()
        composeRule.onNodeWithText(many.last().name).assertDoesNotExist()
    }

    @Test
    fun tappingARowReportsTheSatellite() {
        var picked: GeostationarySatellite? = null
        show(onSelect = { picked = it })

        composeRule.onNodeWithText("HOTBIRD 13E").performClick()

        assertEquals("HOTBIRD 13E", picked?.name)
    }

    @Test
    fun theBackButtonLeavesThePicker() {
        var backs = 0
        show(onBack = { backs++ })

        composeRule.onNodeWithTag("picker_back").performClick()

        assertEquals(1, backs)
    }
}
