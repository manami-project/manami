package io.github.manamiproject.manami.gui.components.animetable

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.LinkEntry
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.gui.components.animetable.AnimeTableSortDirection.ASC
import io.github.manamiproject.manami.gui.components.animetable.AnimeTableSortDirection.DESC
import io.github.manamiproject.manami.gui.tabs.TabBarViewModel
import io.github.manamiproject.manami.gui.tabs.Tabs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.launch

internal abstract class DefaultAnimeTableViewModel<T: AnimeEntry>(
    private val app: Manami = Manami.instance,
    private val tableViewModel: TabBarViewModel = TabBarViewModel.instance,
): AnimeTableViewModel<T> {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())
    private val sortDirection = MutableStateFlow(ASC)
    private val hiddenEntries = MutableStateFlow<MutableSet<T>>(mutableSetOf())
    private var isSortable = true

    override val isFileOpeningRunning: StateFlow<Boolean>
        get() = app.generalAppState
            .map { it.isOpeningFileRunning }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = false,
            )

    override val entries: StateFlow<List<T>>
        get() = combine(source, hiddenEntries, sortDirection) { sourceList, hiddenList, sortDirection ->
            sourceList.toMutableList().apply {
                removeAll(hiddenList)
                if (isSortable) {
                    when (sortDirection) {
                        ASC -> sortBy { it.title }
                        DESC -> sortByDescending { it.title }
                    }
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
        if (!hiddenEntries.value.contains(anime)) {
            hiddenEntries.update { current -> current.apply { add(anime) } }
        }
    }

    override fun sort(direction: AnimeTableSortDirection) {
        if (sortDirection.value != direction) {
            sortDirection.update { direction }
        }
    }

    override fun showAnimeDetails(link: LinkEntry) {
        if (link == NoLink) return

        CoroutineScope(Default).launch {
            app.findAnimeDetails(link.asLink().uri)
        }

        tableViewModel.openOrActivate(FIND_ANIME_DETAILS)
    }

    override fun findRelatedAnime(link: LinkEntry) {
        if (link == NoLink) return

        CoroutineScope(Default).launch {
            app.findRelatedAnime(listOf(link.asLink().uri))
        }

        tableViewModel.openOrActivate(FIND_RELATED_ANIME)
    }

    override fun findSimilarAnime(link: LinkEntry) {
        if (link == NoLink) return

        CoroutineScope(Default).launch {
            app.findSimilarAnime(link.asLink().uri)
        }

        tableViewModel.openOrActivate(FIND_SIMILAR_ANIME)
    }

    override fun isSortable(value: Boolean) {
        isSortable = value
    }

    override fun openDirectory(anime: T) {
        throw UnsupportedOperationException()
    }
}