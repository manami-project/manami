package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.WatchListEntry
import java.net.URI

interface ListHandler {

    fun addWatchListEntry(uri: URI)
    fun watchList(): Set<WatchListEntry>

    fun ignoreList(): Set<IgnoreListEntry>
}