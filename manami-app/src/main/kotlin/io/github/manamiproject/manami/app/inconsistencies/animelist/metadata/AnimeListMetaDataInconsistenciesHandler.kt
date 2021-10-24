package io.github.manamiproject.manami.app.inconsistencies.animelist.metadata

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.app.inconsistencies.InconsistencyHandler
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistencyHandler
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI


internal class AnimeListMetaDataInconsistenciesHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = Caches.animeCache,
): InconsistencyHandler<AnimeListMetaDataInconsistenciesResult> {

    override fun calculateWorkload(): Int = state.animeList().count { it.link is Link }

    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = config.checkAnimeListMetaData

    override fun execute(progressUpdate: (Int) -> Unit): AnimeListMetaDataInconsistenciesResult {
        log.info { "Starting check for meta data inconsistencies in AnimeList." }

        var progress = 0

        val result = state.animeList()
            .asSequence()
            .filter { it.link is Link }
            .map {
                progressUpdate.invoke(++progress)
                it
            }
            .map { it to cache.fetch(it.link.asLink().uri) }
            .filter { it.second is PresentValue }
            .map { toAnimeListEntry(currentEntry = it.first, anime = (it.second as PresentValue).value) }
            .filterNot { it.first == it.second }
            .map { AnimeListMetaDataDiff(currentEntry = it.first, replacementEntry = it.second) }
            .toList()

        log.info { "Finished check for meta data inconsistencies in AnimeList." }

        return AnimeListMetaDataInconsistenciesResult(
            entries = result,
        )
    }

    private fun toAnimeListEntry(currentEntry: AnimeListEntry, anime: Anime): Pair<AnimeListEntry, AnimeListEntry> {
        return currentEntry to currentEntry.copy(
            title = anime.title,
            thumbnail = anime.thumbnail,
            episodes = anime.episodes,
            type = anime.type,
        )
    }

    companion object {
        private val log by LoggerDelegate()
    }
}