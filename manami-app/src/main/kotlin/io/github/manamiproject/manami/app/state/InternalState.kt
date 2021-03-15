package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.WatchListEntry
import io.github.manamiproject.manami.app.state.events.EventfulList
import io.github.manamiproject.manami.app.state.events.ListChangedEvent.ListType.*
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.regularFileExists

internal object InternalState : State {

    private val animeList: EventfulList<AnimeListEntry> = EventfulList(ANIME_LIST)
    private val watchList: EventfulList<WatchListEntry> = EventfulList(WATCH_LIST)
    private val ignoreList: EventfulList<IgnoreListEntry> = EventfulList(IGNORE_LIST)

    private var openedFile: OpenedFile = NoFile

    override fun openedFile(file: RegularFile) {
        check(file.regularFileExists())
        openedFile = CurrentFile(file)
    }

    override fun closeFile() {
        clear()
        openedFile = NoFile
    }

    override fun animeList(): List<AnimeListEntry> = animeList.toList()

    override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
        animeList.addAll(anime.distinct())
    }

    override fun watchList(): Set<WatchListEntry> = watchList.toSet()

    override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
        watchList.addAll(anime.distinct())
    }

    override fun ignoreList(): Set<IgnoreListEntry> = ignoreList.toSet()

    override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
        ignoreList.addAll(anime.distinct())
    }

    override fun createSnapshot(): Snapshot = StateSnapshot(
        animeList = animeList(),
        watchList = watchList(),
        ignoreList = ignoreList(),
    )

    override fun restore(snapshot: Snapshot) {
        animeList.clear()
        animeList.addAll(snapshot.animeList())

        watchList.clear()
        watchList.addAll(snapshot.watchList())

        ignoreList.clear()
        ignoreList.addAll(snapshot.ignoreList())
    }

    override fun clear() {
        animeList.clear()
        watchList.clear()
        ignoreList.clear()
    }
}

private sealed class OpenedFile
private object NoFile : OpenedFile()
private data class CurrentFile(val regularFile: RegularFile) : OpenedFile()