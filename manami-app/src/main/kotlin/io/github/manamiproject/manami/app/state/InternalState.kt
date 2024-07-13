package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.events.EventListType.*
import io.github.manamiproject.manami.app.events.EventfulList
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.regularFileExists

internal object InternalState : State {

    private val animeList: EventfulList<AnimeListEntry> = EventfulList(ANIME_LIST)
    private val watchList: EventfulList<WatchListEntry> = EventfulList(WATCH_LIST)
    private val ignoreList: EventfulList<IgnoreListEntry> = EventfulList(IGNORE_LIST)

    private var openedFile: OpenedFile = NoFile

    override fun setOpenedFile(file: RegularFile) {
        check(file.regularFileExists()) { "Path is not a regular file" }
        openedFile = CurrentFile(file)
    }

    override fun openedFile(): OpenedFile = openedFile

    override fun closeFile() {
        clear()
        openedFile = NoFile
    }

    override fun animeListEntrtyExists(anime: AnimeListEntry): Boolean = animeList.contains(anime)

    override fun animeList(): List<AnimeListEntry> = animeList.toList()

    override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
        animeList.addAll(anime.distinct())

        val uris = anime.map { it.link }.filterIsInstance<Link>().map { it.uri }.toSet()
        watchList.removeIf { uris.contains(it.link.uri) }
        ignoreList.removeIf { uris.contains(it.link.uri) }
    }

    override fun removeAnimeListEntry(entry: AnimeListEntry) {
        animeList.remove(entry)
    }

    override fun watchList(): Set<WatchListEntry> = watchList.toSet()

    override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
        watchList.addAll(anime.distinct())

        val uris = anime.map { it.link.uri }.toSet()
        ignoreList.removeIf { uris.contains(it.link.uri) }
    }

    override fun removeWatchListEntry(entry: WatchListEntry) {
        watchList.remove(entry)
    }

    override fun ignoreList(): Set<IgnoreListEntry> = ignoreList.toSet()

    override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
        ignoreList.addAll(anime.distinct())

        val uris = anime.map { it.link.uri }.toSet()
        watchList.removeIf { uris.contains(it.link.uri) }
    }

    override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
        ignoreList.remove(entry)
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

internal sealed class OpenedFile
internal object NoFile : OpenedFile()
internal data class CurrentFile(val regularFile: RegularFile) : OpenedFile()