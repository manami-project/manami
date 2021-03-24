package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import java.net.URI

interface ListHandler {

    fun addWatchListEntry(uris: Collection<URI>)
    fun watchList(): Set<WatchListEntry>
    fun removeWatchListEntry(entry: WatchListEntry)

    fun addIgnoreListEntry(uris: Collection<URI>)
    fun ignoreList(): Set<IgnoreListEntry>
    fun removeIgnoreListEntry(entry: IgnoreListEntry)
}