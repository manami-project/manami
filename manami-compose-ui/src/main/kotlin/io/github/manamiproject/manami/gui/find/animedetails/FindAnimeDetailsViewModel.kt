package io.github.manamiproject.manami.gui.find.animedetails

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.modb.core.anime.Anime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class FindAnimeDetailsViewModel(private val app: Manami = Manami.instance) {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    val isAnimeDetailsRunning: StateFlow<Boolean>
        get() = app.findAnimeDetailsState
            .map { it.isRunning }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = false,
            )

    val animeDetails: StateFlow<Anime?>
        get() = app.findAnimeDetailsState
            .map { it.entry }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = null,
            )

    internal companion object {
        /**
         * Singleton of [FindAnimeDetailsViewModel]
         * @since 4.0.0
         */
        val instance: FindAnimeDetailsViewModel by lazy { FindAnimeDetailsViewModel() }
    }
}