/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.ui

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.treboada.opensatfinder.R
import net.treboada.opensatfinder.ui.theme.AppTheme

/** Where the catalogue comes from, and where to read about it. */
internal const val CELESTRAK_URL = "https://celestrak.org"

/** The word the link wraps: a proper name, so it reads the same in every translation. */
internal const val CELESTRAK_NAME = "CelesTrak"

/** The public home of the source: the licence says a copy of it travels with the APK. */
internal const val SOURCE_URL = "https://github.com/Treboada/OpenSatFinder"

/** How the source address reads to a person, as a proper noun: the same in every language. */
internal const val SOURCE_NAME = "github.com/Treboada/OpenSatFinder"

/**
 * Where the licence's own text can be read. Its name is a string resource - `GNU GPL v3` is
 * a proper name, the qualifier after it is ordinary language - and one resource feeds both
 * the sentence and the link, so the two cannot drift apart.
 */
internal const val LICENCE_URL = "https://www.gnu.org/licenses/gpl-3.0.html"

/** Who wrote it, and where to write back. */
internal const val AUTHOR_NAME = "Rafa Couto"
internal const val AUTHOR_EMAIL = "caligari@treboada.net"
internal const val AUTHOR_MAILTO = "mailto:$AUTHOR_EMAIL"

/**
 * Where a donation can be left. The app reaches no network itself - the link is opened by
 * whatever the platform uses for browsing - so this costs the app no permission.
 */
internal const val DONATE_URL = "https://liberapay.com/caligari/"

/**
 * What the app is, which version of it is installed, and the two promises that matter: the
 * position stays on the phone, and the catalogue comes from somewhere named - named and
 * linked, so it can be checked.
 *
 * The version is read from the installed package instead of a constant, so it cannot drift
 * from what the user actually has.
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .safeDrawingPadding(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BackButton(onClick = onBack, tag = "about_back")
            Text(
                text = stringResource(R.string.about),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(modifier = Modifier.testTag("about_icon"))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    val version = installedVersion(context)
                    Text(
                        text =
                            if (version == null) {
                                stringResource(R.string.about_version_unknown)
                            } else {
                                stringResource(R.string.about_version, version)
                            },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("about_version"),
                    )
                }
            }
            AboutHeading(R.string.about_heading_how)
            AboutParagraph(R.string.about_purpose)
            AboutHeading(R.string.about_heading_privacy)
            AboutParagraph(R.string.about_location)
            AboutParagraph(R.string.about_free)
            LicenceParagraph()
            SourceParagraph()
            AboutHeading(R.string.about_heading_thanks)
            CatalogueParagraph()
            AuthorParagraph()
            DonationParagraph()
        }
    }
}

/** The paragraph with its source made a link: tapping it opens the browser. */
@Composable
private fun CatalogueParagraph() {
    Spacer(Modifier.height(6.dp))
    Text(
        text = linkedWord(stringResource(R.string.about_catalogue), CELESTRAK_NAME, CELESTRAK_URL, linkStyle()),
        style = MaterialTheme.typography.bodyMedium,
    )
}

/** The author, with the address made a link: tapping it opens the mail app. */
@Composable
private fun AuthorParagraph() {
    Spacer(Modifier.height(20.dp))
    val line = stringResource(R.string.about_author, AUTHOR_NAME, AUTHOR_EMAIL)
    Text(
        text = linkedWord(line, AUTHOR_EMAIL, AUTHOR_MAILTO, linkStyle()),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.testTag("about_author"),
    )
}

/** The invitation that follows the author: one sentence, and the phrase that acts on it. */
@Composable
private fun DonationParagraph() {
    Spacer(Modifier.height(6.dp))
    val phrase = stringResource(R.string.about_donate_link)
    Text(
        text = linkedWord(stringResource(R.string.about_donate, phrase), phrase, DONATE_URL, linkStyle()),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.testTag("about_donate"),
    )
}

/** The licence, with its name made a link: the terms are worth being able to read. */
@Composable
private fun LicenceParagraph() {
    val name = stringResource(R.string.about_licence_name)
    Spacer(Modifier.height(6.dp))
    Text(
        text = linkedWord(stringResource(R.string.about_licence, name), name, LICENCE_URL, linkStyle()),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.testTag("about_licence"),
    )
}

/** Where the source lives, linked: the same address the licence expects to travel. */
@Composable
private fun SourceParagraph() {
    Spacer(Modifier.height(6.dp))
    Text(
        text = linkedWord(stringResource(R.string.about_source, SOURCE_NAME), SOURCE_NAME, SOURCE_URL, linkStyle()),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.testTag("about_source"),
    )
}

/** How a link looks everywhere on this screen. */
@Composable
private fun linkStyle(): TextLinkStyles =
    TextLinkStyles(
        style =
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            ),
    )

/**
 * [text] with one [word] of it carrying [url], and everything else left exactly as it was.
 * The word has to be identical in every translation - a proper name, an address - which is
 * what makes this work without markers in the resources.
 */
internal fun linkedWord(
    text: String,
    word: String,
    url: String,
    linkStyle: TextLinkStyles? = null,
): AnnotatedString {
    val start = text.indexOf(word)
    if (start < 0 || word.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text.substring(0, start))
        withLink(LinkAnnotation.Url(url, linkStyle)) {
            append(word)
        }
        append(text.substring(start + word.length))
    }
}

/** What the paragraphs under it are about, so the screen reads as three answers. */
@Composable
private fun AboutHeading(text: Int) {
    Spacer(Modifier.height(20.dp))
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun AboutParagraph(text: Int) {
    Spacer(Modifier.height(6.dp))
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodyMedium,
    )
}

/** The version of the package the user is running, or nothing when the platform withholds it. */
private fun installedVersion(context: Context): String? =
    runCatching {
        val manager = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            manager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0)).versionName
        } else {
            @Suppress("DEPRECATION")
            manager.getPackageInfo(context.packageName, 0).versionName
        }
    }.getOrNull()

@Preview(name = "Light", showBackground = true)
@Preview(name = "Night", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AboutScreenPreview() {
    AppTheme {
        AboutScreen(onBack = {})
    }
}
