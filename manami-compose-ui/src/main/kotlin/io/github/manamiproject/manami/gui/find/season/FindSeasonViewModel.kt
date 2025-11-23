package io.github.manamiproject.manami.gui.find.season

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.events.SearchResultAnimeEntry
import io.github.manamiproject.manami.gui.components.animetable.DefaultAnimeTableViewModel
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.YEAR_OF_THE_FIRST_ANIME
import io.github.manamiproject.modb.core.anime.Year
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.extensions.EMPTY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

internal class FindSeasonViewModel(private val app: Manami = Manami.instance): DefaultAnimeTableViewModel<SearchResultAnimeEntry>() {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    val isSeasonSearchRunning: StateFlow<Boolean>
        get() = app.findSeasonState
            .map { it.isRunning.also { isRunning -> if (isRunning) clearHiddenEntries() } }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = false,
            )

    val metaDataProviders: StateFlow<List<Hostname>>
        get() = app.dashboardState
            .map { event -> event.entries.toList().sortedByDescending { it.second }.map { it.first } }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    override val source: StateFlow<List<SearchResultAnimeEntry>>
        get() = app.findSeasonState
            .map { it.entries.toList() }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    var metaDataProviderText by mutableStateOf(EMPTY)
    var seasonSelectedText by mutableStateOf(currentSeason())
    var yearSelectedText by mutableStateOf(LocalDate.now().year)

    override fun delete(anime: SearchResultAnimeEntry) = throw UnsupportedOperationException()

    fun yearRange(): List<Year> = (YEAR_OF_THE_FIRST_ANIME..LocalDate.now().year + 5).toList().reversed()

    fun seasons() = listOf("Spring", "Summer", "Fall", "Winter")

    fun currentSeason(): String {
        return when(LocalDate.now().month.value) {
            1, 2, 3 -> "Winter"
            4, 5, 6 -> "Spring"
            7, 8, 9 -> "Summer"
            else -> "Fall"
        }
    }

    fun search(metaDataProvider: Hostname, season: String, year: Year) {
        val animeSeason = AnimeSeason(
            season = AnimeSeason.Season.of(season),
            year = year,
        )

        viewModelScope.launch {
            app.findSeason(animeSeason, metaDataProvider)
        }
    }

    internal companion object {
        /**
         * Singleton of [FindSeasonViewModel]
         * @since 4.0.0
         */
        val instance: FindSeasonViewModel by lazy { FindSeasonViewModel() }
    }
}