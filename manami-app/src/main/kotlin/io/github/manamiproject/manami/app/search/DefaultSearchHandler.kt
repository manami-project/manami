package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.runInBackground
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.AnimeSeason
import java.net.URI

internal class DefaultSearchHandler(
    private val cache: AnimeCache = Caches.animeCache,
    private val eventBus: EventBus = SimpleEventBus,
    private val state: State = InternalState,
) : SearchHandler {

    override fun findInLists(searchString: String) {
        TODO("Not yet implemented")
    }

    override fun findSeason(season: AnimeSeason, metaDataProvider: Hostname) {
        runInBackground {
            val entriesInLists: Set<URI> = state.animeList()
                .map { it.link }
                .filterIsInstance<Link>()
                .map { it.uri }
                .union(state.watchList().map { it.link.uri })
                .union(state.ignoreList().map { it.link.uri })

            cache.allEntries(metaDataProvider)
                .filter { it.animeSeason == season }
                .filterNot { animeSeasonEntry -> entriesInLists.contains(animeSeasonEntry.sources.first()) }
                .forEach {
                    eventBus.post(AnimeSeasonEntryFoundEvent(it))
                }

            eventBus.post(AnimeSeasonSearchFinishedEvent)
        }
    }

    override fun availableMetaDataProviders(): Set<Hostname> = cache.availableMetaDataProvider
}