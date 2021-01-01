package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.WatchListEntry

interface StateHandler {

    fun watchList(): Set<WatchListEntry>
    fun ignoreList(): Set<IgnoreListEntry>
}