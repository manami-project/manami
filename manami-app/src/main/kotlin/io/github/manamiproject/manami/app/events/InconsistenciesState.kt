package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.EpisodeDiff
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataDiff
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry

/**
 * @since 4.0.0
 */
data class InconsistenciesState(
    val isRunning: Boolean = false,
    val animeListMetaDataInconsistencies: List<AnimeListMetaDataDiff> = emptyList(),
    val animeListDeadEntriesInconsistencies: List<AnimeListEntry> = emptyList(),
    val animeListEpisodesInconsistencies: List<EpisodeDiff> = emptyList(),
)
