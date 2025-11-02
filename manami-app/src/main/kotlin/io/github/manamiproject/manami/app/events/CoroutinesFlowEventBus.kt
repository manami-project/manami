package io.github.manamiproject.manami.app.events

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * @since 4.0.0
 */
object CoroutinesFlowEventBus: EventBus {

    override val dashboardState: MutableStateFlow<DashboardState> = MutableStateFlow(DashboardState())
    override val generalAppState: MutableStateFlow<GeneralAppState> = MutableStateFlow(GeneralAppState())
    override val animeListState: MutableStateFlow<AnimeListState> = MutableStateFlow(AnimeListState())
    override val watchListState: MutableStateFlow<WatchListState> = MutableStateFlow(WatchListState())
    override val ignoreListState: MutableStateFlow<IgnoreListState> = MutableStateFlow(IgnoreListState())
    override val inconsistenciesState: MutableStateFlow<InconsistenciesState> = MutableStateFlow(InconsistenciesState())
    override val metaDataProviderMigrationState: MutableStateFlow<MetaDataProviderMigrationState> = MutableStateFlow(MetaDataProviderMigrationState())
    override val findRelatedAnimeState: MutableStateFlow<RelatedAnimeState> = MutableStateFlow(RelatedAnimeState())
    override val findInListState: MutableStateFlow<FindInListState> = MutableStateFlow(FindInListState())
    override val findSeasonState: MutableStateFlow<FindSeasonState> = MutableStateFlow(FindSeasonState())
    override val findByTagState: MutableStateFlow<FindByTagState> = MutableStateFlow(FindByTagState())
    override val findSimilarAnimeState: MutableStateFlow<FindSimilarAnimeState> = MutableStateFlow(FindSimilarAnimeState())
    override val findAnimeState: MutableStateFlow<FindAnimeState> = MutableStateFlow(FindAnimeState())

    override fun clear() {
        dashboardState.update { DashboardState() }
        generalAppState.update { GeneralAppState() }
        animeListState.update { AnimeListState() }
        watchListState.update { WatchListState() }
        ignoreListState.update { IgnoreListState() }
        inconsistenciesState.update { InconsistenciesState() }
        metaDataProviderMigrationState.update { MetaDataProviderMigrationState() }
        findRelatedAnimeState.update { RelatedAnimeState() }
        findInListState.update { FindInListState() }
        findSeasonState.update { FindSeasonState() }
        findByTagState.update { FindByTagState() }
        findSimilarAnimeState.update { FindSimilarAnimeState() }
        findAnimeState.update { FindAnimeState() }
    }
}