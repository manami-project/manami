package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

/**
 * @since 4.0.0
 */
data class FindByTitleState (
    val isRunning: Boolean = false,
    val animeListResults: Collection<AnimeListEntry> = emptyList(),
    val watchListResults: Collection<WatchListEntry> = emptyList(),
    val ignoreListResults: Collection<IgnoreListEntry> = emptyList(),
    val unlistedResults: Collection<SearchResultAnimeEntry> = emptyList(),
)