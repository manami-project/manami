package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.SimpleEventBus
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.runInBackground
import io.github.manamiproject.manami.app.search.SearchType.AND
import io.github.manamiproject.manami.app.search.SearchType.OR
import io.github.manamiproject.manami.app.search.anime.AnimeEntryFinishedEvent
import io.github.manamiproject.manami.app.search.anime.AnimeEntryFoundEvent
import io.github.manamiproject.manami.app.search.anime.AnimeSearchEntryFoundEvent
import io.github.manamiproject.manami.app.search.anime.AnimeSearchFinishedEvent
import io.github.manamiproject.manami.app.search.season.AnimeSeasonEntryFoundEvent
import io.github.manamiproject.manami.app.search.season.AnimeSeasonSearchFinishedEvent
import io.github.manamiproject.manami.app.search.similaranime.SimilarAnimeFoundEvent
import io.github.manamiproject.manami.app.search.similaranime.SimilarAnimeSearchFinishedEvent
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.*
import org.apache.commons.lang3.StringUtils.containsIgnoreCase
import org.apache.commons.text.similarity.LevenshteinDistance
import java.lang.Thread.sleep
import java.net.URI

internal class DefaultSearchHandler(
    private val cache: AnimeCache = Caches.defaultAnimeCache,
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

    override fun findSimilarAnime(uri: URI) {
        runInBackground {
            if (uri.host !in availableMetaDataProviders()) {
                eventBus.post(SimilarAnimeSearchFinishedEvent)
            }

            val origin = when (val cacheEntry = cache.fetch(uri)) {
                is DeadEntry -> {
                    eventBus.post(SimilarAnimeSearchFinishedEvent)
                    return@runInBackground
                }
                is PresentValue<Anime> -> cacheEntry.value
            }

            val results = mutableListOf<Pair<Int, Anime>>()

            cache.allEntries(uri.host).forEach { currentEntry ->
                val intersection = origin.tags.intersect(currentEntry.tags).size
                if (intersection > 0 && !currentEntry.sources.contains(uri)) {
                    results.add(intersection to currentEntry)
                }
            }

            val animeListEntries = state.animeList().filter { it.link is Link }.map { it.link.asLink().uri }
            val watchListEntries = state.watchList().map { it.link.asLink().uri }
            val ignoreListEntries = state.ignoreList().map { it.link.asLink().uri }
            val entriesToRemove = animeListEntries.union(watchListEntries).union(ignoreListEntries)

            results.removeIf { entriesToRemove.contains(it.second.sources.first()) }

            results.sortWith(compareByDescending<Pair<Int, Anime>> { it.first }.thenBy { it.second.title })
            eventBus.post(SimilarAnimeFoundEvent(results.take(10).map { it.second }))
        }
    }

    override fun find(uri: URI) {
        runInBackground {
            val entry = cache.fetch(uri)
            if (entry is PresentValue) {
                eventBus.post(AnimeEntryFoundEvent(entry.value))
            }
            eventBus.post(AnimeEntryFinishedEvent)
        }
    }

    override fun availableMetaDataProviders(): Set<Hostname> = cache.availableMetaDataProvider

    override fun availableTags(): Set<Tag> = cache.availableTags

    private fun isEntryMatchingSearchString(title: Title, searchString: String): Boolean {
        val levenshteinDistance = levenshteinDistance.apply(title.lowercase(), searchString.lowercase())
        val isTitleNearlyEqual = levenshteinDistance in 0..2
        val isInTitle = containsIgnoreCase(title, searchString)

        return when {
            isTitleNearlyEqual || isInTitle -> true
            else -> false
        }
    }
}