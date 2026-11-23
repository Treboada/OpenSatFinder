/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.treboada.opensatfinder.R
import net.treboada.opensatfinder.location.GeoPoint
import net.treboada.opensatfinder.pointing.GeostationarySatellite
import net.treboada.opensatfinder.pointing.SatelliteCatalog
import net.treboada.opensatfinder.pointing.SatelliteLook
import net.treboada.opensatfinder.pointing.favouritesFirst
import net.treboada.opensatfinder.pointing.orderByElevation
import net.treboada.opensatfinder.pointing.satelliteLook
import net.treboada.opensatfinder.pointing.satellitesAbove
import net.treboada.opensatfinder.pointing.searchSatellites
import net.treboada.opensatfinder.ui.theme.AppTheme

/** The star: a button's worth of room, and a mark the size of a line of its text. */
private val STAR_BUTTON_SIZE = 48.dp
private val STAR_SIZE = 22.dp

/** The lens in the search chip, the size of the star it sits opposite to. */
private val SEARCH_ICON_SIZE = 22.dp

/**
 * The catalogue, highest in the sky first and filtered as you type, so one of several
 * hundred satellites can be found by name. Stateless: the selection lives in the caller.
 *
 * The order is the one the screen was opened with, stars included: starring a satellite is
 * written down at once but moves no row until the next visit, so the list never shifts under
 * the finger that is reading it.
 */
@Composable
fun SatellitePickerScreen(
    satellites: List<GeostationarySatellite>,
    selected: GeostationarySatellite?,
    observer: GeoPoint?,
    hideInvisible: Boolean,
    favourites: Set<String>,
    onToggleFavourite: (GeostationarySatellite) -> Unit,
    onSelect: (GeostationarySatellite) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    var query by rememberSaveable { mutableStateOf("") }
    // The name rather than the record, so the dialog survives a recreation.
    var detailed by rememberSaveable { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val candidates =
        remember(satellites, observer, hideInvisible) {
            if (hideInvisible) satellitesAbove(satellites, observer) else satellites
        }
    val ordered = remember(candidates, observer) { orderByElevation(candidates, observer) }
    // The stars this visit was opened with. Starring changes what the next visit shows and not
    // the list under the finger that clicked: a row that jumps away from the hand that just
    // touched it answers a question nobody asked.
    val openedWith = remember { favourites }
    val ranked = remember(ordered, openedWith) { favouritesFirst(ordered, openedWith) }
    val matches = remember(query, ranked) { searchSatellites(query, ranked) }
    val looks =
        remember(matches, observer) {
            if (observer == null) {
                emptyMap()
            } else {
                matches.associateWith { satelliteLook(observer, it) }
            }
        }

    detailed
        ?.let { name -> candidates.firstOrNull { it.name == name } }
        ?.let { satellite ->
            SatelliteDetails(
                satellite = satellite,
                look = looks[satellite],
                isFavourite = satellite.name in favourites,
                onClose = { detailed = null },
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .safeDrawingPadding(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onClick = onBack, tag = "picker_back")
            Text(
                text = stringResource(R.string.satellite_picker_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        // A chip rather than a box: pill shape, tonal fill, no frame - and the ink of the
        // satellite names, so the field reads as the first row of the list it filters.
        TextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text(stringResource(R.string.satellite_search)) },
            leadingIcon = {
                SearchIcon(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(SEARCH_ICON_SIZE),
                )
            },
            singleLine = true,
            shape = CircleShape,
            colors =
                TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("satellite_search"),
        )
        if (observer == null) {
            Text(
                text = stringResource(R.string.satellite_elevation_needs_position),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("elevation_needs_position"),
            )
        }
        if (matches.isEmpty()) {
            Text(
                text = stringResource(R.string.satellite_none_found),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        } else {
            ColumnHeader()
            HorizontalDivider()
            // Weight rather than fillMaxSize: the list takes what the header leaves, instead of
            // asking for the whole column and having its last rows cut off.
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("satellite_list"),
            ) {
                itemsIndexed(matches, key = { _, satellite -> satellite.name }) { index, satellite ->
                    SatelliteRow(
                        satellite = satellite,
                        look = looks[satellite],
                        isSelected = satellite == selected,
                        striped = index % 2 == 1,
                        isFavourite = satellite.name in favourites,
                        onToggleFavourite = onToggleFavourite,
                        onLongPress = { detailed = satellite.name },
                        onSelect = onSelect,
                    )
                }
            }
        }
    }
}

/**
 * Everything the app knows about one satellite: the slot it sits over, where it is from here
 * (when there is a here), and whether it is starred. The catalogue carries no more than that -
 * a name and a nominal longitude - so this is the whole record, not a selection of it.
 */
@Composable
private fun SatelliteDetails(
    satellite: GeostationarySatellite,
    look: SatelliteLook?,
    isFavourite: Boolean,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(satellite.name, modifier = Modifier.testTag("satellite_details_title")) },
        text = {
            Column(modifier = Modifier.testTag("satellite_details")) {
                DetailRow(
                    label = stringResource(R.string.satellite_detail_slot),
                    value = longitudeSlot(satellite.longitudeDegrees),
                )
                DetailRow(
                    label = stringResource(R.string.label_azimuth),
                    value = degreesOrNothing(look?.azimuthDegrees),
                )
                DetailRow(
                    label = stringResource(R.string.label_elevation),
                    value = degreesOrNothing(look?.elevationDegrees),
                )
                DetailRow(
                    label = stringResource(R.string.satellite_detail_favourite),
                    value =
                        stringResource(
                            if (isFavourite) {
                                R.string.satellite_detail_yes
                            } else {
                                R.string.satellite_detail_no
                            },
                        ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onClose, modifier = Modifier.testTag("satellite_details_close")) {
                Text(stringResource(R.string.satellite_detail_close))
            }
        },
    )
}

/** One field of the record: its name on the left, its value on the right. */
@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** `139°`, or a dash when there is no position to compute it from. */
@Composable
private fun degreesOrNothing(degrees: Float?): String =
    if (degrees == null) {
        "—"
    } else {
        stringResource(R.string.degrees, decimalText(degrees, decimals = 0))
    }

/**
 * What the two columns of the list are: the name on the left, and on the right the pair the
 * rows carry - the slot written as azimuth, then the height above the horizon.
 */
@Composable
private fun ColumnHeader() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("satellite_list_header"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.satellite_column_name),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.satellite_column_angles),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        // The star column, kept as wide here as the button is in the rows, so the labels sit
        // over the numbers they name.
        Spacer(Modifier.width(STAR_BUTTON_SIZE))
    }
}

/**
 * One satellite: its name, and what aiming at it means - `19.2E · 33°`, the slot and how high
 * it sits. Every other row is tinted, so a long catalogue reads along a line instead of
 * drifting between them.
 */
@Composable
private fun SatelliteRow(
    satellite: GeostationarySatellite,
    look: SatelliteLook?,
    isSelected: Boolean,
    striped: Boolean,
    isFavourite: Boolean,
    onToggleFavourite: (GeostationarySatellite) -> Unit,
    onLongPress: () -> Unit,
    onSelect: (GeostationarySatellite) -> Unit,
) {
    val elevationDegrees = look?.elevationDegrees
    val stripe = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(if (striped) stripe else Color.Transparent)
                .combinedClickable(
                    onClick = { onSelect(satellite) },
                    onLongClick = onLongPress,
                ).semantics { selected = isSelected }
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = satellite.name,
            style = MaterialTheme.typography.titleMedium,
            modifier =
                Modifier
                    .weight(1f)
                    .testTag("satellite_name"),
        )
        Text(
            text = satelliteTargets(satellite = satellite, elevationDegrees = elevationDegrees),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.testTag("satellite_row_targets"),
        )
        val starLabel =
            stringResource(if (isFavourite) R.string.favourite_remove else R.string.favourite_add)
        IconButton(
            onClick = { onToggleFavourite(satellite) },
            modifier =
                Modifier
                    .size(STAR_BUTTON_SIZE)
                    .testTag("satellite_favourite")
                    .semantics { contentDescription = starLabel },
        ) {
            StarIcon(
                filled = isFavourite,
                tint =
                    if (isFavourite) {
                        // The same ink as the name beside it, whatever the theme calls it.
                        LocalContentColor.current
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                modifier = Modifier.size(STAR_SIZE),
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Night", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SatellitePickerScreenPreview() {
    val astra = SatelliteCatalog.at(19.2).firstOrNull()
    AppTheme {
        SatellitePickerScreen(
            satellites = SatelliteCatalog.all.take(8),
            selected = astra,
            observer = null,
            hideInvisible = false,
            favourites = setOfNotNull(astra?.name),
            onToggleFavourite = {},
            onSelect = {},
            onBack = {},
        )
    }
}
