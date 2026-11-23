/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.compass

import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Subscribes to the compass while the screen is visible and stops on the way out,
 * so a backgrounded app is not holding the sensors while nobody looks at them.
 */
@Composable
fun rememberCompassState(): CompassState {
    val context = LocalContext.current
    val sensorManager = remember(context) { context.getSystemService(SensorManager::class.java) }
    var state by remember(context) { mutableStateOf<CompassState>(CompassState.Waiting) }

    DisposableEffect(context, sensorManager) {
        if (sensorManager == null) {
            state = CompassState.Unavailable
            return@DisposableEffect onDispose {}
        }

        val source =
            CompassSensorSource(
                sensorManager = sensorManager,
                screenRotationDegrees = { context.screenRotationDegrees() },
            )
        var running = false

        fun start() {
            if (running) return
            running =
                source.start { heading, accuracy, tilt ->
                    state = CompassState.Active(heading = heading, accuracy = accuracy, tilt = tilt)
                }
            if (!running) state = CompassState.Unavailable
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

    return state
}
