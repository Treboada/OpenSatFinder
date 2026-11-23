/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.sound

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** The rate the cues are synthesised at; anything the platform accepts will do. */
const val SAMPLE_RATE_HZ = 44100

/** The bip sits high in the range, the bop low enough to be told apart without thinking. */
const val BIP_FREQUENCY_HZ = 1180.0
const val BOP_FREQUENCY_HZ = 740.0

/** Short enough for the third of a second it is played in, long enough to hear. */
const val CUE_MILLIS = 120

/** Over half of full scale: a phone held at arm's length has to be audible outdoors. */
internal const val CUE_AMPLITUDE = 0.6f

/** Ramp in and out, in milliseconds, or the tone starts and ends with a click. */
private const val FADE_MILLIS = 6.0

/** The pitch of a cue, in the one place it is decided. */
fun frequencyOf(cue: AudioCue): Double =
    when (cue) {
        AudioCue.Bip -> BIP_FREQUENCY_HZ
        AudioCue.Bop -> BOP_FREQUENCY_HZ
    }

/** One cue as 16-bit mono PCM, ready to be handed to an audio track. */
fun cueSamples(
    frequencyHz: Double,
    durationMillis: Int = CUE_MILLIS,
    sampleRateHz: Int = SAMPLE_RATE_HZ,
): ShortArray {
    val count = durationMillis * sampleRateHz / 1000
    val fadeSamples = (FADE_MILLIS * sampleRateHz / 1000).toInt()
    val peak = (CUE_AMPLITUDE * Short.MAX_VALUE).toInt()
    return ShortArray(count) { index ->
        val phase = 2.0 * PI * frequencyHz * index / sampleRateHz
        (sin(phase) * envelope(index, count, fadeSamples) * peak).toInt().toShort()
    }
}

/**
 * The mono cue written out across [channels] interleaved channels, so a one-channel tone can
 * be fed to a stereo track - which is the format every phone output opens.
 */
fun channelSamples(
    monoSamples: ShortArray,
    channels: Int,
): ShortArray {
    if (channels <= 1) return monoSamples
    return ShortArray(monoSamples.size * channels) { index ->
        monoSamples[index / channels]
    }
}

/** A raised cosine from silence to full level at both ends of the buffer. */
private fun envelope(
    index: Int,
    count: Int,
    fadeSamples: Int,
): Double {
    if (fadeSamples <= 0) return 1.0
    val fromEdge = minOf(index, count - 1 - index)
    if (fromEdge >= fadeSamples) return 1.0
    return (1.0 - cos(PI * fromEdge / fadeSamples)) / 2.0
}
