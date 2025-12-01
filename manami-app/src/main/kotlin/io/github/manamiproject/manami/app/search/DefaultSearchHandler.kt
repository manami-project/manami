package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.events.*
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.ScoreType.*
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.*
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.config.Hostname
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.yield
import org.apache.commons.lang3.Strings
import org.apache.commons.text.similarity.LevenshteinDistance
import java.net.URI
import kotlin.sequences.map

internal class DefaultSearchHandler(
    private val cache: AnimeCache = DefaultAnimeCache.instance,
    private val eventBus: EventBus = CoroutinesFlowEventBus,
    private val state: State = InternalState,
) : SearchHandler {

    private val levenshteinDistance = LevenshteinDistance(2)

    override suspend fun findByTitle(metaDataProvider: Hostname, searchString: String) {
        if (!cache.availableMetaDataProvider.contains(metaDataProvider)) return

        eventBus.findByTitleState.update { FindByTitleState(isRunning = true) }
        yield()

        val animeListResults = state.animeList()
            .filter { isEntryMatchingSearchString(it.title, searchString) || (it.link is Link && it.link.uri.toString() == searchString) }

        eventBus.findByTitleState.update { current -> current.copy(animeListResults = animeListResults) }
        yield()

        val watchListResults = state.watchList()
            .filter { isEntryMatchingSearchString(it.title, searchString) || it.link.uri.toString() == searchString }

        eventBus.findByTitleState.update { current -> current.copy(watchListResults = watchListResults) }
        yield()

        val ignoreListResults = state.ignoreList()
            .filter { isEntryMatchingSearchString(it.title, searchString) || it.link.uri.toString() == searchString }

        eventBus.findByTitleState.update { current -> current.copy(ignoreListResults = ignoreListResults) }
        yield()

        if (searchString.startsWith("https://$metaDataProvider")
            && animeListResults.map { it.link }.filterIsInstance<Link>().none { it.uri.toString() == searchString }
            && watchListResults.map { it.link }.none { it.uri.toString() == searchString }
            && ignoreListResults.map { it.link }.none { it.uri.toString() == searchString }
        ) {
            cache.fetch(URI(searchString))
        }

        val entriesInLists: Set<URI> = state.animeList()
            .map { it.link }
            .filterIsInstance<Link>()
            .map { it.uri }
            .union(state.watchList().map { it.link.uri })
            .union(state.ignoreList().map { it.link.uri })

        val unlistedResults = cache.allEntries(metaDataProvider)
            .filterNot { anime -> entriesInLists.contains(anime.sources.first()) }
            .filter { isEntryMatchingSearchString(it.title, searchString) || it.sources.first().toString() == searchString || it.synonyms.any { synonym -> isEntryMatchingSearchString(synonym, searchString) } }
            .map { SearchResultAnimeEntry(it) }
            .toList()

        eventBus.findByTitleState.update { current ->
            current.copy(
                isRunning = false,
                unlistedResults = unlistedResults,
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
            .map { SearchResultAnimeEntry(it) }
            .toList()

        eventBus.findSeasonState.update { current ->
            current.copy(
                isRunning = false,
                entries = entries,
            )
        }
    }

    override suspend fun findByCriteria(config: FindByCriteriaConfig) {
        eventBus.findByCriertiaState.update { FindByCriteriaState(isRunning = true) }
        yield()

        // to be excluded from the search result
        val entriesInLists: Set<URI> = state.animeList()
            .map { it.link }
            .filterIsInstance<Link>()
            .map { it.uri }
            .union(state.watchList().map { it.link.uri })
            .union(state.ignoreList().map { it.link.uri })

        // this is what we start with
        val result = cache.allEntries(config.metaDataProvider) // STEP 01 All entries of the selected metadata provider
            .filterNot { anime -> // STEP 02 Remove every anime which is already in one of the three lists
                entriesInLists.contains(anime.sources.first())
            }
            .filter { anime -> // STEP 03 type
                when(config.types.isNotEmpty()) {
                    true -> config.types.contains(anime.type)
                    false -> true
                }
            }
            .filter { anime -> // Step 04 status
                when(config.status.isNotEmpty()) {
                    true -> config.status.contains(anime.status)
                    false -> true
                }
            }
            .filter { anime -> // Step 05 season
                when(config.seasons.isNotEmpty()) {
                    true -> config.seasons.contains(anime.animeSeason.season)
                    false -> true
                }
            }
            .filter { anime -> // Step 06 episodes min
                when {
                    config.episodes.first < 0 -> true
                    else -> config.episodes.first <= anime.episodes
                }
            }
            .filter { anime -> // Step 07 episodes max
                when {
                    config.episodes.last < 0 -> true
                    else -> config.episodes.last >= anime.episodes
                }
            }
            .filter { anime -> // Step 08 duration min
                when {
                    config.durationInSeconds.first < 0 -> true
                    else -> config.durationInSeconds.first <= anime.duration.duration
                }
            }
            .filter { anime -> // Step 09 duration max
                when {
                    config.durationInSeconds.last < 0 -> true
                    else -> config.durationInSeconds.last >= anime.duration.duration
                }
            }
            .filter { anime -> // Step 10 year min
                when {
                    config.year.first < YEAR_OF_THE_FIRST_ANIME -> true
                    else -> config.year.first <= anime.animeSeason.year
                }
            }
            .filter { anime -> // Step 11 year max
                when {
                    config.year.last < YEAR_OF_THE_FIRST_ANIME -> true
                    else -> config.year.last >= anime.animeSeason.year
                }
            }
            .filter { anime -> // Step 12 score min
                when {
                    config.score.first < 0 -> true
                    anime.score is NoScore -> false
                    else -> when(config.scoreType) {
                        ARITHMETIC_GEOMETRIC_MEAN -> config.score.first <= (anime.score as ScoreValue).arithmeticGeometricMean
                        ARITHMETIC_MEAN -> config.score.first <= (anime.score as ScoreValue).arithmeticMean
                        MEDIAN -> config.score.first <= (anime.score as ScoreValue).median
                    }
                }
            }
            .filter { anime -> // Step 13 score max
                when {
                    config.score.last < 0 -> true
                    anime.score is NoScore -> false
                    else -> when(config.scoreType) {
                        ARITHMETIC_GEOMETRIC_MEAN -> config.score.last >= (anime.score as ScoreValue).arithmeticGeometricMean
                        ARITHMETIC_MEAN -> config.score.last >= (anime.score as ScoreValue).arithmeticMean
                        MEDIAN -> config.score.last >= (anime.score as ScoreValue).median
                    }
                }
            }
            .filter { anime -> // Step 14 studios
                when(config.studios.isNotEmpty()) {
                    true -> when(config.studiosConjunction) {
                        OR -> anime.studios.any { studio -> config.studios.contains(studio) }
                        AND -> anime.studios.containsAll(config.studios)
                    }
                    false -> true
                }
            }
            .filter { anime -> // Step 15 producers
                when(config.producers.isNotEmpty()) {
                    true -> when(config.producersConjunction) {
                        OR -> anime.producers.any { producer -> config.producers.contains(producer) }
                        AND -> anime.producers.containsAll(config.producers)
                    }
                    false -> true
                }
            }
            .filter { anime -> // Step 16 tags
                when(config.tags.isNotEmpty()) {
                    true -> when(config.tagsConjunction) {
                        OR -> anime.tags.any { tag -> config.tags.contains(tag) }
                        AND -> anime.tags.containsAll(config.tags)
                    }
                    false -> true
                }
            }
            .map { anime -> SearchResultAnimeEntry(anime) }
            .toList()


        eventBus.findByCriertiaState.update { current ->
            current.copy(
                isRunning = false,
                entries = result,
            )
        }
    }

    override suspend fun findSimilarAnime(uri: URI) {
        if (uri.host !in availableMetaDataProviders()) return

        eventBus.findSimilarAnimeState.update { FindSimilarAnimeState(isRunning = true) }
        yield()

        val origin = when (val cacheEntry = cache.fetch(uri)) {
            is DeadEntry -> {
                eventBus.findSimilarAnimeState.update { FindSimilarAnimeState(isRunning = false) }
                return
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

        val top10 = results.take(10)
            .map { it.second }
            .map { SearchResultAnimeEntry(it) }

        eventBus.findSimilarAnimeState.update { current ->
            current.copy(
                isRunning = false,
                entries = top10,
            )
        }
    }

    override suspend fun findAnimeDetails(uri: URI) {
        eventBus.findAnimeDetailsState.update {
            FindAnimeDetailsState(
                isRunning = true,
                entry = null,
            )
        }
        yield()

        val entry = cache.fetch(uri)
        if (entry is PresentValue) {
            eventBus.findAnimeDetailsState.update {
                FindAnimeDetailsState(
                    isRunning = false,
                    entry = entry.value,
                )
            }
        } else {
            eventBus.findAnimeDetailsState.update {
                FindAnimeDetailsState(
                    isRunning = false,
                )
            }
        }
    }

    override fun availableMetaDataProviders(): Set<Hostname> = cache.availableMetaDataProvider

    override fun availableTags(): Set<Tag> = cache.availableTags

    override fun availableStudios(): Set<Studio> = cache.availableStudios

    override fun availableProducers(): Set<Producer> = cache.availableProducers

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