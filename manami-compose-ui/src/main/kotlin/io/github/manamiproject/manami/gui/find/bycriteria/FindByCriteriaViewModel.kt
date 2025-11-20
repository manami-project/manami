package io.github.manamiproject.manami.gui.find.bycriteria

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig
import io.github.manamiproject.manami.gui.tabs.TabBarViewModel
import io.github.manamiproject.manami.gui.tabs.Tabs.SEARCH_RESULTS
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.HOURS
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.SECONDS
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

internal class FindByCriteriaViewModel(
    private val app: Manami = Manami.instance,
    private val tabBarViewModel: TabBarViewModel = TabBarViewModel.instance,
) {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())
    private var _scrollOffset = 0
    val scrollOffset: Int
        get() = _scrollOffset

    val metaDataProviders: StateFlow<List<Hostname>>
        get() = app.dashboardState
            .map { event -> event.entries.toList().sortedByDescending { it.second }.map { it.first } }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    var metaDataProviderText by mutableStateOf(EMPTY)

    val availableTags: List<Tag> = app.availableTags().toList().sorted()

    val availableStudios: List<Tag> = app.availableStudios().toList().sorted()

    val availableProducers: List<Tag> = app.availableProducers().toList().sorted()

    fun saveScrollPosition(offset: Int) {
        _scrollOffset = offset
    }

    fun search(
        metaDataProvider: String,
        types: Set<String>,
        status: Set<String>,
        seasons: Set<String>,
        episodesMin: String,
        episodesMax: String,
        yearMin: String,
        yearMax: String,
        durationMin: String,
        durationMax: String,
        durationUnit: String,
        scoreMin: String,
        scoreMax: String,
        scoreType: String,
        studios: Set<Studio>,
        producers: Set<Producer>,
        tags: Set<Tag>,
        studiosConjunction: FindByCriteriaConfig.SearchConjunction,
        producersConjunction: FindByCriteriaConfig.SearchConjunction,
        tagsConjunction: FindByCriteriaConfig.SearchConjunction,
    ) {
        viewModelScope.launch {
            val castedEpisodesMin = episodesMin.toIntOrNull() ?: -1
            val castedEpisodesMax = episodesMax.toIntOrNull() ?: -1

            val castedYearMin = yearMin.toIntOrNull() ?: -1
            val castedYearMax = yearMax.toIntOrNull() ?: -1

            var castedDurationMin = durationMin.toIntOrNull() ?: -1
            var castedDurationMax = durationMax.toIntOrNull() ?: -1
            val castedDurationUnit = Duration.TimeUnit.valueOf(durationUnit.uppercase())
            when (castedDurationUnit) {
                HOURS -> {
                    if (castedDurationMin != -1) castedDurationMin *= 3600
                    if (castedDurationMax != -1) castedDurationMax *= 3600
                }
                MINUTES -> {
                    if (castedDurationMin != -1) castedDurationMin *= 60
                    if (castedDurationMax != -1) castedDurationMax *= 60
                }
                SECONDS -> {}
            }

            val castedScoreMin = scoreMin.toIntOrNull() ?: -1
            val castedScoreMax = scoreMax.toIntOrNull() ?: -1
            val castedScoreType = FindByCriteriaConfig.ScoreType.of(scoreType)

            val config = FindByCriteriaConfig(
                metaDataProvider = metaDataProvider,
                types = types.map { AnimeType.of(it) }.toSet(),
                status = status.map { AnimeStatus.of(it) }.toSet(),
                seasons = seasons.map { AnimeSeason.Season.of(it) }.toSet(),
                episodes = castedEpisodesMin..castedEpisodesMax,
                year = castedYearMin..castedYearMax,
                durationInSeconds = castedDurationMin..castedDurationMax,
                score = castedScoreMin..castedScoreMax,
                scoreType = castedScoreType,
                studios = studios,
                studiosConjunction = studiosConjunction,
                producers = producers,
                producersConjunction = producersConjunction,
                tags = tags,
                tagsConjunction = tagsConjunction,
            )
            app.findByCriteria(config)
            tabBarViewModel.openOrActivate(SEARCH_RESULTS)
        }
    }

    internal companion object {
        /**
         * Singleton of [FindByCriteriaViewModel]
         * @since 4.0.0
         */
        val instance: FindByCriteriaViewModel by lazy { FindByCriteriaViewModel() }
    }
}