package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.extensions.RegularFile
import java.net.URL

internal interface State {

    fun openedFile(file: RegularFile)
    fun closeFile()

    fun animeList(): List<AnimeListEntry>
    fun addAllAnimeListEntries(anime: Set<AnimeListEntry>)

    fun watchList(): Set<URL>
    fun addAllWatchListEntries(anime: Set<URL>)

    fun ignoreList(): Set<URL>
    fun addAllIgnoreListEntries(anime: Set<URL>)

    fun createSnapshot(): Snapshot
    fun restore(snapshot: Snapshot)

    fun clear()
}