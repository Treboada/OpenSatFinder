/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.sound

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundLevelTest {
    @Test
    fun `the slider offers five positions`() {
        assertEquals(5, SoundLevel.STEPS)
        assertEquals(0, SoundLevel.OFF)
        assertEquals(4, SoundLevel.FULL)
    }

    @Test
    fun `the level scales the cue a quarter at a time`() {
        assertEquals(0f, SoundLevel.gainOf(0), 0f)
        assertEquals(0.25f, SoundLevel.gainOf(1), 0f)
        assertEquals(0.5f, SoundLevel.gainOf(2), 0f)
        assertEquals(0.75f, SoundLevel.gainOf(3), 0f)
        assertEquals(1f, SoundLevel.gainOf(4), 0f)
    }

    @Test
    fun `the settings read the level as a percentage`() {
        assertEquals(0, SoundLevel.percentOf(0))
        assertEquals(25, SoundLevel.percentOf(1))
        assertEquals(50, SoundLevel.percentOf(2))
        assertEquals(75, SoundLevel.percentOf(3))
        assertEquals(100, SoundLevel.percentOf(4))
    }

    @Test
    fun `a level off the scale is clamped instead of trusted`() {
        assertEquals(0f, SoundLevel.gainOf(-3), 0f)
        assertEquals(1f, SoundLevel.gainOf(9), 0f)
        assertEquals(0, SoundLevel.percentOf(-1))
        assertEquals(100, SoundLevel.percentOf(12))
    }

    @Test
    fun `only the bottom position is silent`() {
        assertFalse(SoundLevel.isOn(0))
        for (level in 1..SoundLevel.FULL) assertTrue(SoundLevel.isOn(level))
    }

    @Test
    fun `a fresh install starts at full volume`() {
        assertEquals(SoundLevel.FULL, SoundLevel.DEFAULT)
    }
}
