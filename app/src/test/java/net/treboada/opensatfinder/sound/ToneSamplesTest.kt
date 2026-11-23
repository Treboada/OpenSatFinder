/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ToneSamplesTest {
    private val bip: ShortArray = cueSamples(frequencyHz = BIP_FREQUENCY_HZ)
    private val bop: ShortArray = cueSamples(frequencyHz = BOP_FREQUENCY_HZ)

    @Test
    fun `a cue is as long as it was asked for`() {
        assertEquals(CUE_MILLIS * SAMPLE_RATE_HZ / 1000, bip.size)
        assertEquals(
            20 * SAMPLE_RATE_HZ / 1000,
            cueSamples(frequencyHz = BIP_FREQUENCY_HZ, durationMillis = 20).size,
        )
    }

    @Test
    fun `the buffer starts and ends in silence so the tone does not click`() {
        assertEquals(0, bip.first().toInt())
        assertEquals(0, bip.last().toInt())
    }

    @Test
    fun `the tone neither clips nor whispers`() {
        val peak = bip.maxOf { sample -> abs(sample.toInt()) }
        val expected = (CUE_AMPLITUDE * Short.MAX_VALUE).toInt()
        assertEquals(expected.toFloat(), peak.toFloat(), 2f)
    }

    @Test
    fun `the pitch is the frequency it was given`() {
        // Two crossings per cycle, so a tone of f hertz lasting a tenth of a second is 2 * f * 0.1.
        assertEquals(2 * BIP_FREQUENCY_HZ * CUE_MILLIS / 1000, crossings(bip).toDouble(), 4.0)
        assertEquals(2 * BOP_FREQUENCY_HZ * CUE_MILLIS / 1000, crossings(bop).toDouble(), 4.0)
    }

    @Test
    fun `a mono cue fills both channels of a stereo buffer`() {
        val stereo = channelSamples(bip, channels = 2)

        assertEquals(bip.size * 2, stereo.size)
        for (index in bip.indices) {
            assertEquals(bip[index], stereo[index * 2])
            assertEquals(bip[index], stereo[index * 2 + 1])
        }
    }

    @Test
    fun `a one channel buffer is left alone`() {
        assertTrue(channelSamples(bip, channels = 1) === bip)
    }

    @Test
    fun `the tone scales with the rate it is generated at`() {
        val slow = cueSamples(frequencyHz = BIP_FREQUENCY_HZ, sampleRateHz = 48000)

        assertEquals(CUE_MILLIS * 48000 / 1000, slow.size)
        assertEquals(2 * BIP_FREQUENCY_HZ * CUE_MILLIS / 1000, crossings(slow).toDouble(), 4.0)
    }

    @Test
    fun `the two cues are told apart by ear`() {
        assertEquals(BIP_FREQUENCY_HZ, frequencyOf(AudioCue.Bip), 0.0)
        assertEquals(BOP_FREQUENCY_HZ, frequencyOf(AudioCue.Bop), 0.0)
        assertTrue(crossings(bip) > crossings(bop) * 3 / 2)
    }

    private fun crossings(samples: ShortArray): Int =
        (1 until samples.size).count { index ->
            (samples[index - 1] < 0) != (samples[index] < 0)
        }
}
