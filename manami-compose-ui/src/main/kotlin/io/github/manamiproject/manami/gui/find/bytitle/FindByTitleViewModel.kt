package io.github.manamiproject.manami.gui.find.bytitle

import androidx.compose.runtime.*
import io.github.manamiproject.manami.app.Manami
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class FindByTitleViewModel(private val app: Manami = Manami.instance) {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    private var _isShowAnimeListResults by mutableStateOf(false)
    val isShowAnimeListResults: State<Boolean>
        get() = derivedStateOf { _isShowAnimeListResults }

    private var _isShowWatchListResults by mutableStateOf(false)
    val isShowWatchListResults: State<Boolean>
        get() = derivedStateOf { _isShowWatchListResults }

    private var _isShowIgnoreListResults by mutableStateOf(false)
    val isShowIgnoreListResults: State<Boolean>
        get() = derivedStateOf { _isShowIgnoreListResults }

    private var _isShowUnlistResults by mutableStateOf(false)
    val isShowUnlistResults: State<Boolean>
        get() = derivedStateOf { _isShowUnlistResults }

    val numberOfAnimeListResults: StateFlow<Int>
        get() = app.findByTitleState
            .map { event ->
                event.animeListResults.size.also {
                    if (it > 0) _isShowAnimeListResults = true
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = 0,
            )

    val numberOfWatchListResults: StateFlow<Int>
        get() = app.findByTitleState
            .map { event ->
                event.watchListResults.size.also {
                    if (!_isShowAnimeListResults && it > 0) _isShowWatchListResults = true
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = 0,
            )

    val numberOfIgnoreListResults: StateFlow<Int>
        get() = app.findByTitleState
            .map { event ->
                event.ignoreListResults.size.also {
                    if (!_isShowAnimeListResults && !_isShowWatchListResults && it > 0) _isShowIgnoreListResults = true
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = 0,
            )

    val numberOfUnlistedResults: StateFlow<Int>
        get() = app.findByTitleState
            .map { event ->
                event.unlistedResults.size.also {
                    if (!_isShowAnimeListResults && !_isShowWatchListResults && !_isShowIgnoreListResults && it > 0) _isShowUnlistResults = true
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = 0,
            )

    fun showAnimeListResults() {
        _isShowAnimeListResults = true
        _isShowWatchListResults = false
        _isShowIgnoreListResults = false
        _isShowUnlistResults = false
    }

    fun showWatchListResults() {
        _isShowAnimeListResults = false
        _isShowWatchListResults = true
        _isShowIgnoreListResults = false
        _isShowUnlistResults = false
    }

    fun showIgnoreListResults() {
        _isShowAnimeListResults = false
        _isShowWatchListResults = false
        _isShowIgnoreListResults = true
        _isShowUnlistResults = false
    }

    fun showUnlistResults() {
        _isShowAnimeListResults = false
        _isShowWatchListResults = false
        _isShowIgnoreListResults = false
        _isShowUnlistResults = true
    }

    internal companion object {
        /**
         * Singleton of [FindByTitleViewModel]
         * @since 4.0.0
         */
        val instance: FindByTitleViewModel by lazy { FindByTitleViewModel() }
    }
}