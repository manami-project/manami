package io.github.manamiproject.manami.app.state.snapshot

import io.github.manamiproject.manami.app.models.AnimeListEntry
import java.net.URL

internal data class StateSnapshot(
        private val animeList: List<AnimeListEntry> = emptyList(),
        private val watchList: Set<URL> = emptySet(),
        private val ignoreList: Set<URL> = emptySet(),
) : Snapshot {

    override fun animeList(): List<AnimeListEntry> = animeList

    override fun watchList(): Set<URL> = watchList

    override fun ignoreList(): Set<URL> = ignoreList
}