package io.github.manamiproject.manami.gui.components.animetable

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.gui.components.animetable.AnimeTableSortDirection.ASC
import io.github.manamiproject.manami.gui.components.animetable.AnimeTableSortDirection.DESC
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.launch

internal abstract class DefaultAnimeTableViewModel<T: AnimeEntry>(private val app: Manami = Manami.instance): AnimeTableViewModel<T> {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())
    private val sortDirection = MutableStateFlow(ASC)
    private val hidden = MutableStateFlow<MutableSet<T>>(mutableSetOf())

    override val entries: StateFlow<List<T>>
        get() = combine(source, hidden, sortDirection) { sourceList, hiddenList, sortDirection ->
            sourceList.toMutableList().apply {
                removeAll(hiddenList)
                when (sortDirection) {
                    ASC -> sortBy { it.title }
                    DESC -> sortByDescending { it.title }
                }
            }
        }.stateIn(viewModelScope, Eagerly, emptyList())

    override fun addToWatchList(anime: T) {
        viewModelScope.launch {
            app.addWatchListEntry(setOf(anime.link.asLink().uri))
        }
    }

    override fun addToIgnoreList(anime: T) {
        viewModelScope.launch {
            app.addIgnoreListEntry(setOf(anime.link.asLink().uri))
        }
    }

    override fun hide(anime: T) {
        if (!hidden.value.contains(anime)) {
            hidden.update { current -> current.apply { add(anime) } }
        }
    }

    override fun sort(direction: AnimeTableSortDirection) {
        if (sortDirection.value != direction) {
            sortDirection.update { direction }
        }
    }
}