package io.github.manamiproject.manami.app.state.snapshot

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.WatchListEntry

internal data class StateSnapshot(
        private val animeList: List<AnimeListEntry> = emptyList(),
        private val watchList: Set<WatchListEntry> = emptySet(),
        private val ignoreList: Set<IgnoreListEntry> = emptySet(),
) : Snapshot {

    override fun animeList(): List<AnimeListEntry> = animeList

    override fun watchList(): Set<WatchListEntry> = watchList

    override fun ignoreList(): Set<IgnoreListEntry> = ignoreList
}