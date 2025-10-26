package io.github.manamiproject.manami.app.events

import io.github.manamiproject.modb.core.anime.Anime

/**
 * @since 4.0.0
 */
data class RelatedAnimeState(
    val isForAnimeListRunning: Boolean = false,
    val forAnimeList: Collection<Anime> = emptyList(),
    val isForIgnoreListRunning: Boolean = false,
    val forIgnoreList: Collection<Anime> = emptyList(),
)