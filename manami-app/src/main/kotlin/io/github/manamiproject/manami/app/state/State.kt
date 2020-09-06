package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.extensions.RegularFile
import java.net.URL

internal interface State {

    fun animeList(): List<AnimeListEntry>

    fun watchList(): Set<URL>

    fun ignoreList(): Set<URL>

    fun createSnapshot(): Snapshot

    fun restore(snapshot: Snapshot)

    fun clear()

















    fun addAllAnimeListEntries(anime: Set<AnimeListEntry>)
    fun upsertAnimeListEntry(anime: AnimeListEntry)
    fun removeAnimeListEntry(anime: AnimeListEntry)

    fun addAllWatchListEntries(anime: Set<URL>)
    fun upsertWatchListEntry(url: URL)
    fun removeWatchListEntry(url: URL)

    fun addAllIgnoreListEntries(anime: Set<URL>)
    fun upsertIgnoreListEntry(url: URL)
    fun removeIgnoreListEntry(url: URL)

    fun openedFile(file: RegularFile)
    fun closeFile()
}