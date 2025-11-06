package io.github.manamiproject.manami.app.events

import io.github.manamiproject.modb.core.anime.Anime

/**
 * @since 4.0.0
 */
data class FindAnimeState(
    val isRunning: Map<RequestOrigin, Boolean> = emptyMap(),
    val entries: Map<RequestOrigin, Anime> = emptyMap(),
)