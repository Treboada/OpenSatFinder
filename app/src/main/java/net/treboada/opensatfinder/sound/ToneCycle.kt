/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.sound

/** One round of the metronome: a second, cut into three equal slots. */
const val CYCLE_MILLIS = 1000L

/** The slots in a round: the bearing's, the elevation's, and the silence between rounds. */
const val SLOTS_PER_CYCLE = 3

/**
 * When each tone is allowed to sound.
 *
 * The second is cut into three: the first third belongs to the bearing's bip, the second to
 * the elevation's bop, and the last one is silent. Two tones a third of a second apart do not
 * overlap and each keeps its place in the rhythm, which is what makes it possible to hear
 * which of the two axes is aimed and which is not.
 */
object ToneCycle {
    /** Which third of the round [elapsedMillis] falls in: 0, 1 or 2. */
    fun slot(elapsedMillis: Long): Int {
        val offset = elapsedMillis % CYCLE_MILLIS
        return (offset * SLOTS_PER_CYCLE / CYCLE_MILLIS).toInt().coerceIn(0, SLOTS_PER_CYCLE - 1)
    }

    /**
     * The cue a slot owes: the bip while the bearing is on target, the bop while the
     * elevation is, and nothing at all in the silent third.
     */
    fun cueIn(
        slot: Int,
        azimuthOnTarget: Boolean,
        elevationOnTarget: Boolean,
    ): AudioCue? =
        when (slot) {
            0 -> AudioCue.Bip.takeIf { azimuthOnTarget }
            1 -> AudioCue.Bop.takeIf { elevationOnTarget }
            else -> null
        }

    /**
     * How long until the next slot begins, so the metronome can sleep exactly that much.
     *
     * Rounded up, because [slot] rounds down: a third of 1000 ms is 333.3, so the first slot
     * ends at 334 and the boundary the sleep aims at is the one the slot change will land on.
     */
    fun millisUntilNextSlot(elapsedMillis: Long): Long {
        val offset = elapsedMillis % CYCLE_MILLIS
        val endOfSlot =
            (CYCLE_MILLIS * (slot(elapsedMillis) + 1) + SLOTS_PER_CYCLE - 1) / SLOTS_PER_CYCLE
        return (endOfSlot - offset).coerceAtLeast(1L)
    }
}
