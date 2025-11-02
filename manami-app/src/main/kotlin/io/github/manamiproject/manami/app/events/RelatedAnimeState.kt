package io.github.manamiproject.manami.app.events

/**
 * @since 4.0.0
 */
data class RelatedAnimeState(
    val isForAnimeListRunning: Boolean = false,
    val forAnimeList: Collection<SearchResultAnimeEntry> = emptyList(),
    val isForIgnoreListRunning: Boolean = false,
    val forIgnoreList: Collection<SearchResultAnimeEntry> = emptyList(),
)