/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ToneCycleTest {
    @Test
    fun `the second is cut into three equal slots`() {
        assertEquals(0, ToneCycle.slot(0))
        assertEquals(0, ToneCycle.slot(333))
        assertEquals(1, ToneCycle.slot(334))
        assertEquals(1, ToneCycle.slot(666))
        assertEquals(2, ToneCycle.slot(667))
        assertEquals(2, ToneCycle.slot(999))
        assertEquals(0, ToneCycle.slot(1000))
        assertEquals(1, ToneCycle.slot(1334))
    }

    @Test
    fun `the bearing owns the first third and the elevation the second`() {
        assertEquals(AudioCue.Bip, ToneCycle.cueIn(0, azimuthOnTarget = true, elevationOnTarget = true))
        assertEquals(AudioCue.Bop, ToneCycle.cueIn(1, azimuthOnTarget = true, elevationOnTarget = true))
        assertNull(ToneCycle.cueIn(2, azimuthOnTarget = true, elevationOnTarget = true))
    }

    @Test
    fun `a slot stays silent while its axis is off target`() {
        assertNull(ToneCycle.cueIn(0, azimuthOnTarget = false, elevationOnTarget = true))
        assertNull(ToneCycle.cueIn(1, azimuthOnTarget = true, elevationOnTarget = false))
        assertEquals(AudioCue.Bop, ToneCycle.cueIn(1, azimuthOnTarget = false, elevationOnTarget = true))
    }

    @Test
    fun `the sleep lands on the next slot boundary`() {
        assertEquals(334L, ToneCycle.millisUntilNextSlot(0))
        assertEquals(4L, ToneCycle.millisUntilNextSlot(330))
        assertEquals(1L, ToneCycle.millisUntilNextSlot(333))
        assertEquals(333L, ToneCycle.millisUntilNextSlot(334))
        assertEquals(1L, ToneCycle.millisUntilNextSlot(999))
        assertEquals(334L, ToneCycle.millisUntilNextSlot(1000))
    }

    /** However the clock lands, the sleep crosses the boundary the slot change needs. */
    @Test
    fun `the sleep always reaches a different slot`() {
        for (elapsed in 0L..3000L) {
            val landed = (elapsed + ToneCycle.millisUntilNextSlot(elapsed)) % CYCLE_MILLIS
            assertTrue(landed == 0L || ToneCycle.slot(landed) != ToneCycle.slot(elapsed))
        }
    }

    /**
     * The metronome's own loop, run at a millisecond a tick: one bip and one bop per second,
     * a third of a second apart, and nothing in between.
     */
    @Test
    fun `aimed on both axes the two tones alternate a third of a second apart`() {
        val played = mutableListOf<Pair<Long, AudioCue>>()
        var lastSlot = -1
        for (elapsed in 0L..2000L) {
            val slot = ToneCycle.slot(elapsed)
            if (slot != lastSlot) {
                ToneCycle
                    .cueIn(slot, azimuthOnTarget = true, elevationOnTarget = true)
                    ?.let { cue -> played += elapsed to cue }
                lastSlot = slot
            }
        }
        assertEquals(
            listOf(
                0L to AudioCue.Bip,
                334L to AudioCue.Bop,
                1000L to AudioCue.Bip,
                1334L to AudioCue.Bop,
                2000L to AudioCue.Bip,
            ),
            played,
        )
    }

    @Test
    fun `an axis that drops out of target drops only its own tone`() {
        val played = mutableListOf<AudioCue>()
        var lastSlot = -1
        for (elapsed in 0L..1000L) {
            val slot = ToneCycle.slot(elapsed)
            if (slot != lastSlot) {
                ToneCycle
                    .cueIn(slot, azimuthOnTarget = false, elevationOnTarget = true)
                    ?.let { cue -> played += cue }
                lastSlot = slot
            }
        }
        // The first third stays silent even though the elevation is aimed: it owns the other
        // tone, and silence is better than the wrong one.
        assertEquals(listOf(AudioCue.Bop), played)
    }
}
