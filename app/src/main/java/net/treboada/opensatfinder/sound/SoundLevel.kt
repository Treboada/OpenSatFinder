/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.sound

/**
 * The five positions of the sound slider: silent, and then a quarter at a time.
 *
 * The level scales the cue's own amplitude rather than the phone's volume, so the setting
 * says how loud the instrument is and not how loud the phone is - which the user already
 * controls with the volume keys.
 */
object SoundLevel {
    /** Silent: no cue is played at all. */
    const val OFF = 0

    /** As loud as the instrument goes. */
    const val FULL = 4

    /** An instrument that cannot be heard is a toy, so a fresh install starts at full. */
    const val DEFAULT = FULL

    /** How many positions the slider offers. */
    const val STEPS = FULL + 1

    /** The level as a fraction of the cue's amplitude; anything off the scale is clamped. */
    fun gainOf(level: Int): Float = level.coerceIn(OFF, FULL) / FULL.toFloat()

    /** The level as the percentage the settings show, so the top position reads as 100. */
    fun percentOf(level: Int): Int = level.coerceIn(OFF, FULL) * 100 / FULL

    fun isOn(level: Int): Boolean = level.coerceIn(OFF, FULL) > OFF
}
