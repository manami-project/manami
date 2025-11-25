package io.github.manamiproject.manami.app.migration

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.modb.core.config.Hostname

interface MetaDataProviderMigrationHandler {

    suspend fun checkMigration(metaDataProviderFrom: Hostname, metaDataProviderTo: Hostname)

    suspend fun migrate(
        animeListMappings: Map<AnimeListEntry, Link> = emptyMap(),
        watchListMappings: Map<WatchListEntry, Link> = emptyMap(),
        ignoreListMappings: Map<IgnoreListEntry, Link> = emptyMap(),
    )

    suspend fun removeUnmapped(
        animeListEntriesWithoutMapping: Collection<AnimeListEntry>,
        watchListEntriesWithoutMapping: Collection<WatchListEntry>,
        ignoreListEntriesWithoutMapping: Collection<IgnoreListEntry>,
    )
}