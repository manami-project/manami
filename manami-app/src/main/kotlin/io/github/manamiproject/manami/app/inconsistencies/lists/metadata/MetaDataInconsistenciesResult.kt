package io.github.manamiproject.manami.app.inconsistencies.lists.metadata

import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

internal data class MetaDataInconsistenciesResult(
    val watchListResults: List<MetaDataDiff<WatchListEntry>> = emptyList(),
    val ignoreListResults: List<MetaDataDiff<IgnoreListEntry>> = emptyList(),
)