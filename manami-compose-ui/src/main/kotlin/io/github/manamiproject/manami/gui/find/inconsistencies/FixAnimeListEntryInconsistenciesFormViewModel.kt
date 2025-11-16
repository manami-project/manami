package io.github.manamiproject.manami.gui.find.inconsistencies

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.gui.find.inconsistencies.FixSelection.*
import io.github.manamiproject.modb.core.anime.AnimeType
import io.github.manamiproject.modb.core.anime.Title
import io.github.manamiproject.modb.core.extensions.EMPTY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class FixAnimeListEntryInconsistenciesFormViewModel(private val app: Manami = Manami.instance) {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    private var _titleSelection: FixSelection by mutableStateOf(CURRENT)
    val titleSelection = derivedStateOf { _titleSelection }

    val titleCurrentValue: StateFlow<Title>
        get() = app.fixAnimeListInconsistencyModificationState
            .map { it?.currentEntry?.title ?: EMPTY }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = EMPTY,
            )

    val titleReplacementValue: StateFlow<Title>
        get() = app.fixAnimeListInconsistencyModificationState
            .map { it?.replacementEntry?.title ?: EMPTY }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = EMPTY,
            )

    private var _episodesSelection: FixSelection by mutableStateOf(CURRENT)
    val episodesSelection = derivedStateOf { _episodesSelection }

    val episodesCurrentValue: StateFlow<String>
        get() = app.fixAnimeListInconsistencyModificationState
            .map { it?.currentEntry?.episodes?.toString() ?: "0" }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = "0",
            )

    val episodesReplacementValue: StateFlow<String>
        get() = app.fixAnimeListInconsistencyModificationState
            .map { it?.currentEntry?.episodes?.toString() ?: "0" }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = "0",
            )

    private var _typeSelection: FixSelection by mutableStateOf(CURRENT)
    val typeSelection = derivedStateOf { _typeSelection }

    val typeCurrentValue: StateFlow<String>
        get() = app.fixAnimeListInconsistencyModificationState
            .map { it?.currentEntry?.type?.toString() ?: EMPTY }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = EMPTY,
            )

    val typeReplacementValue: StateFlow<String>
        get() = app.fixAnimeListInconsistencyModificationState
            .map { it?.currentEntry?.type?.toString() ?: EMPTY }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = EMPTY,
            )

    fun selectTitle(selection: FixSelection) {
        _titleSelection = selection
    }

    fun selectEpisodes(selection: FixSelection) {
        _episodesSelection = selection
    }

    fun selectType(selection: FixSelection) {
        _typeSelection = selection
    }

    fun update() {
        val currentEntry = app.fixAnimeListInconsistencyModificationState.value?.currentEntry ?: return
        val replacementEntry = app.fixAnimeListInconsistencyModificationState.value?.replacementEntry ?: return

        val replaceWith = currentEntry.copy(
            title = if (titleSelection.value == CURRENT) currentEntry.title else replacementEntry.title,
            episodes = if (episodesSelection.value == CURRENT) currentEntry.episodes else replacementEntry.episodes,
            type = if (typeSelection.value == CURRENT) currentEntry.type else replacementEntry.type,
        )

        app.fixAnimeListEntryMetaDataInconsistencies(currentEntry, replaceWith)
    }

    internal companion object {
        /**
         * Singleton of [FixAnimeListEntryInconsistenciesFormViewModel]
         * @since 4.0.0
         */
        val instance: FixAnimeListEntryInconsistenciesFormViewModel by lazy { FixAnimeListEntryInconsistenciesFormViewModel() }
    }
}

internal enum class FixSelection {
    CURRENT,
    REPLACEMENT;
}