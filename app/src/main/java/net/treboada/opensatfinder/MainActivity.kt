/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import net.treboada.opensatfinder.compass.rememberCompassState
import net.treboada.opensatfinder.location.GeoPoint
import net.treboada.opensatfinder.location.LocationState
import net.treboada.opensatfinder.location.lastKnownPosition
import net.treboada.opensatfinder.location.rememberLocationController
import net.treboada.opensatfinder.pointing.SatelliteCatalog
import net.treboada.opensatfinder.pointing.pointingTo
import net.treboada.opensatfinder.pointing.welcomeSatellites
import net.treboada.opensatfinder.settings.DEFAULT_FAVOURITES
import net.treboada.opensatfinder.settings.ThemeMode
import net.treboada.opensatfinder.settings.rememberFavouriteSatellites
import net.treboada.opensatfinder.settings.rememberOnboarding
import net.treboada.opensatfinder.settings.rememberSatelliteSelection
import net.treboada.opensatfinder.settings.rememberScreenSettings
import net.treboada.opensatfinder.settings.rememberThemeSettings
import net.treboada.opensatfinder.sound.CueMetronome
import net.treboada.opensatfinder.ui.AboutScreen
import net.treboada.opensatfinder.ui.CompassScreen
import net.treboada.opensatfinder.ui.KeepScreenOnEffect
import net.treboada.opensatfinder.ui.LocationPermissionDialog
import net.treboada.opensatfinder.ui.OnboardingScreen
import net.treboada.opensatfinder.ui.SatellitePickerScreen
import net.treboada.opensatfinder.ui.SettingsScreen
import net.treboada.opensatfinder.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Read above the theme: the scheme is decided by this value, so it cannot come
            // from the screens the theme paints.
            val theme = rememberThemeSettings()
            AppTheme(darkTheme = theme.mode.isDark(isSystemInDarkTheme())) {
                App(themeMode = theme.mode, onThemeModeChange = theme.setMode)
            }
        }
    }
}

/** Which of the app's screens is on show. */
private enum class Screen {
    Compass,
    Satellites,
    Settings,
    Help,
    About,
}

/**
 * Owns the app's settings and the three screens: the compass, the catalogue that
 * replaces it while a satellite is being picked, and the settings.
 */
@Composable
fun App(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selection = rememberSatelliteSelection()
    val favourites = rememberFavouriteSatellites()
    val screenSettings = rememberScreenSettings()
    val onboarding = rememberOnboarding()
    var screen by rememberSaveable { mutableStateOf(Screen.Compass) }

    // Kept in memory only: the catalogue can show look angles without holding the
    // location subscription open on a screen that does not answer the compass.
    var lastPosition by remember { mutableStateOf<GeoPoint?>(null) }

    // What the welcome offers and what was picked there. The pick is kept by name so a
    // rotation does not forget it; a name the offered list no longer carries counts as none.
    var welcomePick by rememberSaveable { mutableStateOf("") }
    val welcomeNames = favourites.names.ifEmpty { DEFAULT_FAVOURITES }
    val welcome =
        remember(welcomeNames, lastPosition) {
            // Stars a catalogue regeneration no longer carries would leave the page with nothing
            // to pick; the defaults stand in, never the whole catalogue.
            welcomeSatellites(
                catalogue = SatelliteCatalog.all,
                names = welcomeNames,
                observer = lastPosition,
            ).ifEmpty {
                welcomeSatellites(
                    catalogue = SatelliteCatalog.all,
                    names = DEFAULT_FAVOURITES,
                    observer = lastPosition,
                )
            }
        }
    val picked = welcome.firstOrNull { it.name == welcomePick }
    val pickSatellite = { satellite: net.treboada.opensatfinder.pointing.GeostationarySatellite ->
        welcomePick = satellite.name
        selection.select(satellite)
    }

    // Only the compass: sighting takes longer than the screen timeout, but the catalogue and
    // the settings are read, not aimed with, and holding the light on for them is a battery
    // bill with nothing to show for it.
    KeepScreenOnEffect(enabled = screenSettings.keepScreenOn && screen == Screen.Compass)

    when (screen) {
        Screen.Satellites -> {
            SatellitePickerScreen(
                satellites = SatelliteCatalog.all,
                selected = selection.satellite,
                observer = lastPosition,
                hideInvisible = screenSettings.hideInvisibleSatellites,
                favourites = favourites.names,
                onToggleFavourite = favourites.toggle,
                onSelect = { satellite ->
                    selection.select(satellite)
                    screen = Screen.Compass
                },
                onBack = { screen = Screen.Compass },
                modifier = modifier,
            )
        }

        Screen.Settings -> {
            SettingsScreen(
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                keepScreenOn = screenSettings.keepScreenOn,
                onKeepScreenOnChange = screenSettings.setKeepScreenOn,
                hideInvisibleSatellites = screenSettings.hideInvisibleSatellites,
                onHideInvisibleSatellitesChange = screenSettings.setHideInvisibleSatellites,
                soundLevel = screenSettings.soundLevel,
                onSoundLevelChange = screenSettings.setSoundLevel,
                onOpenHelp = { screen = Screen.Help },
                onOpenAbout = { screen = Screen.About },
                onBack = { screen = Screen.Compass },
                modifier = modifier,
            )
        }

        Screen.Help -> {
            // The welcome again, as a reference rather than as a gate: it leaves by the arrow
            // to where it was opened from, and "start" lands on the compass it describes. The
            // current satellite is added to the list, or the page would offer a choice that
            // cannot show the one already made.
            val current = selection.satellite
            val offered =
                if (current != null && welcome.none { it.name == current.name }) listOf(current) + welcome else welcome
            OnboardingScreen(
                onFinish = { screen = Screen.Compass },
                onExit = { screen = Screen.Settings },
                modifier = modifier,
                candidates = offered,
                selected = picked ?: current,
                onSelect = pickSatellite,
            )
        }

        Screen.About -> {
            AboutScreen(
                // Back returns to the screen that opened it, which is the settings.
                onBack = { screen = Screen.Settings },
                modifier = modifier,
            )
        }

        Screen.Compass -> {
            val compass = rememberCompassState()
            val location = rememberLocationController()
            // The dialog stands until the position is granted: it is the only way in, and it
            // has no exit of its own.
            if (location.state.needsPermission()) {
                val activity = LocalActivity.current
                LocationPermissionDialog(
                    onRequest = {
                        if (canStillPrompt(activity, location.state)) {
                            location.requestPermission()
                        } else {
                            openAppPermissions(activity)
                        }
                    },
                )
            }
            LaunchedEffect(location.state) {
                lastPosition = lastKnownPosition(previous = lastPosition, current = location.state)
            }
            // The welcome waits for the permission to be settled: explaining arrows to someone
            // the app is holding behind a dialog that cannot be dismissed talks past them.
            if (!onboarding.seen && !location.state.needsPermission()) {
                // Nothing picked yet: the first page is a question, and the app's own default
                // satellite is not an answer to it.
                OnboardingScreen(
                    onFinish = onboarding.markSeen,
                    modifier = modifier,
                    candidates = welcome,
                    selected = picked,
                    onSelect = pickSatellite,
                )
            } else {
                val pointing =
                    pointingTo(compass = compass, location = location.state, satellite = selection.satellite)
                CueMetronome(
                    azimuthOnTarget = pointing?.target?.isOnTarget == true,
                    elevationOnTarget = pointing?.target?.isOnElevation == true,
                    soundLevel = screenSettings.soundLevel,
                )
                CompassScreen(
                    state = compass,
                    pointing = pointing,
                    satellite = selection.satellite,
                    onChooseSatellite = { screen = Screen.Satellites },
                    onOpenSettings = { screen = Screen.Settings },
                    locationState = location.state,
                    onRequestLocationPermission = location.requestPermission,
                    modifier = modifier,
                )
            }
        }
    }
}

/** True while the app may not read the position: never asked, or asked and refused. */
private fun LocationState.needsPermission(): Boolean = this is LocationState.PermissionRequired || this is LocationState.PermissionDenied

/**
 * Whether the platform would still show its own prompt. Once the permission has been
 * refused and the rationale is gone too, the prompt never comes back and only the app's
 * page in the system settings can undo it.
 */
private fun canStillPrompt(
    activity: Activity?,
    state: LocationState,
): Boolean {
    if (state !is LocationState.PermissionDenied) return true
    if (activity == null) return false
    return activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
}

/** The app's page in the system settings, where a refused permission can be granted. */
private fun openAppPermissions(activity: Activity?) {
    if (activity == null) return
    val intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", activity.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { activity.startActivity(intent) }
}
