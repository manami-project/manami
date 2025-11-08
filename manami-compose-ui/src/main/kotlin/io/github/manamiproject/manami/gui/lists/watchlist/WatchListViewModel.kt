package io.github.manamiproject.manami.gui.lists.watchlist

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.gui.components.animetable.DefaultAnimeTableViewModel
import io.github.manamiproject.manami.gui.tabs.TabBarViewModel
import io.github.manamiproject.manami.gui.tabs.Tabs.FIND_RELATED_ANIME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class WatchListViewModel(
    private val app: Manami = Manami.instance,
    private val tabBarViewModel: TabBarViewModel = TabBarViewModel.instance,
): DefaultAnimeTableViewModel<WatchListEntry>() {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    override val source: StateFlow<List<WatchListEntry>>
        get() = app.watchListState
                .map { it.entries.toList() }
                .stateIn(
                    scope = viewModelScope,
                    started = Eagerly,
                    initialValue = emptyList(),
                )

    override fun delete(anime: WatchListEntry) {
        viewModelScope.launch {
            app.removeWatchListEntry(anime)
        }
    }

    fun findRelatedAnime() {
        viewModelScope.launch {
            app.findRelatedAnime(app.watchList().map { it.link.uri })
        }

        tabBarViewModel.openOrActivate(FIND_RELATED_ANIME)
    }

    internal companion object {
        /**
         * Singleton of [WatchListViewModel]
         * @since 4.0.0
         */
        val instance: WatchListViewModel by lazy { WatchListViewModel() }
    }
}