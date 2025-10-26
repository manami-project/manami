package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.events.*
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.search.SearchType.AND
import io.github.manamiproject.manami.app.search.SearchType.OR
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.config.Hostname
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.yield
import org.apache.commons.lang3.Strings
import org.apache.commons.text.similarity.LevenshteinDistance
import java.net.URI

internal class DefaultSearchHandler(
    private val cache: AnimeCache = DefaultAnimeCache.instance,
    private val eventBus: EventBus = CoroutinesFlowEventBus,
    private val state: State = InternalState,
) : SearchHandler {

    private val levenshteinDistance = LevenshteinDistance(2)

    override suspend fun findInLists(searchString: String) {
        eventBus.findInListState.update { FindInListState(isRunning = true) }
        yield()

        val animeListResults = state.animeList()
            .filter { isEntryMatchingSearchString(it.title, searchString) || (it.link is Link && it.link.uri.toString() == searchString) }

        eventBus.findInListState.update { current -> current.copy(animeListResults = animeListResults) }

        val watchListResults = state.watchList()
            .filter { isEntryMatchingSearchString(it.title, searchString) || it.link.uri.toString() == searchString }

        eventBus.findInListState.update { current -> current.copy(watchListResults = watchListResults) }

        val ignoreListResults = state.ignoreList()
            .filter { isEntryMatchingSearchString(it.title, searchString) || it.link.uri.toString() == searchString }

        eventBus.findInListState.update { current ->
            current.copy(
                isRunning = false,
                ignoreListResults = ignoreListResults,
            )
        }
    }

    override suspend fun findSeason(season: AnimeSeason, metaDataProvider: Hostname) {
        eventBus.findSeasonState.update { FindSeasonState(isRunning = true) }
        yield()

        val entriesInLists: Set<URI> = state.animeList()
            .map { it.link }
            .filterIsInstance<Link>()
            .map { it.uri }
            .union(state.watchList().map { it.link.uri })
            .union(state.ignoreList().map { it.link.uri })

        val entries = cache.allEntries(metaDataProvider)
            .filter { it.animeSeason == season }
            .filterNot { animeSeasonEntry -> entriesInLists.contains(animeSeasonEntry.sources.first()) }
            .toList()

        eventBus.findSeasonState.update { current ->
            current.copy(
                isRunning = false,
                entries = entries,
            )
        }
    }

    override suspend fun findByTag(tags: Set<Tag>, metaDataProvider: Hostname, searchType: SearchType, status: Set<AnimeStatus>) {
        eventBus.findByTagState.update { FindByTagState(isRunning = true) }
        yield()

        val entriesInLists: Set<URI> = state.animeList()
            .map { it.link }
            .filterIsInstance<Link>()
            .map { it.uri }
            .union(state.watchList().map { it.link.uri })
            .union(state.ignoreList().map { it.link.uri })

        val allEntriesNotInAnyList = cache.allEntries(metaDataProvider)
            .filterNot { anime -> entriesInLists.contains(anime.sources.first()) }

        val entriesWithMatchingTags = if (tags.isNotEmpty()) {
            when(searchType) {
                OR -> allEntriesNotInAnyList.filter { anime -> anime.tags.any { tag -> tags.contains(tag) } }
                AND -> allEntriesNotInAnyList.filter { anime -> anime.tags.containsAll(tags) }
            }
        } else {
            allEntriesNotInAnyList
        }

        val filteredByStatus = entriesWithMatchingTags.filter { it.status in status }.toList()

        eventBus.findByTagState.update { current ->
            current.copy(
                isRunning = false,
                entries = filteredByStatus,
            )
        }
    }

    override suspend fun findSimilarAnime(uri: URI) {
        if (uri.host !in availableMetaDataProviders()) return

        val origin = when (val cacheEntry = cache.fetch(uri)) {
            is DeadEntry -> {
                return
            }
            is PresentValue<Anime> -> cacheEntry.value
        }

        eventBus.findSimilarAnimeState.update { FindSimilarAnimeState(isRunning = true) }
        yield()

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
        val top10 = results.take(10).map { it.second }
        eventBus.findSimilarAnimeState.update { current ->
            current.copy(
                isRunning = true,
                entries = top10,
            )
        }
    }

    override suspend fun findAnime(uri: URI) {
        eventBus.findAnimeState.update { FindAnimeState(isRunning = true) }
        yield()

        val entry = cache.fetch(uri)
        if (entry is PresentValue) {
            eventBus.findAnimeState.update { current ->
                current.copy(
                    isRunning = false,
                    entry = entry.value,
                )
            }
        } else {
            eventBus.findAnimeState.update { FindAnimeState(isRunning = false) }
        }
    }

    override fun availableMetaDataProviders(): Set<Hostname> = cache.availableMetaDataProvider

    override fun availableTags(): Set<Tag> = cache.availableTags

    private fun isEntryMatchingSearchString(title: Title, searchString: String): Boolean {
        val levenshteinDistance = levenshteinDistance.apply(title.lowercase(), searchString.lowercase())
        val isTitleNearlyEqual = levenshteinDistance in 0..2
        val isInTitle = Strings.CI.contains(title, searchString)

        return when {
            isTitleNearlyEqual || isInTitle -> true
            else -> false
        }
    }

    companion object {
        /**
         * Singleton of [DefaultSearchHandler]
         * @since 4.0.0
         */
        val instance: DefaultSearchHandler by lazy { DefaultSearchHandler() }
    }
}