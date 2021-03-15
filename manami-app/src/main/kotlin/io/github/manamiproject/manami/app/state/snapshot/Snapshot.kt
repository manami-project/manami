package io.github.manamiproject.manami.app.state.snapshot

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry

internal interface Snapshot {

    fun animeList(): List<AnimeListEntry>

    fun watchList(): Set<WatchListEntry>

    fun ignoreList(): Set<IgnoreListEntry>
}