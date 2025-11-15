package io.github.manamiproject.manami.gui.components.animetable

import androidx.compose.foundation.lazy.LazyListState
import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.LinkEntry
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.gui.components.animetable.AnimeTableSortDirection.ASC
import io.github.manamiproject.manami.gui.components.animetable.AnimeTableSortDirection.DESC
import io.github.manamiproject.manami.gui.lists.animelist.AddAnimeToAnimeListFormViewModel
import io.github.manamiproject.manami.gui.lists.animelist.EditAnimeListEntryFormViewModel
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
    private val tabBarViewModel: TabBarViewModel = TabBarViewModel.instance,
    private val addAnimeToAnimeListFormViewModel: AddAnimeToAnimeListFormViewModel = AddAnimeToAnimeListFormViewModel.instance,
    private val editAnimeListEntryFormViewModel: EditAnimeListEntryFormViewModel = EditAnimeListEntryFormViewModel.instance,
): AnimeTableViewModel<T> {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    private val sortDirection = MutableStateFlow(ASC)
    private var isSortable = true

    private val hiddenEntries = MutableStateFlow<Set<T>>(emptySet())

    private var lastIndex = 0
    private var lastOffset = 0
    override val listState = LazyListState()

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

    override fun addToAnimeList(anime: T) {
        saveScrollPosition()
        viewModelScope.launch {
            tabBarViewModel.openOrActivate(ADD_ANIME_TO_ANIME_LIST_FORM)
            addAnimeToAnimeListFormViewModel.fetchAnimeDetails(anime.link)
        }
    }

    override fun editAnimeListEntry(anime: AnimeListEntry) {
        viewModelScope.launch {
            tabBarViewModel.openOrActivate(EDIT_ANIME_LIST_ENTRY_FORM)
            editAnimeListEntryFormViewModel.editEntry(anime)
        }
    }

    override fun addToWatchList(anime: T) {
        saveScrollPosition()
        viewModelScope.launch {
            app.addWatchListEntry(setOf(anime.link.asLink().uri))
        }
    }

    override fun addToIgnoreList(anime: T) {
        saveScrollPosition()
        viewModelScope.launch {
            app.addIgnoreListEntry(setOf(anime.link.asLink().uri))
        }
    }

    override fun hide(anime: T) {
        saveScrollPosition()
        if (!hiddenEntries.value.contains(anime)) {
            hiddenEntries.update { current -> current + anime }
        }
    }

    override fun clearHiddenEntries() {
        hiddenEntries.update { emptySet() }
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

        tabBarViewModel.openOrActivate(FIND_ANIME_DETAILS)
    }

    override fun findRelatedAnime(link: LinkEntry) {
        if (link == NoLink) return

        CoroutineScope(Default).launch {
            app.findRelatedAnime(listOf(link.asLink().uri))
        }

        tabBarViewModel.openOrActivate(FIND_RELATED_ANIME)
    }

    override fun findSimilarAnime(link: LinkEntry) {
        if (link == NoLink) return

        CoroutineScope(Default).launch {
            app.findSimilarAnime(link.asLink().uri)
        }

        tabBarViewModel.openOrActivate(FIND_SIMILAR_ANIME)
    }

    override fun isSortable(value: Boolean) {
        isSortable = value
    }

    override fun openDirectory(anime: T) {
        throw UnsupportedOperationException()
    }

    override fun saveScrollPosition() {
        lastIndex = listState.firstVisibleItemIndex
        lastOffset = listState.firstVisibleItemScrollOffset
    }

    override suspend fun restoreScrollPosition() {
        listState.scrollToItem(lastIndex, lastOffset)
    }
}