package io.github.manamiproject.manami.gui.lists.watchlist

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.gui.components.animetable.DefaultAnimeTableViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class WatchListViewModel(private val app: Manami = Manami.instance): DefaultAnimeTableViewModel<WatchListEntry>() {

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

    internal companion object {
        /**
         * Singleton of [WatchListViewModel]
         * @since 4.0.0
         */
        val instance: WatchListViewModel by lazy { WatchListViewModel() }
    }
}