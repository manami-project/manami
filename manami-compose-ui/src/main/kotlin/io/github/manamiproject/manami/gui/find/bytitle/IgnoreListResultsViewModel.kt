package io.github.manamiproject.manami.gui.find.bytitle

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.gui.components.animetable.DefaultAnimeTableViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class IgnoreListResultsViewModel(private val app: Manami = Manami.instance): DefaultAnimeTableViewModel<IgnoreListEntry>() {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    override val source: StateFlow<List<IgnoreListEntry>>
        get() = app.findByTitleState
            .map { it.ignoreListResults.toList() }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    override fun delete(anime: IgnoreListEntry) = throw UnsupportedOperationException()

    internal companion object {
        /**
         * Singleton of [IgnoreListResultsViewModel]
         * @since 4.0.0
         */
        val instance: IgnoreListResultsViewModel by lazy { IgnoreListResultsViewModel() }
    }
}