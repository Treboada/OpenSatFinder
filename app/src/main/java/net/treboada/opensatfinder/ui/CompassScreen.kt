/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import android.content.res.Configuration
import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import net.treboada.opensatfinder.R
import net.treboada.opensatfinder.compass.CardinalDirection
import net.treboada.opensatfinder.compass.CompassState
import net.treboada.opensatfinder.compass.Heading
import net.treboada.opensatfinder.compass.HeadingAccuracy
import net.treboada.opensatfinder.compass.Tilt
import net.treboada.opensatfinder.compass.bearingPoint
import net.treboada.opensatfinder.location.GeoPoint
import net.treboada.opensatfinder.location.Hemisphere
import net.treboada.opensatfinder.location.LocationState
import net.treboada.opensatfinder.pointing.GeostationarySatellite
import net.treboada.opensatfinder.pointing.ON_TARGET_TOLERANCE_DEGREES
import net.treboada.opensatfinder.pointing.Pointing
import net.treboada.opensatfinder.pointing.SatelliteCatalog
import net.treboada.opensatfinder.ui.theme.AlignedColor
import net.treboada.opensatfinder.ui.theme.AppTheme
import kotlin.math.abs

/**
 * The dial takes four fifths of the smaller screen side, which leaves the tenth of
 * margin on each side that the screen is meant to have.
 */
internal const val DIAL_SIZE_FRACTION = 0.8f
private const val TICKS_PER_TURN = 24

private val ICON_SIZE = 28.dp

/** The header's icon, twice the gear's: the top of the screen says whose screen it is. */
private val HEADER_ICON_SIZE = ICON_SIZE * 2

/** Room kept for the gear at the end of the top bar, so the title cannot run into it. */
private val GEAR_RESERVE = 52.dp
private val CHIP_ICON_SIZE = 22.dp

/** How big the corner arrows are, as a fraction of the dial's side. */
private const val ARROW_FRACTION = 0.16f

/** How big each tilt arrow is, as a fraction of the gauge's side, which they sit beside. */
internal const val ELEVATION_ARROW_FRACTION = 0.4f

/** How far the arrow leans out of the gauge box, as a fraction of the gauge's side. */
private const val ELEVATION_ARROW_SHIFT_FRACTION = 0.26f

/** How far above or below the middle the arrow sits, as a fraction of the gauge's side. */
private const val ELEVATION_ARROW_GAP_FRACTION = 0.3f

/** The elevation gauge under the dial, sized against the dial so the two scale together. */
internal const val GAUGE_FRACTION = 0.5f

/** Room above the dial for the marks that sit on its rim and reach beyond the circle. */
private val DIAL_OVERHANG = TARGET_RING_RADIUS + TARGET_RING_WIDTH

/**
 * How long a fix may take before the screen says it is waiting for one: a cold GPS can take
 * a minute outdoors, but silence for four seconds indoors is the user's cue to look outside,
 * or at the system's location switch.
 */
private const val LOCATION_WAITING_NOTE_MS = 4_000L

/**
 * Stateless compass screen: state comes in, nothing goes out. The rose turns so
 * that its north marker sits on true north; the fixed pointer at the top shows
 * where the device itself is pointing.
 */
@Composable
fun CompassScreen(
    state: CompassState,
    pointing: Pointing?,
    satellite: GeostationarySatellite?,
    onChooseSatellite: () -> Unit,
    onOpenSettings: () -> Unit,
    locationState: LocationState,
    onRequestLocationPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        // Three bands - header, selection, dial - with the room between them shared out
        // evenly, so the selection sits in the middle of the space the other two leave
        // instead of hanging off the header, and the dial keeps a margin under it.
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenHeader(onOpenSettings = onOpenSettings)
        SatelliteBlock(
            satellite = satellite,
            targetElevationDegrees = pointing?.target?.elevationDegrees,
            locationState = locationState,
            onChooseSatellite = onChooseSatellite,
            onRequestLocationPermission = onRequestLocationPermission,
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val dialSize = minOf(maxWidth, maxHeight) * DIAL_SIZE_FRACTION
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // The fixed mark and the moving dot straddle the rim and reach past it, so the
                // dial's box needs that much room above it or the top of the rose is where
                // they get cut. The bubble is what the mark occupies outside the circle.
                Spacer(Modifier.height(DIAL_OVERHANG))
                Box(
                    modifier = Modifier.size(dialSize),
                    contentAlignment = Alignment.Center,
                ) {
                    CompassDial(
                        heading = pointing?.azimuth,
                        targetAzimuth = pointing?.target?.azimuth?.degrees,
                        targetAligned = pointing?.target?.isAligned == true,
                        dimmed = state is CompassState.Unavailable,
                        labels = cardinalLabels(),
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .testTag("compass_dial"),
                    )
                    if (pointing != null) {
                        DialCentre(longitudeDegrees = pointing.pointedLongitudeDegrees)
                    }
                    TurnArrow(differenceDegrees = pointing?.target?.differenceDegrees, size = dialSize * ARROW_FRACTION)
                }
                Spacer(Modifier.height(12.dp))
                ElevationReadout(
                    tilt = (state as? CompassState.Active)?.tilt,
                    target = pointing?.target,
                    gaugeSize = dialSize * GAUGE_FRACTION,
                )
                Spacer(Modifier.height(12.dp))
                DialNotes(state = state)
            }
        }
    }
}

/** The app's icon and name at the start of the top bar, and the gear at the end. */
@Composable
private fun ScreenHeader(onOpenSettings: () -> Unit) {
    val settingsLabel = stringResource(R.string.settings)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        val title = MaterialTheme.typography.titleMedium
        Row(
            modifier =
                Modifier
                    .align(Alignment.CenterStart)
                    // The gear keeps its corner: the title stops before it rather than
                    // sliding under it on a narrow screen.
                    .padding(end = GEAR_RESERVE)
                    .testTag("app_header"),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(size = HEADER_ICON_SIZE)
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = title.copy(fontSize = title.fontSize * 2f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        IconButton(
            onClick = onOpenSettings,
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .testTag("settings_button")
                    .semantics { contentDescription = settingsLabel },
        ) {
            GearIcon(modifier = Modifier.size(ICON_SIZE))
        }
    }
}

/** What is being aimed at: the chip that opens the catalogue, the targets of the satellite
 * it names, the way in when there is no location permission yet, and what is missing when
 * the position is not there for a reason of the phone's rather than the user's. */
@Composable
private fun SatelliteBlock(
    satellite: GeostationarySatellite?,
    targetElevationDegrees: Float?,
    locationState: LocationState,
    onChooseSatellite: () -> Unit,
    onRequestLocationPermission: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SatelliteChip(
            satellite = satellite,
            targetElevationDegrees = targetElevationDegrees,
            onClick = onChooseSatellite,
        )
        if (locationState is LocationState.PermissionRequired || locationState is LocationState.PermissionDenied) {
            TextButton(
                onClick = onRequestLocationPermission,
                modifier = Modifier.testTag("location_permission_button"),
            ) {
                Text(stringResource(R.string.location_allow))
            }
        } else {
            LocationNote(state = locationState)
        }
    }
}

/**
 * Why the elevation and the targets are missing, when the position is not coming: the
 * system's location switch is off, or no fix has arrived in a while. Nothing at all while
 * the fix is on its way, which is the normal half-second after the screen opens.
 */
@Composable
private fun LocationNote(state: LocationState) {
    var waited by remember(state) { mutableStateOf(false) }
    LaunchedEffect(state) {
        if (state is LocationState.Waiting) {
            delay(LOCATION_WAITING_NOTE_MS)
            waited = true
        }
    }
    val note = locationNote(state = state, waitingLongEnough = waited) ?: return
    Text(
        text = stringResource(note),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier
                .padding(top = 4.dp)
                .testTag("location_note"),
    )
}

/**
 * Which note the position owes the user, if any. Separate from the drawing of it so the
 * answer can be checked without a screen: the states that need one are the states in which
 * nothing else on the screen would say why the numbers are absent.
 */
@StringRes
internal fun locationNote(
    state: LocationState,
    waitingLongEnough: Boolean,
): Int? =
    when {
        state is LocationState.Unavailable -> R.string.location_off
        state is LocationState.Waiting && waitingLongEnough -> R.string.location_waiting
        else -> null
    }

/** The current satellite and what aiming at it asks for, as one filled button. */
@Composable
private fun SatelliteChip(
    satellite: GeostationarySatellite?,
    targetElevationDegrees: Float?,
    onClick: () -> Unit,
) {
    if (satellite == null) {
        SatelliteChipButton(label = stringResource(R.string.satellite_choose), targets = null, onClick = onClick)
        return
    }
    SatelliteChipButton(
        label = satellite.name,
        targets = satelliteTargets(satellite = satellite, elevationDegrees = targetElevationDegrees),
        onClick = onClick,
    )
}

/**
 * The chip itself: a filled button carrying the glyph, the name of what it selects, and -
 * under the name, a step down in size - the slot and elevation that name means. The targets
 * live in the button because they are part of choosing it, not a caption hanging off it.
 */
@Composable
private fun SatelliteChipButton(
    label: String,
    targets: String?,
    onClick: () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        // Equal air above and below keeps the chip halfway between the gear and the dial.
        modifier =
            Modifier
                .padding(vertical = 8.dp)
                .testTag("satellite_selector"),
    ) {
        SatelliteIcon(modifier = Modifier.size(CHIP_ICON_SIZE))
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            if (targets != null) {
                Text(
                    text = targets,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("satellite_targets"),
                )
            }
        }
    }
}

/**
 * A curved arrow in the top corner of the dial square, outside the circle: the way to
 * turn, drawn rather than written. The arrow appears on the side it sends you to, and
 * neither arrow shows once the phone is within the on-target window.
 */
@Composable
private fun BoxScope.TurnArrow(
    differenceDegrees: Float?,
    size: Dp,
) {
    if (differenceDegrees == null) return
    if (abs(differenceDegrees) <= ON_TARGET_TOLERANCE_DEGREES) return

    val clockwise = differenceDegrees > 0f
    val arrowColor = MaterialTheme.colorScheme.primary
    Canvas(
        modifier =
            Modifier
                .align(if (clockwise) Alignment.TopEnd else Alignment.TopStart)
                .size(size)
                .testTag(if (clockwise) "turn_arrow_clockwise" else "turn_arrow_anticlockwise"),
    ) {
        drawTurnArrow(color = arrowColor, clockwise = clockwise)
    }
}

private fun DrawScope.drawTurnArrow(
    color: Color,
    clockwise: Boolean,
) {
    val radius = size.minDimension * ARROW_RADIUS_FRACTION
    drawCurvedArrow(
        color = color,
        center = center,
        radius = radius,
        startAngle = if (clockwise) 190f else -10f,
        sweepAngle = if (clockwise) 150f else -150f,
    )
}

/** The letters drawn on the rose, translated once per composition. */
@Composable
private fun cardinalLabels(): Map<CardinalDirection, String> =
    CardinalDirection.entries.associateWith { direction ->
        stringResource(cardinalLabel(direction))
    }

@StringRes
private fun cardinalLabel(direction: CardinalDirection): Int =
    when (direction) {
        CardinalDirection.North -> R.string.cardinal_north
        CardinalDirection.NorthEast -> R.string.cardinal_north_east
        CardinalDirection.East -> R.string.cardinal_east
        CardinalDirection.SouthEast -> R.string.cardinal_south_east
        CardinalDirection.South -> R.string.cardinal_south
        CardinalDirection.SouthWest -> R.string.cardinal_south_west
        CardinalDirection.West -> R.string.cardinal_west
        CardinalDirection.NorthWest -> R.string.cardinal_north_west
    }

@Composable
private fun CompassDial(
    heading: Heading?,
    targetAzimuth: Float?,
    targetAligned: Boolean,
    dimmed: Boolean,
    labels: Map<CardinalDirection, String>,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurface
    val northColor = MaterialTheme.colorScheme.error
    val pointerColor = MaterialTheme.colorScheme.primary
    val targetColor = if (targetAligned) AlignedColor else MaterialTheme.colorScheme.tertiary
    val labelPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER } }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = heading?.dialRotationDegrees ?: 0f
                        alpha = if (dimmed) 0.3f else 1f
                    },
        ) {
            drawCompassRose(
                outlineColor = outlineColor,
                tickColor = tickColor,
                labelColor = labelColor,
                northColor = northColor,
                targetColor = targetColor,
                labelPaint = labelPaint,
                labels = labels,
                targetAzimuth = targetAzimuth,
                targetAligned = targetAligned,
            )
        }

        if (!dimmed) {
            // The fixed mark sits on the rim where the pointer used to be, in the same shape
            // the elevation gauge uses for its own: the moving dot can land inside it.
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.minDimension / 2f
                drawCircle(
                    color = pointerColor,
                    radius = TARGET_RING_RADIUS.toPx(),
                    center = bearingPoint(center = center, radius = radius - 2.dp.toPx(), bearing = 0f),
                    style = Stroke(width = TARGET_RING_WIDTH.toPx()),
                )
            }
        }
    }
}

private fun DrawScope.drawCompassRose(
    outlineColor: Color,
    tickColor: Color,
    labelColor: Color,
    northColor: Color,
    targetColor: Color,
    labelPaint: Paint,
    labels: Map<CardinalDirection, String>,
    targetAzimuth: Float?,
    targetAligned: Boolean,
) {
    val radius = size.minDimension / 2f
    drawCircle(
        color = outlineColor,
        radius = radius - 2.dp.toPx(),
        style = Stroke(width = 2.dp.toPx()),
    )

    for (index in 0 until TICKS_PER_TURN) {
        val bearing = index * (360f / TICKS_PER_TURN)
        val isCardinal = bearing % 90f == 0f
        val isIntercardinal = bearing % 45f == 0f
        val length =
            when {
                isCardinal -> 18.dp.toPx()
                isIntercardinal -> 12.dp.toPx()
                else -> 7.dp.toPx()
            }
        val outer = radius - 6.dp.toPx()
        drawLine(
            color = if (bearing == 0f) northColor else tickColor,
            start = bearingPoint(center = center, radius = outer, bearing = bearing),
            end = bearingPoint(center = center, radius = outer - length, bearing = bearing),
            strokeWidth = if (isCardinal) 3.dp.toPx() else 1.5.dp.toPx(),
        )
    }

    targetAzimuth?.let { bearing ->
        // The satellite's bearing has to be findable across the room, so it is drawn
        // heavier than the ticks and gets a bigger head; aligned, it also turns green.
        val inner = bearingPoint(center = center, radius = radius * 0.45f, bearing = bearing)
        val outer = bearingPoint(center = center, radius = radius - 2.dp.toPx(), bearing = bearing)
        drawLine(
            color = targetColor,
            start = inner,
            end = outer,
            strokeWidth = (if (targetAligned) 8.dp else 6.dp).toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16.dp.toPx(), 10.dp.toPx())),
        )
        drawCircle(
            color = targetColor,
            radius = (if (targetAligned) 12.dp else 9.dp).toPx(),
            center = outer,
        )
    }

    CardinalDirection.entries.forEach { direction ->
        val isCardinal = direction.degrees % 90f == 0f
        labelPaint.apply {
            textSize = if (isCardinal) 20.sp.toPx() else 13.sp.toPx()
            typeface = if (isCardinal) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            color = (if (direction == CardinalDirection.North) northColor else labelColor).toArgb()
        }
        val position = bearingPoint(center = center, radius = radius - 46.dp.toPx(), bearing = direction.degrees)
        drawContext.canvas.nativeCanvas.drawText(
            labels[direction].orEmpty(),
            position.x,
            position.y - (labelPaint.ascent() + labelPaint.descent()) / 2f,
            labelPaint,
        )
    }
}

/**
 * The reading itself, in the middle of the rose: the equatorial longitude the phone is
 * aiming at, so it can be compared with the satellite's own at a glance.
 */
@Composable
private fun DialCentre(longitudeDegrees: Float?) {
    val numberStyle = MaterialTheme.typography.displaySmall
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = pointedLongitudeText(longitudeDegrees),
            style = numberStyle,
            modifier = Modifier.testTag("pointed_longitude"),
        )
        Text(
            text = stringResource(R.string.label_azimuth),
            style = numberStyle.copy(fontSize = numberStyle.fontSize / 2f),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("dial_azimuth_label"),
        )
    }
}

/** `19.2E`, or a dash when the sight line never reaches the equator ahead. */
@Composable
private fun pointedLongitudeText(longitudeDegrees: Float?): String {
    if (longitudeDegrees == null) return "—"
    val hemisphere = if (longitudeDegrees < 0f) Hemisphere.West else Hemisphere.East
    return stringResource(
        R.string.pointed_longitude,
        decimalText(abs(longitudeDegrees), LONGITUDE_DECIMALS),
        hemisphereLetter(hemisphere),
    )
}

/** The sky gauge with its number at the arc's centre, for as long as there is a heading. */
@Composable
private fun ElevationReadout(
    tilt: Tilt?,
    target: Pointing.Target?,
    gaugeSize: Dp,
) {
    if (tilt == null) return
    val numberStyle = MaterialTheme.typography.displaySmall
    Box(modifier = Modifier.size(gaugeSize)) {
        ElevationGauge(
            phoneElevationDegrees = tilt.pitchDegrees,
            satelliteElevationDegrees = target?.elevationDegrees,
            aligned = target?.isOnElevation == true,
            modifier = Modifier.fillMaxSize(),
        )
        // The arc is a quarter of the circle centred on the gauge's bottom-left corner, so the
        // number's own centre sits on that corner - spilling out of the box to the left and
        // below, which is what the corner position costs - and its name hangs under it.
        Layout(
            content = {
                Text(
                    text = stringResource(R.string.degrees, decimalText(tilt.pitchDegrees, ELEVATION_DECIMALS)),
                    style = numberStyle,
                    modifier = Modifier.testTag("elevation_phone"),
                )
                Text(
                    text = stringResource(R.string.label_elevation),
                    style = numberStyle.copy(fontSize = numberStyle.fontSize / 2f),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("elevation_label"),
                )
            },
            modifier = Modifier.fillMaxSize(),
        ) { measurables, constraints ->
            val number = measurables[0].measure(Constraints())
            val label = measurables[1].measure(Constraints())
            layout(constraints.maxWidth, constraints.maxHeight) {
                val corner = constraints.maxHeight
                number.place(x = -number.width / 2, y = corner - number.height / 2)
                label.place(x = -label.width / 2, y = corner + number.height / 2)
            }
        }
        ElevationTiltArrow(
            differenceDegrees = target?.elevationDifferenceDegrees,
            size = gaugeSize * ELEVATION_ARROW_FRACTION,
            gap = gaugeSize * ELEVATION_ARROW_GAP_FRACTION,
            shift = gaugeSize * ELEVATION_ARROW_SHIFT_FRACTION,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

/**
 * Which way to tilt the phone: one upright arrow beside the gauge - up while the satellite
 * sits above the phone by more than the tolerance, down while it sits below it by more than
 * the same - and nothing at all once the two agree. It sits above or below the middle rather
 * than swapping ends in place, so the direction reads before the arrow does.
 */
@Composable
private fun ElevationTiltArrow(
    differenceDegrees: Float?,
    size: Dp,
    gap: Dp,
    shift: Dp,
    modifier: Modifier = Modifier,
) {
    if (differenceDegrees == null) return
    if (abs(differenceDegrees) <= ON_TARGET_TOLERANCE_DEGREES) return
    val raise = differenceDegrees > 0f
    TiltArrow(
        color = MaterialTheme.colorScheme.primary,
        up = raise,
        size = size,
        modifier =
            modifier
                .offset(x = shift, y = if (raise) -gap else gap)
                .testTag(if (raise) "elevation_arrow_up" else "elevation_arrow_down"),
    )
}

/** What is left under the rose when there is no heading to draw: why there is none. */
@Composable
private fun DialNotes(state: CompassState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (state) {
            CompassState.Unavailable -> {
                Text(
                    text = stringResource(R.string.compass_unavailable),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            CompassState.Waiting -> {
                Text(
                    text = stringResource(R.string.compass_waiting),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            is CompassState.Active -> Unit
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Night", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CompassScreenPreview() {
    AppTheme {
        CompassScreen(
            state =
                CompassState.Active(
                    heading = Heading.of(34f),
                    accuracy = HeadingAccuracy.High,
                    tilt = Tilt.of(pitchDegrees = 18f, rollDegrees = -3f),
                ),
            pointing =
                Pointing(
                    azimuth = Heading.of(36.5f),
                    usesTrueNorth = true,
                    pointedLongitudeDegrees = 19.2f,
                    target =
                        Pointing.Target(
                            satellite = checkNotNull(SatelliteCatalog.at(19.2).firstOrNull()),
                            azimuth = Heading.of(139.2f),
                            differenceDegrees = 102.7f,
                            elevationDegrees = 36.4f,
                            elevationDifferenceDegrees = 18.4f,
                        ),
                ),
            satellite = checkNotNull(SatelliteCatalog.at(19.2).firstOrNull()),
            onChooseSatellite = {},
            onOpenSettings = {},
            locationState =
                LocationState.Available(
                    point = checkNotNull(GeoPoint.of(latitudeDegrees = 43.3623, longitudeDegrees = -8.4115)),
                    accuracyMetres = 12f,
                    declinationDegrees = -2.4f,
                ),
            onRequestLocationPermission = {},
        )
    }
}

@Preview(name = "Without sensors", showBackground = true)
@Composable
private fun CompassScreenWithoutSensorsPreview() {
    AppTheme {
        CompassScreen(
            state = CompassState.Unavailable,
            pointing = null,
            satellite = null,
            onChooseSatellite = {},
            onOpenSettings = {},
            locationState = LocationState.PermissionRequired,
            onRequestLocationPermission = {},
        )
    }
}
