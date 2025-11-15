package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.versioning.SemanticVersion

internal data class ManamiFile(
    val version: SemanticVersion = SemanticVersion(),
    val animeListEntries: Collection<AnimeListEntry> = emptySet(),
    val watchListEntries: Collection<WatchListEntry> = emptySet(),
    val ignoreListEntries:  Collection<IgnoreListEntry> = emptySet(),
)