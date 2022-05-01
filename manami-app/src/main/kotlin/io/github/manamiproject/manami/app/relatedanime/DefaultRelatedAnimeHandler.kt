package io.github.manamiproject.manami.app.relatedanime

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.EventListType
import io.github.manamiproject.manami.app.events.EventListType.ANIME_LIST
import io.github.manamiproject.manami.app.events.EventListType.IGNORE_LIST
import io.github.manamiproject.manami.app.events.SimpleEventBus
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI
import java.util.*

internal class DefaultRelatedAnimeHandler(
    private val cache: AnimeCache = Caches.defaultAnimeCache,
    private val state: State = InternalState,
    private val eventBus: EventBus = SimpleEventBus,
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
        var numberOfEntriesToBeChecked: Int
        val entriesToCheck = Stack<URI>()

        val initialRelatedAnime = initialSources.map { cache.fetch(it) }
            .filterIsInstance<PresentValue<Anime>>()
            .flatMap { it.value.relatedAnime }
        initialSources.union(initialRelatedAnime).forEach { entriesToCheck.add(it) }
        numberOfEntriesToBeChecked = entriesToCheck.size

        val animeList = state.animeList().map { it.link }.filterIsInstance<Link>().map { it.uri }.toSet()
        val watchList = state.watchList().map { it.link.uri }.toSet()
        val ignoreList = state.ignoreList().map { it.link.uri }.toSet()

        val checkedEntries = mutableSetOf<URI>()

        log.info { "Initializing search for [$eventListType] related anime is done." }

        while (entriesToCheck.isNotEmpty()) {
            log.trace { "Checking ${checkedEntries.size+1}/$numberOfEntriesToBeChecked for [$eventListType] related anime" }
            val currentEntry = entriesToCheck.pop()

            if (!checkedEntries.contains(currentEntry)) {
                val entry = cache.fetch(currentEntry)
                if (entry is PresentValue<Anime>) {
                    entry.value.relatedAnime.filterNot { checkedEntries.contains(it) }
                        .filterNot { entriesToCheck.contains(it) }
                        .forEach {
                            entriesToCheck.push(it)
                            numberOfEntriesToBeChecked++
                        }

                    val entrySource = entry.value.sources.first()
                    if (!animeList.contains(entrySource) && !watchList.contains(entrySource) && !ignoreList.contains(entrySource)) {
                        eventBus.post(RelatedAnimeFoundEvent(eventListType, entry.value))
                    }
                }
            }

            checkedEntries.add(currentEntry)
            eventBus.post(RelatedAnimeStatusEvent(eventListType, checkedEntries.size, numberOfEntriesToBeChecked))
        }

        eventBus.post(RelatedAnimeFinishedEvent(eventListType))
        log.info { "Finished searching for [$eventListType] related anime" }
    }

    companion object {
        private val log by LoggerDelegate()
    }
}