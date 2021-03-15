package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

data class ParsedManamiFile(
    val animeListEntries: Set<AnimeListEntry> = emptySet(),
    val watchListEntries: Set<WatchListEntry> = emptySet(),
    val ignoreListEntries:  Set<IgnoreListEntry> = emptySet(),
)