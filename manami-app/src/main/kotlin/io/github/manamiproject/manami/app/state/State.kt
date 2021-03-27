package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.extensions.RegularFile

internal interface State {

    fun setOpenedFile(file: RegularFile)
    fun openedFile(): OpenedFile
    fun closeFile()

    fun animeList(): List<AnimeListEntry>
    fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>)
    fun removeAnimeListEntry(entry: AnimeListEntry)

    fun watchList(): Set<WatchListEntry>
    fun addAllWatchListEntries(anime: Collection<WatchListEntry>)
    fun removeWatchListEntry(entry: WatchListEntry)

    fun ignoreList(): Set<IgnoreListEntry>
    fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>)
    fun removeIgnoreListEntry(entry: IgnoreListEntry)

    fun createSnapshot(): Snapshot
    fun restore(snapshot: Snapshot)

    fun clear()
}