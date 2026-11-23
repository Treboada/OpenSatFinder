/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * The F-Droid listing is files in the repository, and a file the store cannot read is a release
 * it rejects weeks later. These are the rules it applies, checked here instead: every language
 * the app speaks, texts within the store's limits, a changelog for the current version code,
 * and an icon and screenshots where the client looks for them.
 */
class FastlaneListingTest {
    private val metadata = File(repoRoot(), "fastlane/metadata/android")

    /** The same eight the app translates into; a language the listing lacks falls back to English. */
    private val languages = listOf("en-US", "es-ES", "ca", "de", "eu", "fr", "gl", "pt")

    @Test
    fun `every language of the app has a listing`() {
        assertEquals(emptyList<String>(), languages.filter { !File(metadata, it).isDirectory })
    }

    @Test
    fun `the texts stay within the limits the store applies`() {
        for (language in languages) {
            val folder = File(metadata, language)
            val title = text(folder, "title.txt")
            val short = text(folder, "short_description.txt")
            val full = text(folder, "full_description.txt")

            assertTrue("$language: title is ${title.length} characters", title.length in 1..30)
            assertTrue("$language: short description is ${short.length} characters", short.length in 1..80)
            assertTrue("$language: full description is ${full.length} characters", full.length in 1..4000)
        }
    }

    @Test
    fun `every version code has a changelog in every language`() {
        val code = versionCode()
        for (language in languages) {
            val changelog = File(metadata, "$language/changelogs/$code.txt")

            assertTrue("$language has no changelog for version code $code", changelog.isFile)
            assertTrue("$language: changelog is empty", changelog.readText().isNotBlank())
        }
    }

    @Test
    fun `the base listing carries the icon and the screenshots`() {
        assertTrue("no listing icon", File(metadata, "en-US/images/icon.png").isFile)

        val screenshots = File(metadata, "en-US/images/phoneScreenshots").listFiles().orEmpty()
        assertTrue("no screenshots", screenshots.any { it.name.endsWith(".png") })
    }

    private fun text(
        folder: File,
        name: String,
    ): String {
        val file = File(folder, name)
        assertTrue("$folder has no $name", file.isFile)
        return file.readText().trim()
    }

    /** The `versionCode` the APK will carry, read from the module that declares it. */
    private fun versionCode(): Int {
        val build = File(repoRoot(), "app/build.gradle.kts").readText()
        val match =
            Regex("""versionCode\s*=\s*(\d+)""").find(build)
                ?: error("no versionCode in app/build.gradle.kts")
        return match.groupValues[1].toInt()
    }

    /** The repository root, found upwards from wherever the test runner put us. */
    private fun repoRoot(): File {
        val start =
            System.getProperty("user.dir")?.let(::File)?.absoluteFile
                ?: error("the test runner gave no working directory")
        return generateSequence(start) { it.parentFile }
            .firstOrNull { File(it, "fastlane/metadata/android").isDirectory }
            ?: error("no repository root above $start")
    }
}
