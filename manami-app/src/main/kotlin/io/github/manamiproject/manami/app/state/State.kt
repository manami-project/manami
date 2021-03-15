package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.WatchListEntry
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.extensions.RegularFile

internal interface State {

    fun openedFile(file: RegularFile)
    fun closeFile()

    fun animeList(): List<AnimeListEntry>
    fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>)

    fun watchList(): Set<WatchListEntry>
    fun addAllWatchListEntries(anime: Collection<WatchListEntry>)

    fun ignoreList(): Set<IgnoreListEntry>
    fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>)

    fun createSnapshot(): Snapshot
    fun restore(snapshot: Snapshot)

    fun clear()
}