package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.WatchListEntry
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.extensions.RegularFile

internal interface State : StateHandler {

    fun openedFile(file: RegularFile)
    fun closeFile()

    fun animeList(): List<AnimeListEntry>
    fun addAllAnimeListEntries(anime: Set<AnimeListEntry>)

    fun addAllWatchListEntries(anime: Set<WatchListEntry>)

    fun addAllIgnoreListEntries(anime: Set<IgnoreListEntry>)

    fun createSnapshot(): Snapshot
    fun restore(snapshot: Snapshot)

    fun clear()
}