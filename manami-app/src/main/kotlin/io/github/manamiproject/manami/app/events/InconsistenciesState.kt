package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.AnimeListEpisodesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistenciesResult

/**
 * @since 4.0.0
 */
data class InconsistenciesState(
    val isRunning: Boolean = false,
    val metaDataInconsistencies: MetaDataInconsistenciesResult = MetaDataInconsistenciesResult(),
    val deadEntryInconsistencies: DeadEntriesInconsistenciesResult = DeadEntriesInconsistenciesResult(),
    val animeListMetaDataInconsistencies: AnimeListMetaDataInconsistenciesResult = AnimeListMetaDataInconsistenciesResult(),
    val animeListDeadEntriesInconsistencies: AnimeListDeadEntriesInconsistenciesResult = AnimeListDeadEntriesInconsistenciesResult(),
    val animeListEpisodesInconsistencies: AnimeListEpisodesInconsistenciesResult = AnimeListEpisodesInconsistenciesResult(),
)
