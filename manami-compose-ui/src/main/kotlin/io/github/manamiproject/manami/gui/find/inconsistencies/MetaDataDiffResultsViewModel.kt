package io.github.manamiproject.manami.gui.find.inconsistencies

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.NoFile
import io.github.manamiproject.manami.gui.components.animetable.DefaultAnimeTableViewModel
import io.github.manamiproject.manami.gui.extensions.toOnClick
import io.github.manamiproject.manami.gui.tabs.TabBarViewModel
import io.github.manamiproject.manami.gui.tabs.Tabs.FIX_ANIME_LIST_ENTRY_INCONSISTENCIES_FORM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class MetaDataDiffResultsViewModel(
    private val app: Manami = Manami.instance,
    private val tabBarViewModel: TabBarViewModel = TabBarViewModel.instance,
): DefaultAnimeTableViewModel<AnimeListEntry>()  {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    override val source: StateFlow<List<AnimeListEntry>>
        get() = app.inconsistenciesState
            .map { event -> event.animeListMetaDataInconsistencies.map { it.currentEntry } }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    override fun delete(anime: AnimeListEntry) = throw UnsupportedOperationException()

    override fun editAnimeListEntry(anime: AnimeListEntry) {
        val diff = app.inconsistenciesState.value.animeListMetaDataInconsistencies.first { it.currentEntry == anime }
        viewModelScope.launch {
            app.setForEdit(diff)
        }
        tabBarViewModel.openOrActivate(FIX_ANIME_LIST_ENTRY_INCONSISTENCIES_FORM)
    }

    override fun openDirectory(anime: AnimeListEntry) {
        when(val openedFile = app.generalAppState.value.openedFile) {
            NoFile -> anime.location.toUri().toOnClick().invoke()
            is CurrentFile -> openedFile.regularFile.parent.resolve(anime.location).toUri().toOnClick().invoke()
        }
    }

    internal companion object {
        /**
         * Singleton of [MetaDataDiffResultsViewModel]
         * @since 4.0.0
         */
        val instance: MetaDataDiffResultsViewModel by lazy { MetaDataDiffResultsViewModel() }
    }
}
