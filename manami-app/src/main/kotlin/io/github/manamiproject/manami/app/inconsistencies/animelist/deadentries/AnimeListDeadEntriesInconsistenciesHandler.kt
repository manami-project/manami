package io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.inconsistencies.InconsistencyHandler
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.flow.update
import java.net.URI

internal class AnimeListDeadEntriesInconsistenciesHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = DefaultAnimeCache.instance,
    private val eventBus: EventBus = CoroutinesFlowEventBus,
) : InconsistencyHandler<List<AnimeListEntry>> {

    override suspend fun execute(): List<AnimeListEntry> {
        log.info { "Starting check for dead entries in AnimeList." }

        val deadEntries = state.animeList()
            .filter { it.link is Link }
            .map { it to cache.fetch(it.link.asLink().uri) }
            .filter { it.second is DeadEntry }
            .map { it.first }
            .toSet()

        // TODO 4.1.0 Remove as soon as AnimeListEntry uses Link instead of LinkEntry
        val entriesWithoutLink = state.animeList()
            .filter { it.link is NoLink }
            .toSet()

        val result = deadEntries.union(entriesWithoutLink).toList()

        eventBus.inconsistenciesState.update { current ->
            current.copy(animeListDeadEntriesInconsistencies = result)
        }

        log.info { "Finished check for dead entries in AnimeList." }

        return result
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimeListDeadEntriesInconsistenciesHandler]
         * @since 4.0.0
         */
        val instance: AnimeListDeadEntriesInconsistenciesHandler by lazy { AnimeListDeadEntriesInconsistenciesHandler() }
    }
}