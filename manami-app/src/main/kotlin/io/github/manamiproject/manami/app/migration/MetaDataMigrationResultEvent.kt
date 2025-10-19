package io.github.manamiproject.manami.app.migration

import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

data class MetaDataMigrationResultEvent(
    val animeListEntriesWithoutMapping: Collection<AnimeListEntry>,
    val animeListEntriesMultipleMappings: Map<AnimeListEntry, Set<Link>>,
    val animeListMappings: Map<AnimeListEntry, Link>,
    val watchListEntriesWithoutMapping: Collection<WatchListEntry>,
    val watchListEntriesMultipleMappings: Map<WatchListEntry, Set<Link>>,
    val watchListMappings: Map<WatchListEntry, Link>,
    val ignoreListEntriesWithoutMapping: Collection<IgnoreListEntry>,
    val ignoreListEntriesMultipleMappings: Map<IgnoreListEntry, Set<Link>>,
    val ignoreListMappings: Map<IgnoreListEntry, Link>,
) : Event