package io.github.manamiproject.manami.app.inconsistencies.animelist.metadata

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.inconsistencies.InconsistencyHandler
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.flow.update
import java.net.URI

internal class AnimeListMetaDataInconsistenciesHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = DefaultAnimeCache.instance,
    private val eventBus: EventBus = CoroutinesFlowEventBus,
): InconsistencyHandler<List<AnimeListMetaDataDiff>> {

    override suspend fun execute(): List<AnimeListMetaDataDiff> {
        log.info { "Starting check for meta data inconsistencies in AnimeList." }

        val result = state.animeList()
            .map { it to cache.fetch(it.link.asLink().uri) }
            .filter { it.second is PresentValue }
            .map { toAnimeListEntry(currentEntry = it.first, anime = (it.second as PresentValue).value) }
            .filterNot { it.first == it.second }
            .map { AnimeListMetaDataDiff(currentEntry = it.first, replacementEntry = it.second) }
            .toList()

        log.info { "Finished check for meta data inconsistencies in AnimeList." }

        eventBus.inconsistenciesState.update { current ->
            current.copy(
                animeListMetaDataInconsistencies = result,
            )
        }

        return result
    }

    private fun toAnimeListEntry(currentEntry: AnimeListEntry, anime: Anime): Pair<AnimeListEntry, AnimeListEntry> {
        return currentEntry to currentEntry.copy(
            title = anime.title,
            thumbnail = anime.picture,
            episodes = anime.episodes,
            type = anime.type,
        )
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimeListMetaDataInconsistenciesHandler]
         * @since 4.0.0
         */
        val instance: AnimeListMetaDataInconsistenciesHandler by lazy { AnimeListMetaDataInconsistenciesHandler() }
    }
}