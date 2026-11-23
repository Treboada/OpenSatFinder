/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.treboada.opensatfinder.R
import net.treboada.opensatfinder.pointing.GeostationarySatellite
import net.treboada.opensatfinder.ui.theme.AppTheme

private val ILLUSTRATION_ICON_SIZE = 104.dp

/**
 * The arrow's box as a share of the screen, worked out the way the compass works it out: the
 * dial takes [DIAL_SIZE_FRACTION] of the shorter side, the gauge [GAUGE_FRACTION] of the dial,
 * and the tilt arrow [ELEVATION_ARROW_FRACTION] of the gauge. In portrait the shorter side is
 * the width, so a picture that draws its arrows in that share of its own width gets the arrow
 * the gauge shows - the same head, the same shaft - instead of a smaller copy of it.
 */
private const val ILLUSTRATION_ARROW_FRACTION =
    DIAL_SIZE_FRACTION * GAUGE_FRACTION * ELEVATION_ARROW_FRACTION

/**
 * What the app asks of anyone opening it for the first time: which satellite, and which way
 * to hold the phone. Two steps, because a single wall of text about arrows nobody has seen
 * yet explains nothing.
 */
private enum class OnboardingStep(
    @StringRes val title: Int,
    @StringRes val body: Int,
) {
    Choose(R.string.onboarding_choose_title, R.string.onboarding_choose_body),
    Turn(R.string.onboarding_turn_title, R.string.onboarding_turn_body),
}

/**
 * The welcome itself: picture, title, body and one button that either turns the page or
 * finishes. Back returns to the previous step and, on the first one, does nothing - there is
 * nowhere behind the first launch to go.
 *
 * The first page asks which satellite to start with, offering the [candidates] and holding
 * [selected] as the answer, because the app's own default is one slot on one continent and a
 * reader elsewhere is better served by a list of what they can actually see. With nothing
 * selected the page does not turn: the question is the point of it.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    onExit: (() -> Unit)? = null,
    candidates: List<GeostationarySatellite> = emptyList(),
    selected: GeostationarySatellite? = null,
    onSelect: (GeostationarySatellite) -> Unit = {},
) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    val steps = OnboardingStep.entries
    val step = steps[index]
    val last = index == steps.lastIndex
    // A page that asks a question waits for the answer; the ones that only tell do not.
    val answered = step != OnboardingStep.Choose || candidates.isEmpty() || selected != null
    // On the first launch there is nowhere behind the first page to go; opened from the
    // settings, there is, and both the arrow and the gesture have to lead there.
    BackHandler(enabled = index > 0 || onExit != null) {
        if (index > 0) index-- else onExit?.invoke()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .safeDrawingPadding(),
    ) {
        if (onExit != null) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackButton(onClick = onExit, tag = "onboarding_back")
                Text(
                    text = stringResource(R.string.settings_help),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
        ) {
            StepIllustration(step = step)
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(step.title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("onboarding_title"),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(step.body),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("onboarding_body"),
                )
                if (step == OnboardingStep.Choose && candidates.isNotEmpty()) {
                    Spacer(Modifier.height(20.dp))
                    SatelliteChooser(candidates = candidates, selected = selected, onSelect = onSelect)
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                StepDots(index = index, count = steps.size)
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { if (last) onFinish() else index++ },
                    enabled = answered,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("onboarding_next"),
                ) {
                    Text(stringResource(if (last) R.string.onboarding_start else R.string.onboarding_next))
                }
            }
        }
    }
}

/**
 * The answer to "which satellite": a list that opens, one row per candidate. The exposed
 * dropdown is still an experimental Material API; it is opted into here and nowhere else, so
 * the opt-in is one function to revisit rather than the whole screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SatelliteChooser(
    candidates: List<GeostationarySatellite>,
    selected: GeostationarySatellite?,
    onSelect: (GeostationarySatellite) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name.orEmpty(),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.onboarding_choose_field)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .testTag("onboarding_satellite"),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            candidates.forEach { satellite ->
                DropdownMenuItem(
                    text = { Text(satellite.name) },
                    modifier = Modifier.testTag("onboarding_option"),
                    onClick = {
                        onSelect(satellite)
                        expanded = false
                    },
                )
            }
        }
    }
}

/**
 * The thing the step is about, drawn with the same ink as the screen it describes: the
 * satellite glyph for the chip, the turn arrow and the tilt arrow for the aiming.
 */
@Composable
private fun StepIllustration(step: OnboardingStep) {
    val color = MaterialTheme.colorScheme.primary
    BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        // The arrows take the share of the width the compass gives them; the glyph keeps its
        // own size, being a picture of a button rather than of the gauge.
        val arrow = maxWidth * ILLUSTRATION_ARROW_FRACTION
        when (step) {
            OnboardingStep.Choose ->
                SatelliteIcon(
                    modifier = Modifier.size(ILLUSTRATION_ICON_SIZE),
                    color = color,
                )

            OnboardingStep.Turn ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Canvas(modifier = Modifier.size(arrow)) {
                        drawCurvedArrow(
                            color = color,
                            center = center,
                            radius = size.minDimension * ARROW_RADIUS_FRACTION,
                            startAngle = 190f,
                            sweepAngle = 150f,
                        )
                    }
                    TiltArrow(color = color, up = true, size = arrow)
                }
        }
    }
}

/** Which page this is, as dots: the screen reader gets the same thing in words. */
@Composable
private fun StepDots(
    index: Int,
    count: Int,
) {
    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.outlineVariant
    val description = stringResource(R.string.onboarding_step, index + 1, count)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .semantics { contentDescription = description }
                .testTag("onboarding_step"),
    ) {
        repeat(count) { dot ->
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = if (dot == index) active else inactive)
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Night", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen(onFinish = {})
    }
}
