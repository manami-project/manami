package io.github.manamiproject.manami.app.events

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * @since 4.0.0
 */
interface EventBus {

    /**
     * @since 4.0.0
     */
    val dashboardState: MutableStateFlow<DashboardState>

    /**
     * @since 4.0.0
     */
    val generalAppState: MutableStateFlow<GeneralAppState>

    /**
     * @since 4.0.0
     */
    val animeListState: MutableStateFlow<AnimeListState>

    /**
     * @since 4.0.0
     */
    val watchListState: MutableStateFlow<WatchListState>

    /**
     * @since 4.0.0
     */
    val ignoreListState: MutableStateFlow<IgnoreListState>

    /**
     * @since 4.0.0
     */
    val inconsistenciesState: MutableStateFlow<InconsistenciesState>

    /**
     * @since 4.0.0
     */
    val metaDataProviderMigrationState: MutableStateFlow<MetaDataProviderMigrationState>

    /**
     * @since 4.0.0
     */
    val relatedAnimeState: MutableStateFlow<RelatedAnimeState>

    /**
     * @since 4.0.0
     */
    val findInListState: MutableStateFlow<FindInListState>

    /**
     * @since 4.0.0
     */
    val findSeasonState: MutableStateFlow<FindSeasonState>

    /**
     * @since 4.0.0
     */
    val findByTagState: MutableStateFlow<FindByTagState>

    /**
     * @since 4.0.0
     */
    val findSimilarAnimeState: MutableStateFlow<FindSimilarAnimeState>

    /**
     * @since 4.0.0
     */
    val findAnimeState: MutableStateFlow<FindAnimeState>

    /**
     * @since 4.0.0
     */
    fun clear()
}