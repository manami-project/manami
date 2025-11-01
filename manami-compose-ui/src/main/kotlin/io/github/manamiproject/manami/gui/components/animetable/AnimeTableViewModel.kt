package io.github.manamiproject.manami.gui.components.animetable

import io.github.manamiproject.manami.app.lists.AnimeEntry
import kotlinx.coroutines.flow.StateFlow

internal interface AnimeTableViewModel<T: AnimeEntry> {

    val source: StateFlow<List<T>>
    val entries: StateFlow<List<T>>

    fun addToWatchList(anime: T)
    fun addToIgnoreList(anime: T)
    fun hide(anime: T)
    fun delete(anime: T)
    fun sort(direction: AnimeTableSortDirection)
}