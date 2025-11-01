package io.github.manamiproject.manami.gui.lists.animelist

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.gui.components.animetable.DefaultAnimeTableViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class AnimeListViewModel(private val app: Manami = Manami.instance): DefaultAnimeTableViewModel<AnimeListEntry>() {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    override val source: StateFlow<List<AnimeListEntry>>
        get() = app.animeListState
            .map { it.entries.toList() }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    override fun delete(anime: AnimeListEntry) {
        viewModelScope.launch {
            app.removeAnimeListEntry(anime)
        }
    }

    internal companion object {
        /**
         * Singleton of [AnimeListViewModel]
         * @since 4.0.0
         */
        val instance: AnimeListViewModel by lazy { AnimeListViewModel() }
    }
}