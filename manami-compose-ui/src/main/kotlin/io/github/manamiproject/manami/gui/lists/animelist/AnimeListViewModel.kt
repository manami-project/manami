package io.github.manamiproject.manami.gui.lists.animelist

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.NoFile
import io.github.manamiproject.manami.gui.components.animetable.DefaultAnimeTableViewModel
import io.github.manamiproject.manami.gui.extensions.toOnClick
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

internal class AnimeListViewModel(
    private val app: Manami = Manami.instance,
    private val tabBarViewModel: TabBarViewModel = TabBarViewModel.instance,
): DefaultAnimeTableViewModel<AnimeListEntry>() {

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

    override fun openDirectory(anime: AnimeListEntry) {
        when(val openedFile = app.generalAppState.value.openedFile) {
            NoFile -> anime.location.toUri().toOnClick().invoke()
            is CurrentFile -> openedFile.regularFile.parent.resolve(anime.location).toUri().toOnClick().invoke()
        }
    }

    fun findRelatedAnime() {
        viewModelScope.launch {
            app.findRelatedAnime(app.animeList().map { it.link }.filterIsInstance<Link>().map { it.uri })
        }

        tabBarViewModel.openOrActivate(FIND_RELATED_ANIME)
    }

    internal companion object {
        /**
         * Singleton of [AnimeListViewModel]
         * @since 4.0.0
         */
        val instance: AnimeListViewModel by lazy { AnimeListViewModel() }
    }
}