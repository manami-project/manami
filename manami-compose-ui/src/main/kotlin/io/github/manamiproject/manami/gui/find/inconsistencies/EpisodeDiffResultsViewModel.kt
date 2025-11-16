package io.github.manamiproject.manami.gui.find.inconsistencies

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.NoFile
import io.github.manamiproject.manami.gui.components.animetable.DefaultAnimeTableViewModel
import io.github.manamiproject.manami.gui.extensions.toOnClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class EpisodeDiffResultsViewModel(private val app: Manami = Manami.instance): DefaultAnimeTableViewModel<AnimeListEntry>()  {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    override val source: StateFlow<List<AnimeListEntry>>
        get() = app.inconsistenciesState
            .map { event ->
                event.animeListEpisodesInconsistencies
                    .map { it.animeListEntry.copy(title = "[Expected ${it.animeListEntry.episodes} | Files ${it.numberOfFiles}] ${it.animeListEntry.title}") }
            }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    override fun delete(anime: AnimeListEntry) = throw UnsupportedOperationException()

    override fun openDirectory(anime: AnimeListEntry) {
        when(val openedFile = app.generalAppState.value.openedFile) {
            NoFile -> anime.location.toUri().toOnClick().invoke()
            is CurrentFile -> openedFile.regularFile.parent.resolve(anime.location).toUri().toOnClick().invoke()
        }
    }

    internal companion object {
        /**
         * Singleton of [EpisodeDiffResultsViewModel]
         * @since 4.0.0
         */
        val instance: EpisodeDiffResultsViewModel by lazy { EpisodeDiffResultsViewModel() }
    }
}
