package io.github.manamiproject.manami.app.events

import io.github.manamiproject.modb.core.anime.Anime

/**
 * @since 4.0.0
 */
data class FindSeasonState(
    val isRunning: Boolean = false,
    val entries: Collection<Anime> = emptyList(),
)
