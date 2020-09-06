package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URL

internal object TestState: State {
    override fun animeList(): List<AnimeListEntry> = shouldNotBeInvoked()
    override fun watchList(): Set<URL> = shouldNotBeInvoked()
    override fun ignoreList(): Set<URL> = shouldNotBeInvoked()
    override fun createSnapshot(): Snapshot = shouldNotBeInvoked()
    override fun restore(snapshot: Snapshot) = shouldNotBeInvoked()
    override fun clear() = shouldNotBeInvoked()
    override fun addAllAnimeListEntries(anime: Set<AnimeListEntry>) = shouldNotBeInvoked()
    override fun upsertAnimeListEntry(anime: AnimeListEntry) = shouldNotBeInvoked()
    override fun removeAnimeListEntry(anime: AnimeListEntry) = shouldNotBeInvoked()
    override fun addAllWatchListEntries(anime: Set<URL>) = shouldNotBeInvoked()
    override fun upsertWatchListEntry(url: URL) = shouldNotBeInvoked()
    override fun removeWatchListEntry(url: URL) = shouldNotBeInvoked()
    override fun addAllIgnoreListEntries(anime: Set<URL>) = shouldNotBeInvoked()
    override fun upsertIgnoreListEntry(url: URL) = shouldNotBeInvoked()
    override fun removeIgnoreListEntry(url: URL) = shouldNotBeInvoked()
    override fun openedFile(file: RegularFile) = shouldNotBeInvoked()
    override fun closeFile() = shouldNotBeInvoked()
}

internal object TestSnapshot : Snapshot {
    override fun animeList(): List<AnimeListEntry> = shouldNotBeInvoked()
    override fun watchList(): Set<URL> = shouldNotBeInvoked()
    override fun ignoreList(): Set<URL> = shouldNotBeInvoked()
}