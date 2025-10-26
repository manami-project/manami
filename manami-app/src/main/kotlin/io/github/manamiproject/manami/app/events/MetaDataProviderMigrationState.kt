package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

/**
 * @since 4.0.0
 */
data class MetaDataProviderMigrationState(
    val isRunning: Boolean = false,
    val animeListEntriesWithoutMapping: Collection<AnimeListEntry> = emptyList(),
    val animeListEntriesMultipleMappings: Map<AnimeListEntry, Set<Link>> = emptyMap(),
    val animeListMappings: Map<AnimeListEntry, Link> = emptyMap(),
    val watchListEntriesWithoutMapping: Collection<WatchListEntry> = emptyList(),
    val watchListEntriesMultipleMappings: Map<WatchListEntry, Set<Link>> = emptyMap(),
    val watchListMappings: Map<WatchListEntry, Link> = emptyMap(),
    val ignoreListEntriesWithoutMapping: Collection<IgnoreListEntry> = emptyList(),
    val ignoreListEntriesMultipleMappings: Map<IgnoreListEntry, Set<Link>> = emptyMap(),
    val ignoreListMappings: Map<IgnoreListEntry, Link> = emptyMap(),
)
