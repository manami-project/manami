package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.EpisodeDiff
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataDiff
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.modb.test.shouldNotBeInvoked

internal object TestInconsistencyHandler: InconsistencyHandler<Unit> {
    override suspend fun execute(): Unit = shouldNotBeInvoked()
}

internal object TestAnimeListMetaDataInconsistencyHandler: InconsistencyHandler<List<AnimeListMetaDataDiff>> {
    override suspend fun execute(): List<AnimeListMetaDataDiff> = shouldNotBeInvoked()
}

internal object TestAnimeListEpisodesInconsistenciesHandler: InconsistencyHandler<List<EpisodeDiff>> {
    override suspend fun execute(): List<EpisodeDiff> = shouldNotBeInvoked()
}

internal object TestAnimeListDeadEntriesInconsistenciesHandler: InconsistencyHandler<List<AnimeListEntry>> {
    override suspend fun execute(): List<AnimeListEntry> = shouldNotBeInvoked()
}
