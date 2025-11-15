package io.github.manamiproject.manami.gui.find.relatedanime

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.events.SearchResultAnimeEntry
import io.github.manamiproject.manami.gui.components.animetable.DefaultAnimeTableViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class FindRelatedAnimeViewModel(private val app: Manami = Manami.instance): DefaultAnimeTableViewModel<SearchResultAnimeEntry>() {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    val isRelatedAnimeSearchRunning: StateFlow<Boolean>
        get() = app.findRelatedAnimeState
            .map { it.isRunning.also { isRunning -> if (isRunning) clearHiddenEntries() } }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = false,
            )

    override val source: StateFlow<List<SearchResultAnimeEntry>>
        get() = app.findRelatedAnimeState
            .map { it.entries }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    override fun delete(anime: SearchResultAnimeEntry) = throw UnsupportedOperationException()

    internal companion object {
        /**
         * Singleton of [FindRelatedAnimeViewModel]
         * @since 4.0.0
         */
        val instance: FindRelatedAnimeViewModel by lazy { FindRelatedAnimeViewModel() }
    }
}