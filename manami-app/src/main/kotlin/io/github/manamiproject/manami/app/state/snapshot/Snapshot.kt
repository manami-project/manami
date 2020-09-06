package io.github.manamiproject.manami.app.state.snapshot

import io.github.manamiproject.manami.app.models.AnimeListEntry
import java.net.URL

internal interface Snapshot {

    fun animeList(): List<AnimeListEntry>

    fun watchList(): Set<URL>

    fun ignoreList(): Set<URL>
}