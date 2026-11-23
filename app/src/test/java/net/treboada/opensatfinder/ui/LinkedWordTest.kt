/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import androidx.compose.ui.text.LinkAnnotation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkedWordTest {
    private val sentence = "The catalogue is the list of geostationary satellites CelesTrak publishes."

    private fun urlOf(annotated: androidx.compose.ui.text.AnnotatedString): String {
        val link = annotated.getLinkAnnotations(0, annotated.length).single()
        return (link.item as LinkAnnotation.Url).url
    }

    @Test
    fun `the word is linked where it is written`() {
        val annotated = linkedWord(sentence, CELESTRAK_NAME, CELESTRAK_URL)
        val link = annotated.getLinkAnnotations(0, annotated.length).single()

        assertEquals(CELESTRAK_NAME, annotated.text.substring(link.start, link.end))
        assertEquals(CELESTRAK_URL, urlOf(annotated))
    }

    @Test
    fun `the words stay the words`() {
        assertEquals(sentence, linkedWord(sentence, CELESTRAK_NAME, CELESTRAK_URL).text)
    }

    @Test
    fun `a text without the word is left alone`() {
        val text = "The catalogue comes from somewhere."

        val annotated = linkedWord(text, CELESTRAK_NAME, CELESTRAK_URL)

        assertEquals(text, annotated.text)
        assertTrue(annotated.getLinkAnnotations(0, annotated.length).isEmpty())
    }

    @Test
    fun `the link covers one word and no more`() {
        val annotated = linkedWord(sentence, CELESTRAK_NAME, CELESTRAK_URL)
        val link = annotated.getLinkAnnotations(0, annotated.length).single()

        assertEquals(CELESTRAK_NAME.length, link.end - link.start)
    }

    @Test
    fun `the licence name is what carries the link, in any language`() {
        val name = "GNU GPL v3 o posterior"
        val line = "Licencia: $name"

        val annotated = linkedWord(line, name, LICENCE_URL)
        val link = annotated.getLinkAnnotations(0, annotated.length).single()

        assertEquals(name, annotated.text.substring(link.start, link.end))
        assertEquals("https://www.gnu.org/licenses/gpl-3.0.html", urlOf(annotated))
        assertEquals(line, annotated.text)
    }

    @Test
    fun `a phrase of several words links as one`() {
        val phrase = "puedes hacer una pequeña donación"
        val line = "Si te gusta esta app, $phrase."

        val annotated = linkedWord(line, phrase, DONATE_URL)
        val link = annotated.getLinkAnnotations(0, annotated.length).single()

        assertEquals(phrase, annotated.text.substring(link.start, link.end))
        assertEquals("https://liberapay.com/caligari/", urlOf(annotated))
        assertEquals(line, annotated.text)
    }

    @Test
    fun `the address links to the mail app`() {
        val line = "Author: Rafa Couto <caligari@treboada.net>"

        val annotated = linkedWord(line, AUTHOR_EMAIL, AUTHOR_MAILTO)
        val link = annotated.getLinkAnnotations(0, annotated.length).single()

        assertEquals(AUTHOR_EMAIL, annotated.text.substring(link.start, link.end))
        assertEquals("mailto:$AUTHOR_EMAIL", urlOf(annotated))
        assertEquals(line, annotated.text)
    }
}
