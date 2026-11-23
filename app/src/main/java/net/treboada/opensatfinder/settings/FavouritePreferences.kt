/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.settings

import android.content.Context
import androidx.core.content.edit

/**
 * The satellites marked as favourites, kept between launches by name.
 *
 * Names, not records, for the same reason the chosen satellite is stored that way: a name a
 * newer catalogue no longer carries simply stops matching, instead of resurrecting a satellite
 * that no longer exists.
 *
 * Nothing stored is not the same as nothing starred: the key absent means nobody has starred
 * anything yet, and the well-known neighbourhoods stand in until someone does. Starring or
 * unstarring writes the whole set, an empty one included, so a reader who unstars every
 * default gets what they asked for and keeps it.
 */
class FavouritePreferences(
    context: Context,
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    /** The stored names, copied out: the platform hands back a set it may go on mutating. */
    fun names(): Set<String> = preferences.getStringSet(KEY_FAVOURITES, null)?.toSet() ?: DEFAULT_FAVOURITES

    fun save(names: Set<String>) {
        preferences.edit { putStringSet(KEY_FAVOURITES, names) }
    }

    /** Forgets the choice entirely, which puts the default stars back; write an empty set to
     * keep none. */
    fun clear() {
        preferences.edit { remove(KEY_FAVOURITES) }
    }

    private companion object {
        const val KEY_FAVOURITES = "favourite_satellites"
    }
}
