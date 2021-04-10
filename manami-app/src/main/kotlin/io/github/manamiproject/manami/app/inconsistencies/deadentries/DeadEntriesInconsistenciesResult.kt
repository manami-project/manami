package io.github.manamiproject.manami.app.inconsistencies.deadentries

import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

data class DeadEntriesInconsistenciesResult(
    val watchListResults: List<WatchListEntry>,
    val ignoreListResults: List<IgnoreListEntry>
)