package io.github.manamiproject.manami.app.inconsistencies.metadata

import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

internal data class MetaDataInconsistenciesResult(
    val watchListResults: List<MetaDataDiff<WatchListEntry>>,
    val ignoreListResults: List<MetaDataDiff<IgnoreListEntry>>
)