package io.github.manamiproject.manami.app.events

/**
 * @since 4.0.0
 */
data class FindRelatedAnimeState(
    val isRunning: Boolean = false,
    val entries: List<SearchResultAnimeEntry> = emptyList(),
)