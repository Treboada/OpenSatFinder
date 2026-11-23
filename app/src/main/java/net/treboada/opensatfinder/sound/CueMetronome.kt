/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.sound

import android.os.SystemClock
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.delay

/**
 * Beeps while the instrument is aimed: the bip once a second while the bearing is on the
 * satellite, the bop once a second while the elevation is, each in its own third of the
 * second so the two never collide (see [ToneCycle]).
 *
 * Nothing else about the screen needs watching: the flags are read when a slot comes round,
 * so the rhythm keeps its phase while the aim flickers in and out of the tolerance, and a
 * slot missed is a slot missed rather than a burst of catch-up tones. The tones stop with
 * the activity, because a compass that beeps from a pocket is a nuisance.
 */
@Composable
fun CueMetronome(
    azimuthOnTarget: Boolean,
    elevationOnTarget: Boolean,
    soundLevel: Int = SoundLevel.DEFAULT,
) {
    val bearing: State<Boolean> = rememberUpdatedState(azimuthOnTarget)
    val elevation: State<Boolean> = rememberUpdatedState(elevationOnTarget)
    val gain = SoundLevel.gainOf(soundLevel)
    val context = LocalContext.current
    val player = remember(context) { TonePlayer(context) }
    val onScreen = rememberActivityInFront()

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LaunchedEffect(onScreen, soundLevel, player) {
        if (!onScreen || !SoundLevel.isOn(soundLevel)) return@LaunchedEffect
        val start = SystemClock.elapsedRealtime()
        var lastSlot = -1
        while (true) {
            val elapsed = SystemClock.elapsedRealtime() - start
            val slot = ToneCycle.slot(elapsed)
            if (slot != lastSlot) {
                ToneCycle
                    .cueIn(slot, bearing.value, elevation.value)
                    ?.let { cue -> player.play(cue, gain) }
                lastSlot = slot
            }
            delay(ToneCycle.millisUntilNextSlot(elapsed))
        }
    }
}

/**
 * True while the activity is in front. Compose keeps running effects on a stopped activity,
 * so without this the tones would follow the user out of the app.
 */
@Composable
private fun rememberActivityInFront(): Boolean {
    val lifecycle = (LocalActivity.current as? LifecycleOwner)?.lifecycle ?: return true
    var inFront by remember(lifecycle) {
        mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }
    DisposableEffect(lifecycle) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> inFront = true
                    Lifecycle.Event.ON_PAUSE -> inFront = false
                    else -> Unit
                }
            }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    return inFront
}
