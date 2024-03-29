package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import java.net.URI

interface ListHandler {

    fun addAnimeListEntry(entry: AnimeListEntry)
    fun animeList(): List<AnimeListEntry>
    fun removeAnimeListEntry(entry: AnimeListEntry)
    fun replaceAnimeListEntry(current: AnimeListEntry, replacement: AnimeListEntry)

    fun addWatchListEntry(uris: Collection<URI>)
    fun watchList(): Set<WatchListEntry>
    fun removeWatchListEntry(entry: WatchListEntry)

    fun addIgnoreListEntry(uris: Collection<URI>)
    fun ignoreList(): Set<IgnoreListEntry>
    fun removeIgnoreListEntry(entry: IgnoreListEntry)
}