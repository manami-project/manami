package io.github.manamiproject.manami.app.state.snapshot

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.WatchListEntry

internal interface Snapshot {

    fun animeList(): List<AnimeListEntry>

    fun watchList(): Set<WatchListEntry>

    fun ignoreList(): Set<IgnoreListEntry>
}