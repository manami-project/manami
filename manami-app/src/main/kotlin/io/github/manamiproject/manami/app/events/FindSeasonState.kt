package io.github.manamiproject.manami.app.events

/**
 * @since 4.0.0
 */
data class FindSeasonState(
    val isRunning: Boolean = false,
    val entries: Collection<SearchResultAnimeEntry> = emptyList(),
)
