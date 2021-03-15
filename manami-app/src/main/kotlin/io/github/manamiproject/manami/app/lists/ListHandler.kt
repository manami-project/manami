package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import java.net.URI

interface ListHandler {

    fun addWatchListEntry(uri: URI)
    fun watchList(): Set<WatchListEntry>

    fun ignoreList(): Set<IgnoreListEntry>
}