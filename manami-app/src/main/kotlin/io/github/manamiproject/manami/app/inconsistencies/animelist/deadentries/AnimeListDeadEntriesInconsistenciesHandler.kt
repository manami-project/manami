package io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.app.inconsistencies.InconsistencyHandler
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI

internal class AnimeListDeadEntriesInconsistenciesHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = Caches.defaultAnimeCache,
) : InconsistencyHandler<AnimeListDeadEntriesInconsistenciesResult> {

    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = config.checkAnimeListDeadEnties

    override fun calculateWorkload(): Int = state.animeList().count { it.link is Link }

    override fun execute(progressUpdate: (Int) -> Unit): AnimeListDeadEntriesInconsistenciesResult {
        log.info { "Starting check for dead entries in AnimeList." }

        var progress = 0

        val result = state.animeList()
            .asSequence()
            .filter { it.link is Link }
            .map {
                progressUpdate.invoke(++progress)
                it
            }
            .map { it to cache.fetch(it.link.asLink().uri) }
            .filter { it.second is DeadEntry }
            .map { it.first }
            .toList()

        log.info { "Finished check for dead entries in AnimeList." }

        return AnimeListDeadEntriesInconsistenciesResult(
            entries = result,
        )
    }

    companion object {
        private val log by LoggerDelegate()
    }
}