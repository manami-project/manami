package io.github.manamiproject.manami.gui.migration

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.modb.core.anime.Title
import java.net.URI

internal data class MigrationTableEntry(
    val thumbnail: URI,
    val title: Title,
    val currentLink: Link,
    val alternatives: Set<Link>,
    val animeListEntry: AnimeListEntry? = null,
    val watchListEntry: WatchListEntry? = null,
    val ignoreListEntry: IgnoreListEntry? = null,
)