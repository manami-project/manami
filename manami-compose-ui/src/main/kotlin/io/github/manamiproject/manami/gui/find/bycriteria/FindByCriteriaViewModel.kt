package io.github.manamiproject.manami.gui.find.bycriteria

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.AND
import io.github.manamiproject.manami.gui.extensions.capitalize
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

    private var firstValueMetaDataProviders = EMPTY
    val metaDataProviders: StateFlow<List<Hostname>>
        get() = app.dashboardState
            .map { event ->
                val entries = event.entries.toList().sortedByDescending { it.second }.map { it.first }
                firstValueMetaDataProviders = entries.first()
                entries
            }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    val availableTags: List<Tag> = app.availableTags().toList().sorted()
    val availableStudios: List<Studio> = app.availableStudios().toList().sorted()
    val availableProducers: List<Producer> = app.availableProducers().toList().sorted()

    var metaDataProviderText by mutableStateOf(EMPTY)
    val selectedTypes = mutableStateListOf<String>()
    val selectedStatus = mutableStateListOf<String>()
    val selectedSeasons = mutableStateListOf<String>()
    val selectedMinEpisodes = mutableStateOf(EMPTY)
    val selectedMaxEpisodes = mutableStateOf(EMPTY)
    val selectedMinYear = mutableStateOf(EMPTY)
    val selectedMaxYear = mutableStateOf(EMPTY)
    val selectedMinDuration = mutableStateOf(EMPTY)
    val selectedMaxDuration = mutableStateOf(EMPTY)
    var durationUnitSelectText by mutableStateOf(Duration.TimeUnit.entries.first().toString().capitalize())
    val selectedMinScore = mutableStateOf(EMPTY)
    val selectedMaxScore = mutableStateOf(EMPTY)
    var scoreTypeSelectText by mutableStateOf(FindByCriteriaConfig.ScoreType.entries.map { it.viewName }.first())
    val selectedStudios = mutableStateListOf<Studio>()
    val selectedSearchConjunctionStudios = mutableStateOf(AND)
    val selectedProducers = mutableStateListOf<Producer>()
    val selectedSearchConjunctionProducers = mutableStateOf(AND)
    val selectedTags = mutableStateListOf<Tag>()
    val selectedSearchConjunctionTags = mutableStateOf(AND)

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

    fun reset() {
        metaDataProviderText = firstValueMetaDataProviders
        selectedTypes.clear()
        selectedStatus.clear()
        selectedSeasons.clear()
        selectedMinEpisodes.value = EMPTY
        selectedMaxEpisodes.value = EMPTY
        selectedMinYear.value = EMPTY
        selectedMaxYear.value = EMPTY
        selectedMinDuration.value = EMPTY
        selectedMaxDuration.value = EMPTY
        durationUnitSelectText = Duration.TimeUnit.entries.first().toString().capitalize()
        selectedMinScore.value = EMPTY
        selectedMaxScore.value = EMPTY
        scoreTypeSelectText = FindByCriteriaConfig.ScoreType.entries.map { it.viewName }.first()
        selectedStudios.clear()
        selectedSearchConjunctionStudios.value = AND
        selectedProducers.clear()
        selectedSearchConjunctionProducers.value = AND
        selectedTags.clear()
        selectedSearchConjunctionTags.value = AND
    }

    internal companion object {
        /**
         * Singleton of [FindByCriteriaViewModel]
         * @since 4.0.0
         */
        val instance: FindByCriteriaViewModel by lazy { FindByCriteriaViewModel() }
    }
}