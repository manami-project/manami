package io.github.manamiproject.manami.app.relatedanime

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.FindRelatedAnimeState
import io.github.manamiproject.manami.app.events.SearchResultAnimeEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.yield
import java.net.URI

internal class DefaultRelatedAnimeHandler(
    private val cache: AnimeCache = DefaultAnimeCache.instance,
    private val state: State = InternalState,
    private val eventBus: EventBus = CoroutinesFlowEventBus,
) : RelatedAnimeHandler {

    override suspend fun findRelatedAnime(initialSources: Collection<URI>) {
        if (initialSources.isEmpty()) return

        log.info { "Started related anime search for [${initialSources.size}] entries." }

        eventBus.findRelatedAnimeState.update { FindRelatedAnimeState(isRunning = true) }
        yield()

        val entriesToCheck = HashSet<URI>()
        var lastSize = 0

        val initialRelatedAnime = initialSources.map { cache.fetch(it) }
            .filterIsInstance<PresentValue<Anime>>()
            .flatMap { it.value.relatedAnime }
        initialSources.union(initialRelatedAnime).forEach { entriesToCheck.add(it) }

        val animeList = state.animeList().map { it.link }.filterIsInstance<Link>().map { it.uri }.toSet()
        val watchList = state.watchList().map { it.link.uri }.toSet()
        val ignoreList = state.ignoreList().map { it.link.uri }.toSet()

        log.info { "Initializing related anime search for [${initialSources.size}] entries is done." }

        while (entriesToCheck.size != lastSize) {
            lastSize = entriesToCheck.size
            entriesToCheck.map { cache.fetch(it) }
                .filterIsInstance<PresentValue<Anime>>()
                .flatMap { it.value.relatedAnime }
                .forEach { entriesToCheck.add(it) }
        }

        entriesToCheck.removeAll(animeList)
        entriesToCheck.removeAll(watchList)
        entriesToCheck.removeAll(ignoreList)

        val result = entriesToCheck.map { cache.fetch(it) }
            .filterIsInstance<PresentValue<Anime>>()
            .map { it.value }
            .map { SearchResultAnimeEntry(it) }

        eventBus.findRelatedAnimeState.update {
            FindRelatedAnimeState(
                isRunning = false,
                entries = result,
            )
        }
        log.info { "Finished related anime search for [${initialSources.size}] entries." }
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultRelatedAnimeHandler]
         * @since 4.0.0
         */
        val instance: DefaultRelatedAnimeHandler by lazy { DefaultRelatedAnimeHandler() }
    }
}