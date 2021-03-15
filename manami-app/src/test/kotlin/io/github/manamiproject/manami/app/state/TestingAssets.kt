package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.test.shouldNotBeInvoked

internal object TestState: State {
    override fun openedFile(file: RegularFile) = shouldNotBeInvoked()
    override fun closeFile() = shouldNotBeInvoked()
    override fun animeList(): List<AnimeListEntry> = shouldNotBeInvoked()
    override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) = shouldNotBeInvoked()
    override fun watchList(): Set<WatchListEntry> = shouldNotBeInvoked()
    override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) = shouldNotBeInvoked()
    override fun ignoreList(): Set<IgnoreListEntry> = shouldNotBeInvoked()
    override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) = shouldNotBeInvoked()
    override fun createSnapshot(): Snapshot = shouldNotBeInvoked()
    override fun restore(snapshot: Snapshot) = shouldNotBeInvoked()
    override fun clear() = shouldNotBeInvoked()
}

internal object TestSnapshot : Snapshot {
    override fun animeList(): List<AnimeListEntry> = shouldNotBeInvoked()
    override fun watchList(): Set<WatchListEntry> = shouldNotBeInvoked()
    override fun ignoreList(): Set<IgnoreListEntry> = shouldNotBeInvoked()
}