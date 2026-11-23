/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.location

import android.Manifest
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

private const val TAG = "OpenSatFinder"

/** The position state plus the one action the screen can take about it. */
class LocationController(
    val state: LocationState,
    val requestPermission: () -> Unit,
)

/**
 * Asks for the position permission only when the user presses the button, and
 * listens for positions only while the screen is visible: a compass does not need
 * GPS in the background, and neither does anything else here.
 */
@Composable
fun rememberLocationController(): LocationController {
    val context = LocalContext.current
    val locationManager = remember(context) { context.getSystemService(LocationManager::class.java) }
    val allowedAtStart = remember(context) { LocationSource.hasPermission(context) }
    var permissionGranted by remember(context) { mutableStateOf(allowedAtStart) }
    var state by remember(context) {
        mutableStateOf(if (allowedAtStart) LocationState.Waiting else LocationState.PermissionRequired)
    }
    var accessGranted by remember(context) { mutableIntStateOf(0) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            val granted = grants.values.any { it }
            permissionGranted = granted
            if (granted) {
                state = LocationState.Waiting
                accessGranted++
            } else {
                state = LocationState.PermissionDenied
            }
        }

    DisposableEffect(context, locationManager, permissionGranted, accessGranted) {
        if (locationManager == null) {
            state = LocationState.Unavailable
            return@DisposableEffect onDispose {}
        }
        if (!permissionGranted) {
            return@DisposableEffect onDispose {}
        }

        val source = LocationSource(context, locationManager)
        var running = false

        fun start() {
            if (running) return
            running = true
            val started =
                source.start { point, accuracy, declination ->
                    state = LocationState.Available(point, accuracy, declination)
                }
            when (started) {
                LocationStart.Started -> Unit
                LocationStart.NoPermission -> {
                    running = false
                    Log.w(TAG, "the position permission is not granted, so nothing is asked for")
                    state = LocationState.PermissionRequired
                }

                LocationStart.NoProvider -> {
                    running = false
                    Log.w(TAG, "no location provider is enabled on this phone")
                    state = LocationState.Unavailable
                }
            }
        }

        fun stop() {
            if (!running) return
            source.stop()
            running = false
        }

        val lifecycle = (context as? LifecycleOwner)?.lifecycle
        if (lifecycle == null) {
            start()
            return@DisposableEffect onDispose { stop() }
        }

        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> start()
                    Lifecycle.Event.ON_STOP -> stop()
                    else -> Unit
                }
            }
        lifecycle.addObserver(observer)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) start()

        onDispose {
            lifecycle.removeObserver(observer)
            stop()
        }
    }

    return LocationController(
        state = state,
        requestPermission = {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        },
    )
}
