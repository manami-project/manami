package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

/**
 * @since 4.0.0
 */
data class WatchListState(
    val isAdditionRunning: Boolean = false,
    val entries: Collection<WatchListEntry> = emptyList(),
)
