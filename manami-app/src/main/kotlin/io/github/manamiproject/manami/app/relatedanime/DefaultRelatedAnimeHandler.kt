package io.github.manamiproject.manami.app.relatedanime

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.EventListType
import io.github.manamiproject.manami.app.events.EventListType.ANIME_LIST
import io.github.manamiproject.manami.app.events.EventListType.IGNORE_LIST
import io.github.manamiproject.manami.app.events.SimpleEventBus
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.net.URI

internal class DefaultRelatedAnimeHandler(
    private val cache: AnimeCache = DefaultAnimeCache.instance,
    private val state: State = InternalState,
    private val eventBus: EventBus = SimpleEventBus, // TODO 4.0.0: Migrate
) : RelatedAnimeHandler {

    override fun findRelatedAnimeForAnimeList() {
        log.info { "Searching related anime for anime list." }
        findRelatedAnime(ANIME_LIST, state.animeList().map { it.link }.filterIsInstance<Link>().map { it.uri }.toSet())
    }

    override fun findRelatedAnimeForIgnoreList() {
        log.info { "Searching related anime for ignore list." }
        findRelatedAnime(IGNORE_LIST, state.ignoreList().map { it.link.uri }.toSet())
    }

    private fun findRelatedAnime(eventListType: EventListType, initialSources: Collection<URI>) {
        val entriesToCheck = HashSet<URI>()
        var lastSize = 0

        val initialRelatedAnime = initialSources.map { cache.fetch(it) }
            .filterIsInstance<PresentValue<Anime>>()
            .flatMap { it.value.relatedAnime }
        initialSources.union(initialRelatedAnime).forEach { entriesToCheck.add(it) }

        val animeList = state.animeList().map { it.link }.filterIsInstance<Link>().map { it.uri }.toSet()
        val watchList = state.watchList().map { it.link.uri }.toSet()
        val ignoreList = state.ignoreList().map { it.link.uri }.toSet()

        log.info { "Initializing search for [$eventListType] related anime is done." }

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

        eventBus.post(RelatedAnimeFinishedEvent(eventListType, entriesToCheck.map { cache.fetch(it) }.filterIsInstance<PresentValue<Anime>>().map { it.value })) // TODO 4.0.0: Migrate
        log.info { "Finished searching for [$eventListType] related anime" }
    }

    companion object {
        private val log by LoggerDelegate()
    }
}