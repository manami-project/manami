package io.github.manamiproject.manami.app.relatedanime

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI
import java.util.*

internal class DefaultRelatedAnimeHandler(
    private val cache: Cache<URI, CacheEntry<Anime>> = Caches.animeCache,
    private val state: State = InternalState,
    private val eventBus: EventBus = SimpleEventBus,
) : RelatedAnimeHandler {

    override fun findRelatedAnimeForAnimeList() {
        var numberOfEntriesToBeChecked: Int
        val entriesToCheck = Stack<URI>()

        val animeList = state.animeList().map { it.link }.filterIsInstance<Link>().map { it.uri }.toSet()
        val relatedAnime = animeList.map { cache.fetch(it) }
            .filterIsInstance<PresentValue<Anime>>()
            .flatMap { it.value.relatedAnime }
        animeList.union(relatedAnime).forEach { entriesToCheck.add(it) }
        numberOfEntriesToBeChecked = entriesToCheck.size

        val watchList = state.watchList().map { it.link.uri }.toSet()
        val ignoreList = state.ignoreList().map { it.link.uri }.toSet()

        val checkedEntries = mutableSetOf<URI>()

        while (entriesToCheck.isNotEmpty()) {
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
                        eventBus.post(RelatedAnimeFoundEvent(entry.value))
                    }
                }
            }

            checkedEntries.add(currentEntry)
            eventBus.post(RelatedAnimeStatusEvent(checkedEntries.size, numberOfEntriesToBeChecked))
        }
    }

    override fun findRelatedAnimeForIgnoreList() {
        TODO("Not yet implemented")
    }
}