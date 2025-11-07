package io.github.manamiproject.manami.gui.components.animetable

import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.LinkEntry
import io.github.manamiproject.modb.core.anime.Anime
import kotlinx.coroutines.flow.StateFlow

internal interface AnimeTableViewModel<T: AnimeEntry> {

    val source: StateFlow<List<T>>
    val entries: StateFlow<List<T>>

    val isAnimeDetailsRunning: StateFlow<Boolean>
    val animeDetails: StateFlow<Anime?>
    val showAnimeDetails: StateFlow<Boolean>

    val isFileOpeningRunning: StateFlow<Boolean>

    fun addToWatchList(anime: T)
    fun addToIgnoreList(anime: T)
    fun hide(anime: T)
    fun delete(anime: T)
    fun sort(direction: AnimeTableSortDirection)
    fun showAnimeDetails(link: LinkEntry)
    fun hideAnimeDetails()
    fun openDirectory(anime: T)
}