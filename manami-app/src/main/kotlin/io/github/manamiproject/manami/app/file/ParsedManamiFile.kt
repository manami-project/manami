package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.WatchListEntry

data class ParsedManamiFile(
    val animeListEntries: Set<AnimeListEntry> = emptySet(),
    val watchListEntries: Set<WatchListEntry> = emptySet(),
    val ignoreListEntries:  Set<IgnoreListEntry> = emptySet(),
)