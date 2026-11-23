/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val TAG = "OpenSatFinder"

/** Stereo, because that is what a phone's output is and every device opens it. */
private const val CHANNELS = 2

private const val CHANNEL_MASK = AudioFormat.CHANNEL_OUT_STEREO

/** Used only when the platform will not say what its output runs at. */
private const val FALLBACK_SAMPLE_RATE_HZ = 48000

/**
 * Plays the cues through two streaming audio tracks, one per tone, built the first time
 * each is needed and reused after that.
 *
 * Streaming, not a static buffer: a static buffer is not resampled, so a cue generated at
 * 44.1 kHz comes back uninitialised on a device whose output runs at 48 kHz - silence with
 * nothing to show for it but the failure. A stream is resampled, mixed and outlives the cue.
 * The samples are generated at the output's own rate anyway, and written from a background
 * thread, because filling a track blocks until the audio device has room for the data.
 *
 * The tones are synthesised, so there are no assets and no permission involved. They ride
 * the media stream, the one that stays audible whatever the ringer is doing: a cue that
 * arrives only when the phone is not on silent cannot be trusted while aiming at the sky.
 * A device that refuses a track leaves the instrument visual, and every refusal is logged
 * under the [TAG] tag, because silence is otherwise indistinguishable from a bug.
 */
class TonePlayer(
    private val context: Context? = null,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val tracks = mutableMapOf<AudioCue, AudioTrack?>()

    /** One write at a time: two cues must not interleave inside one track. */
    private val writes = Mutex()

    private val sampleRateHz: Int by lazy(::outputSampleRateHz)
    private val cueData = mutableMapOf<AudioCue, ShortArray>()

    fun play(
        cue: AudioCue,
        gain: Float = 1f,
    ) {
        if (gain <= 0f) return
        val track = tracks[cue] ?: buildTrack(cue)?.also { tracks[cue] = it } ?: return
        val samples = cueData.getOrPut(cue) { samplesOf(cue) }
        // The level rides the track, so a change of setting needs no new samples.
        track.setVolume(gain.coerceIn(0f, 1f))
        scope.launch {
            writes.withLock {
                val written = track.write(samples, 0, samples.size)
                if (written < 0) Log.w(TAG, "cannot feed the $cue track: error $written")
            }
        }
    }

    fun release() {
        scope.cancel()
        tracks.values.forEach { track -> track?.release() }
        tracks.clear()
    }

    private fun samplesOf(cue: AudioCue): ShortArray =
        channelSamples(
            monoSamples = cueSamples(frequencyHz = frequencyOf(cue), sampleRateHz = sampleRateHz),
            channels = CHANNELS,
        )

    private fun buildTrack(cue: AudioCue): AudioTrack? {
        val samples = cueData.getOrPut(cue) { samplesOf(cue) }
        val minimum = AudioTrack.getMinBufferSize(sampleRateHz, CHANNEL_MASK, AudioFormat.ENCODING_PCM_16BIT)
        val bufferBytes = maxOf(if (minimum > 0) minimum else 0, samples.size * Short.SIZE_BYTES)
        val track =
            runCatching {
                AudioTrack
                    .Builder()
                    .setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build(),
                    ).setAudioFormat(
                        AudioFormat
                            .Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRateHz)
                            .setChannelMask(CHANNEL_MASK)
                            .build(),
                    ).setBufferSizeInBytes(bufferBytes)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            }.onFailure { error ->
                Log.w(TAG, "cannot build the $cue track", error)
            }.getOrNull() ?: return null
        if (track.state != AudioTrack.STATE_INITIALIZED) {
            Log.w(
                TAG,
                "the $cue track came back uninitialised at $sampleRateHz Hz, " +
                    "$CHANNELS channels, $bufferBytes bytes",
            )
            track.release()
            return null
        }
        track.play()
        Log.d(
            TAG,
            "the $cue track plays at $sampleRateHz Hz, $CHANNELS channels: " +
                "${samples.size} samples in a $bufferBytes byte buffer",
        )
        return track
    }

    /** The rate the phone's output actually runs at, which is the one that never fails. */
    private fun outputSampleRateHz(): Int {
        val manager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val reported = manager?.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)?.toIntOrNull()
        return if (reported != null && reported > 0) reported else FALLBACK_SAMPLE_RATE_HZ
    }
}
