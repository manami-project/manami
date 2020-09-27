package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URL

internal object TestState: State {
    override fun openedFile(file: RegularFile) = shouldNotBeInvoked()
    override fun closeFile() = shouldNotBeInvoked()
    override fun animeList(): List<AnimeListEntry> = shouldNotBeInvoked()
    override fun addAllAnimeListEntries(anime: Set<AnimeListEntry>) = shouldNotBeInvoked()
    override fun watchList(): Set<URL> = shouldNotBeInvoked()
    override fun addAllWatchListEntries(anime: Set<URL>) = shouldNotBeInvoked()
    override fun ignoreList(): Set<URL> = shouldNotBeInvoked()
    override fun addAllIgnoreListEntries(anime: Set<URL>) = shouldNotBeInvoked()
    override fun createSnapshot(): Snapshot = shouldNotBeInvoked()
    override fun restore(snapshot: Snapshot) = shouldNotBeInvoked()
    override fun clear() = shouldNotBeInvoked()
}

internal object TestSnapshot : Snapshot {
    override fun animeList(): List<AnimeListEntry> = shouldNotBeInvoked()
    override fun watchList(): Set<URL> = shouldNotBeInvoked()
    override fun ignoreList(): Set<URL> = shouldNotBeInvoked()
}