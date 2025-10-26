package io.github.manamiproject.manami.app.inconsistencies.lists.deadentries

import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

data class DeadEntriesInconsistenciesResult(
    val watchListResults: List<WatchListEntry> = emptyList(),
    val ignoreListResults: List<IgnoreListEntry> = emptyList(),
)