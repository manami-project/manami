package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.regularFileExists
import kotlinx.coroutines.flow.update
import kotlin.collections.toList

internal object InternalState: State {

    private val animeList: MutableList<AnimeListEntry> = mutableListOf()
    private val watchList: MutableList<WatchListEntry> = mutableListOf()
    private val ignoreList: MutableList<IgnoreListEntry> = mutableListOf()

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

    override fun animeListEntryExists(anime: AnimeListEntry): Boolean = animeList.contains(anime)

    override fun animeList(): List<AnimeListEntry> = animeList.toList()

    override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
        animeList.addAll(anime.distinct())

        val uris = anime.map { it.link }.filterIsInstance<Link>().map { it.uri }.toSet()
        watchList.removeIf { uris.contains(it.link.uri) }
        ignoreList.removeIf { uris.contains(it.link.uri) }

        notifyAllEventBus()
    }

    override fun removeAnimeListEntry(entry: AnimeListEntry) {
        animeList.remove(entry)
        CoroutinesFlowEventBus.animeListState.update { current -> current.copy(entries = animeList()) }
    }

    override fun watchList(): Set<WatchListEntry> = watchList.toSet()

    override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
        watchList.addAll(anime.distinct())
        CoroutinesFlowEventBus.watchListState.update { current -> current.copy(entries = watchList()) }

        val uris = anime.map { it.link.uri }.toSet()

        if (ignoreList.removeIf { uris.contains(it.link.uri) }) {
            CoroutinesFlowEventBus.watchListState.update { current -> current.copy(entries = watchList()) }
        }
    }

    override fun removeWatchListEntry(entry: WatchListEntry) {
        watchList.remove(entry)
        CoroutinesFlowEventBus.watchListState.update { current -> current.copy(entries = watchList()) }
    }

    override fun ignoreList(): Set<IgnoreListEntry> = ignoreList.toSet()

    override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
        ignoreList.addAll(anime.distinct())
        CoroutinesFlowEventBus.ignoreListState.update { current -> current.copy(entries = ignoreList()) }

        val uris = anime.map { it.link.uri }.toSet()

        if (watchList.removeIf { uris.contains(it.link.uri) }) {
            CoroutinesFlowEventBus.ignoreListState.update { current -> current.copy(entries = ignoreList()) }
        }
    }

    override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
        ignoreList.remove(entry)
        CoroutinesFlowEventBus.ignoreListState.update { current -> current.copy(entries = ignoreList()) }
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

        notifyAllEventBus()
    }

    override fun clear() {
        animeList.clear()
        watchList.clear()
        ignoreList.clear()
        notifyAllEventBus()
    }

    private fun notifyAllEventBus() {
        CoroutinesFlowEventBus.animeListState.update { current -> current.copy(entries = animeList()) }
        CoroutinesFlowEventBus.watchListState.update { current -> current.copy(entries = watchList()) }
        CoroutinesFlowEventBus.ignoreListState.update { current -> current.copy(entries = ignoreList()) }
    }
}

internal sealed class OpenedFile
internal data object NoFile : OpenedFile()
internal data class CurrentFile(val regularFile: RegularFile) : OpenedFile()