package io.github.manamiproject.manami.gui.components.animetable

import androidx.compose.foundation.lazy.LazyListState
import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.LinkEntry
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import kotlinx.coroutines.flow.StateFlow

internal interface AnimeTableViewModel<T: AnimeEntry> {

    val source: StateFlow<List<T>>
    val entries: StateFlow<List<T>>
    val isFileOpeningRunning: StateFlow<Boolean>
    val listState: LazyListState

    fun isSortable(value: Boolean)
    fun addToAnimeList(anime: T)
    fun editAnimeListEntry(anime: AnimeListEntry)
    fun addToWatchList(anime: T)
    fun addToIgnoreList(anime: T)
    fun hide(anime: T)
    fun delete(anime: T)
    fun sort(direction: AnimeTableSortDirection)
    fun showAnimeDetails(link: LinkEntry)
    fun findRelatedAnime(link: LinkEntry)
    fun findSimilarAnime(link: LinkEntry)
    fun openDirectory(anime: T)
    fun clearHiddenEntries()

    fun saveScrollPosition()
    suspend fun restoreScrollPosition()
}