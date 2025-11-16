package io.github.manamiproject.manami.gui.find.inconsistencies

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.manamiproject.manami.app.Manami
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class FindInconsistenciesViewModel(private val app: Manami = Manami.instance) {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    private var _isShowMetaDataDiffResults by mutableStateOf(false)
    val isShowMetaDataDiffResults: State<Boolean>
        get() = derivedStateOf { _isShowMetaDataDiffResults }

    private var _isShowEpisodesDiffResults by mutableStateOf(false)
    val isShowEpisodeDiffResults: State<Boolean>
        get() = derivedStateOf { _isShowEpisodesDiffResults }

    private var _isShowDeadEntryResults by mutableStateOf(false)
    val isShowDeadEntryResults: State<Boolean>
        get() = derivedStateOf { _isShowDeadEntryResults }

    val isRunning: StateFlow<Boolean>
        get() = app.inconsistenciesState
            .map {
                it.isRunning.also {
                    MetaDataDiffResultsViewModel.instance.clearHiddenEntries()
                    EpisodeDiffResultsViewModel.instance.clearHiddenEntries()
                    DeadEntryResultsViewModel.instance.clearHiddenEntries()
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = false,
            )

    val numberOfMetaDataDiffs: StateFlow<Int>
        get() = app.inconsistenciesState
            .map { it.animeListMetaDataInconsistencies.size }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = 0,
            )

    val numberOfEpisodesDiffs: StateFlow<Int>
        get() = app.inconsistenciesState
            .map { it.animeListEpisodesInconsistencies.size }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = 0,
            )

    val numberOfDeadEntries: StateFlow<Int>
        get() = app.inconsistenciesState
            .map { it.animeListDeadEntriesInconsistencies.size }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = 0,
            )

    fun findInconsistencies() {
        viewModelScope.launch {
            app.findInconsistencies()
        }
    }

    fun showMetaDataDiffResults() {
        _isShowMetaDataDiffResults = true
        _isShowEpisodesDiffResults = false
        _isShowDeadEntryResults = false
    }

    fun showEpisodesDiffResults() {
        _isShowMetaDataDiffResults = false
        _isShowEpisodesDiffResults = true
        _isShowDeadEntryResults = false
    }

    fun showDeadEntryResults() {
        _isShowMetaDataDiffResults = false
        _isShowEpisodesDiffResults = false
        _isShowDeadEntryResults = true
    }

    internal companion object {
        /**
         * Singleton of [FindInconsistenciesViewModel]
         * @since 4.0.0
         */
        val instance: FindInconsistenciesViewModel by lazy { FindInconsistenciesViewModel() }
    }
}