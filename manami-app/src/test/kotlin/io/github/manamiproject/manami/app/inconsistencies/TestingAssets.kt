package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.AnimeListEpisodesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistenciesResult
import io.github.manamiproject.modb.test.shouldNotBeInvoked

internal object TestInconsistencyHandler: InconsistencyHandler<String> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(): String = shouldNotBeInvoked()
    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = shouldNotBeInvoked()
}

internal object TestMetaDataInconsistencyHandler: InconsistencyHandler<MetaDataInconsistenciesResult> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(): MetaDataInconsistenciesResult = shouldNotBeInvoked()
    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = shouldNotBeInvoked()
}

internal object TestDeadEntriesInconsistencyHandler: InconsistencyHandler<DeadEntriesInconsistenciesResult> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(): DeadEntriesInconsistenciesResult = shouldNotBeInvoked()
    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = shouldNotBeInvoked()
}

internal object TestAnimeListMetaDataInconsistencyHandler: InconsistencyHandler<AnimeListMetaDataInconsistenciesResult> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(): AnimeListMetaDataInconsistenciesResult = shouldNotBeInvoked()
    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = shouldNotBeInvoked()
}

internal object TestAnimeListEpisodesInconsistenciesHandler: InconsistencyHandler<AnimeListEpisodesInconsistenciesResult> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(): AnimeListEpisodesInconsistenciesResult = shouldNotBeInvoked()
    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = shouldNotBeInvoked()
}

internal object TestAnimeListDeadEntriesInconsistenciesHandler: InconsistencyHandler<AnimeListDeadEntriesInconsistenciesResult> {
    override fun calculateWorkload(): Int = shouldNotBeInvoked()
    override fun execute(): AnimeListDeadEntriesInconsistenciesResult = shouldNotBeInvoked()
    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = shouldNotBeInvoked()
}
