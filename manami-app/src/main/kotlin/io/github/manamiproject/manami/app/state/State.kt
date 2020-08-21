package io.github.manamiproject.manami.app.state

import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.models.Anime
import java.net.URL

interface State {

    fun upsertAnimeListEntry(anime: Anime)
    fun removeAnimeListEntry(anime: Anime)

    fun upsertWatchListEntry(url: URL)
    fun removeWatchListEntry(url: URL)

    fun upsertIgnoreListEntry(url: URL)
    fun removeIgnoreListEntry(url: URL)

    fun openedFile(file: RegularFile)
    fun closeFile()
}