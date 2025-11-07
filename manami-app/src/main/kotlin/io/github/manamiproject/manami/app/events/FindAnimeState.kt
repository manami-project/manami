package io.github.manamiproject.manami.app.events

import io.github.manamiproject.modb.core.anime.Anime

/**
 * @since 4.0.0
 */
data class FindAnimeState(
    val isRunning: Boolean = false,
    val entry: Anime? = null,
)