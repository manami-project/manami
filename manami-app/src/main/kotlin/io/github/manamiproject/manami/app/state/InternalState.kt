package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.regularFileExists
import java.net.URL

internal object InternalState : State {

    private val animeList: MutableList<AnimeListEntry> = mutableListOf()
    private val watchList: MutableSet<URL> = mutableSetOf()
    private val ignoreList: MutableSet<URL> = mutableSetOf()

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

    override fun watchList(): Set<URL> = watchList.toSet()

    override fun ignoreList(): Set<URL> = ignoreList.toSet()

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



















    override fun addAllAnimeListEntries(anime: Set<AnimeListEntry>) {
        animeList.addAll(anime)
    }

    override fun upsertAnimeListEntry(anime: AnimeListEntry) {
        TODO("Not yet implemented")
    }

    override fun removeAnimeListEntry(anime: AnimeListEntry) {
        TODO("Not yet implemented")
    }

    override fun addAllWatchListEntries(anime: Set<URL>) {
        watchList.addAll(anime)
    }


    override fun upsertWatchListEntry(url: URL) {
        TODO("Not yet implemented")
    }

    override fun removeWatchListEntry(url: URL) {
        TODO("Not yet implemented")
    }

    override fun addAllIgnoreListEntries(anime: Set<URL>) {
        ignoreList.addAll(anime)
    }

    override fun upsertIgnoreListEntry(url: URL) {
        TODO("Not yet implemented")
    }

    override fun removeIgnoreListEntry(url: URL) {
        TODO("Not yet implemented")
    }

}

private sealed class OpenedFile
private object NoFile : OpenedFile()
private data class CurrentFile(val regularFile: RegularFile) : OpenedFile()