package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.runInBackground
import io.github.manamiproject.manami.app.search.SearchType.AND
import io.github.manamiproject.manami.app.search.SearchType.OR
import io.github.manamiproject.manami.app.search.anime.AnimeSearchEntryFoundEvent
import io.github.manamiproject.manami.app.search.anime.AnimeSearchFinishedEvent
import io.github.manamiproject.manami.app.search.season.AnimeSeasonEntryFoundEvent
import io.github.manamiproject.manami.app.search.season.AnimeSeasonSearchFinishedEvent
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.Tag
import io.github.manamiproject.modb.core.models.Title
import org.apache.commons.lang3.StringUtils.containsIgnoreCase
import org.apache.commons.text.similarity.LevenshteinDistance
import java.net.URI

internal class DefaultSearchHandler(
    private val cache: AnimeCache = Caches.animeCache,
    private val eventBus: EventBus = SimpleEventBus,
    private val state: State = InternalState,
) : SearchHandler {

    private val levenshteinDistance = LevenshteinDistance(2)

    override fun findInLists(searchString: String) {
        runInBackground {
            val animeListResults = state.animeList()
                .filter { isEntryMatchingSearchString(it.title, searchString) || (it.link is Link && it.link.uri.toString() == searchString) }
            eventBus.post(FileSearchAnimeListResultsEvent(animeListResults))

            val watchListResults = state.watchList()
                .filter { isEntryMatchingSearchString(it.title, searchString) || it.link.uri.toString() == searchString }
            eventBus.post(FileSearchWatchListResultsEvent(watchListResults))

            val ignoreListResults = state.ignoreList()
                .filter { isEntryMatchingSearchString(it.title, searchString) || it.link.uri.toString() == searchString }
            eventBus.post(FileSearchIgnoreListResultsEvent(ignoreListResults))
        }
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

    override fun findByTag(tags: Set<Tag>, metaDataProvider: Hostname, searchType: SearchType) {
        runInBackground {
            val entriesInLists: Set<URI> = state.animeList()
                .map { it.link }
                .filterIsInstance<Link>()
                .map { it.uri }
                .union(state.watchList().map { it.link.uri })
                .union(state.ignoreList().map { it.link.uri })

            val allEntriesNotInAnyList = cache.allEntries(metaDataProvider)
                .filterNot { anime -> entriesInLists.contains(anime.sources.first()) }

            when(searchType) {
                OR -> allEntriesNotInAnyList.filter { anime -> anime.tags.any { tag -> tags.contains(tag) } }
                AND -> allEntriesNotInAnyList.filter { anime -> anime.tags.containsAll(tags) }
            }.forEach {
                eventBus.post(AnimeSearchEntryFoundEvent(it))
            }

            eventBus.post(AnimeSearchFinishedEvent)
        }
    }

    override fun find(uri: URI) {
        runInBackground {
            val entry = cache.fetch(uri)
            if (entry is PresentValue) {
                eventBus.post(AnimeSearchEntryFoundEvent(entry.value))
            }
            eventBus.post(AnimeSearchFinishedEvent)
        }
    }

    override fun availableMetaDataProviders(): Set<Hostname> = cache.availableMetaDataProvider

    override fun availableTags(): Set<Tag> = cache.availableTags

    private fun isEntryMatchingSearchString(title: Title, searchString: String): Boolean {
        val levenshteinDistance = levenshteinDistance.apply(title.toLowerCase(), searchString.toLowerCase())
        val isTitleNearlyEqual = levenshteinDistance in 0..2
        val isInTitle = containsIgnoreCase(title, searchString)

        return when {
            isTitleNearlyEqual || isInTitle -> true
            else -> false
        }
    }
}