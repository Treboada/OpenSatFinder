/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.treboada.opensatfinder.R
import net.treboada.opensatfinder.settings.ThemeMode
import net.treboada.opensatfinder.sound.SoundLevel
import net.treboada.opensatfinder.ui.theme.AppTheme
import kotlin.math.roundToInt

/**
 * The app's settings, in the groups a user would look for them in: what the screen does, what
 * the catalogue shows, and how loud the cues are. Under its own rule the help and the About
 * sit at the bottom, where two rows that only open something are out of the way of the things
 * that change behaviour.
 */
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    keepScreenOn: Boolean,
    onKeepScreenOnChange: (Boolean) -> Unit,
    hideInvisibleSatellites: Boolean,
    onHideInvisibleSatellitesChange: (Boolean) -> Unit,
    soundLevel: Int,
    onSoundLevelChange: (Int) -> Unit,
    onOpenHelp: () -> Unit,
    onOpenAbout: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
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
            BackButton(onClick = onBack, tag = "settings_back")
            Text(
                text = stringResource(R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
        ) {
            SettingSection(
                title = stringResource(R.string.settings_section_screen),
                testTag = "settings_section_screen",
            ) {
                SettingSwitch(
                    title = stringResource(R.string.settings_keep_screen_on),
                    summary = stringResource(R.string.settings_keep_screen_on_summary),
                    checked = keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange,
                    testTag = "settings_keep_screen_on",
                )
            }
            HorizontalDivider()
            SettingSection(
                title = stringResource(R.string.settings_section_appearance),
                testTag = "settings_section_appearance",
            ) {
                ThemeChoice(mode = themeMode, onModeChange = onThemeModeChange)
            }
            HorizontalDivider()
            SettingSection(
                title = stringResource(R.string.settings_section_satellites),
                testTag = "settings_section_satellites",
            ) {
                SettingSwitch(
                    title = stringResource(R.string.settings_hide_invisible),
                    summary = stringResource(R.string.settings_hide_invisible_summary),
                    checked = hideInvisibleSatellites,
                    onCheckedChange = onHideInvisibleSatellitesChange,
                    testTag = "settings_hide_invisible",
                )
            }
            HorizontalDivider()
            SettingSection(
                title = stringResource(R.string.settings_sound),
                testTag = "settings_section_sound",
            ) {
                // The section names the setting, so the row carries only what the cues do.
                SettingSlider(
                    summary = stringResource(R.string.settings_sound_summary),
                    value = soundLevelLabel(soundLevel),
                    level = soundLevel,
                    onLevelChange = onSoundLevelChange,
                    testTag = "settings_sound",
                )
            }
            HorizontalDivider()
            // Both links open something; neither changes a value. The welcome first, because
            // it is the one a reader reaches for while learning the app.
            SettingLink(
                title = stringResource(R.string.settings_help),
                onClick = onOpenHelp,
                testTag = "settings_help",
            )
            SettingLink(
                title = stringResource(R.string.about),
                onClick = onOpenAbout,
                testTag = "settings_about",
            )
        }
    }
}

/** The three answers to "which theme": the phone's, light, dark. */
@Composable
private fun ThemeChoice(
    mode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (choice in ThemeMode.entries) {
            FilterChip(
                selected = mode == choice,
                onClick = { onModeChange(choice) },
                label = { Text(stringResource(choice.label())) },
                modifier = Modifier.testTag(choice.tag()),
            )
        }
    }
}

@StringRes
private fun ThemeMode.label(): Int =
    when (this) {
        ThemeMode.System -> R.string.settings_theme_system
        ThemeMode.Light -> R.string.settings_theme_light
        ThemeMode.Dark -> R.string.settings_theme_dark
    }

private fun ThemeMode.tag(): String = "settings_theme_${name.lowercase()}"

/** A named group of settings: the name above, the rows below it. */
@Composable
private fun SettingSection(
    title: String,
    testTag: String,
    content: @Composable () -> Unit,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .testTag(testTag),
    )
    content()
}

/**
 * How loud the cues are, as a five-step slider: silent, then a quarter at a time. The step it
 * sits on is written beside the summary, so the position reads as a number and not as a guess.
 */
@Composable
private fun SettingSlider(
    summary: String,
    value: String,
    level: Int,
    onLevelChange: (Int) -> Unit,
    testTag: String,
) {
    val title = stringResource(R.string.settings_sound)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            // Air between the explanation and the number, so a wrapped summary does not end
            // against it.
            Spacer(Modifier.width(24.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Slider(
            value = level.toFloat(),
            onValueChange = { moved -> onLevelChange(moved.roundToInt()) },
            valueRange = 0f..(SoundLevel.STEPS - 1).toFloat(),
            steps = SoundLevel.STEPS - 2,
            modifier =
                Modifier
                    .testTag(testTag)
                    .semantics { contentDescription = title },
        )
    }
}

/** The slider's position written out: silent, or the percentage of full volume. */
@Composable
private fun soundLevelLabel(level: Int): String =
    if (SoundLevel.isOn(level)) {
        stringResource(R.string.settings_sound_percent, SoundLevel.percentOf(level))
    } else {
        stringResource(R.string.settings_sound_off)
    }

/** A row that opens another screen rather than changing a value. */
@Composable
private fun SettingLink(
    title: String,
    onClick: () -> Unit,
    testTag: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

/** One setting: a switch whose whole row toggles it and reads out to a screen reader. */
@Composable
private fun SettingSwitch(
    title: String,
    summary: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    role = Role.Switch,
                    onValueChange = onCheckedChange,
                ).padding(horizontal = 16.dp, vertical = 16.dp)
                .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Night", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            themeMode = ThemeMode.System,
            onThemeModeChange = {},
            keepScreenOn = true,
            onKeepScreenOnChange = {},
            hideInvisibleSatellites = true,
            onHideInvisibleSatellitesChange = {},
            soundLevel = SoundLevel.FULL,
            onSoundLevelChange = {},
            onOpenHelp = {},
            onOpenAbout = {},
            onBack = {},
        )
    }
}
