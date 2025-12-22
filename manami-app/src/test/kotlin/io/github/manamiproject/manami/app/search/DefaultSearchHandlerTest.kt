package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.events.*
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.ScoreType.ARITHMETIC_GEOMETRIC_MEAN
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.AND
import io.github.manamiproject.manami.app.search.FindByCriteriaConfig.SearchConjunction.OR
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.anime.*
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.serde.json.deserializer.AnimeFromJsonInputStreamDeserializer
import io.github.manamiproject.modb.serde.json.deserializer.FromRegularFileDeserializer
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.net.URI
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.test.AfterTest
import kotlin.test.Test

internal class DefaultSearchHandlerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class FindSeasonTests {

        @Test
        fun `find all entries that match the specific season and meta data provider`() {
            runBlocking { 
                // given
                val receivedEvents = mutableListOf<FindSeasonState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findSeasonState.collect { event -> receivedEvents.add(event) } }
                delay(100)
                
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }
    
                val matchingEntry1 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                    ),
                    title = "Fruits Basket: The Final",
                    type = TV,
                    episodes = 1,
                    status = UPCOMING,
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 2021,
                    ),
                )
    
                val matchingEntry2 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15807"),
                        URI("https://anilist.co/anime/125368"),
                        URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                        URI("https://kitsu.app/anime/43731"),
                        URI("https://myanimelist.net/anime/43609"),
                    ),
                    title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                    type = OVA,
                    episodes = 1,
                    status = UPCOMING,
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 2021,
                    ),
                )
    
                val notMatching1 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15070"),
                        URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                        URI("https://myanimelist.net/anime/40356"),
                    ),
                    title = "Tate no Yuusha no Nariagari Season 2",
                    type = TV,
                    episodes = 0,
                    status = UPCOMING,
                    animeSeason = AnimeSeason(
                        season = FALL,
                        year = 2021,
                    ),
                )
    
                val notMatching2 = Anime(
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/46587"),
                    ),
                    title = "Tenchi Souzou Design-bu Special",
                    type = ONA,
                    episodes = 1,
                    status = UPCOMING,
                    animeSeason = AnimeSeason(
                        season = UNDEFINED,
                        year = 2021
                    ),
                )
    
                val notMatching3 = Anime(
                    sources = hashSetOf(
                        URI("https://kitsu.app/anime/40614"),
                        URI("https://myanimelist.net/anime/34705"),
                    ),
                    title = "Tejina Shi",
                    type = MOVIE,
                    episodes = 1,
                    status = FINISHED,
                    animeSeason = AnimeSeason(
                        season = UNDEFINED,
                        year = 0,
                    ),
                )
    
                val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                    matchingEntry1.sources.forEach {
                        populate(it, PresentValue(matchingEntry1))
                    }
                    matchingEntry2.sources.forEach {
                        populate(it, PresentValue(matchingEntry2))
                    }
                    notMatching1.sources.forEach {
                        populate(it, PresentValue(notMatching1))
                    }
                    notMatching2.sources.forEach {
                        populate(it, PresentValue(notMatching2))
                    }
                    notMatching3.sources.forEach {
                        populate(it, PresentValue(notMatching3))
                    }
                }
    
                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )
    
                // when
                defaultSearchHandler.findSeason(
                    season = AnimeSeason(
                        year = 2021,
                        season = SPRING,
                    ),
                    "myanimelist.net"
                )
    
                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                    "Fruits Basket: The Final",
                    "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                )
            }
        }

        @Test
        fun `exclude entries in animelist`() {
            runBlocking { 
                tempDirectory {
                    // given
                    val receivedEvents = mutableListOf<FindSeasonState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findSeasonState.collect { event -> receivedEvents.add(event) } }
                    delay(100)
                    
                    val matchingEntry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                    )
    
                    val matchingEntry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                    )
    
                    val testLocation = tempDir.resolve("test").createDirectory().toAbsolutePath()
                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = listOf(
                            AnimeListEntry(
                                link = Link("https://myanimelist.net/anime/42938"),
                                title = "Fruits Basket: The Final",
                                episodes = 24,
                                type = TV,
                                location = testLocation,
                            )
                        )
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }
    
                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        matchingEntry1.sources.forEach {
                            populate(it, PresentValue(matchingEntry1))
                        }
                        matchingEntry2.sources.forEach {
                            populate(it, PresentValue(matchingEntry2))
                        }
                    }
    
                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )
    
                    // when
                    defaultSearchHandler.findSeason(
                        season = AnimeSeason(
                            year = 2021,
                            season = SPRING,
                        ),
                        "myanimelist.net"
                    )
    
                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                        "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                    )
                }
            }
        }

        @Test
        fun `exclude entries in watchlist`() {
            runBlocking { 
                // given
                val receivedEvents = mutableListOf<FindSeasonState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findSeasonState.collect { event -> receivedEvents.add(event) } }
                delay(100)
                
                val matchingEntry1 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                    ),
                    title = "Fruits Basket: The Final",
                    type = TV,
                    episodes = 24,
                    status = UPCOMING,
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 2021,
                    ),
                )
    
                val matchingEntry2 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15807"),
                        URI("https://anilist.co/anime/125368"),
                        URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                        URI("https://kitsu.app/anime/43731"),
                        URI("https://myanimelist.net/anime/43609"),
                    ),
                    title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                    type = OVA,
                    episodes = 1,
                    status = UPCOMING,
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 2021,
                    ),
                )
    
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = setOf(
                        WatchListEntry(
                            link = Link("https://myanimelist.net/anime/42938"),
                            title = "Fruits Basket: The Final",
                            thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png")
                        )
                    )
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }
    
                val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                    matchingEntry1.sources.forEach {
                        populate(it, PresentValue(matchingEntry1))
                    }
                    matchingEntry2.sources.forEach {
                        populate(it, PresentValue(matchingEntry2))
                    }
                }
    
                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )
    
                // when
                defaultSearchHandler.findSeason(
                    season = AnimeSeason(
                        year = 2021,
                        season = SPRING,
                    ),
                    "myanimelist.net"
                )
    
                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                    "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                )
            }
        }

        @Test
        fun `exclude entries in ignorelist`() {
            runBlocking { 
                // given
                val receivedEvents = mutableListOf<FindSeasonState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findSeasonState.collect { event -> receivedEvents.add(event) } }
                delay(100)
                
                val matchingEntry1 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                    ),
                    title = "Fruits Basket: The Final",
                    type = TV,
                    episodes = 24,
                    status = UPCOMING,
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 2021,
                    ),
                )
    
                val matchingEntry2 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15807"),
                        URI("https://anilist.co/anime/125368"),
                        URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                        URI("https://kitsu.app/anime/43731"),
                        URI("https://myanimelist.net/anime/43609"),
                    ),
                    title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                    type = OVA,
                    episodes = 1,
                    status = UPCOMING,
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 2021,
                    ),
                )
    
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                        IgnoreListEntry(
                            link = Link("https://myanimelist.net/anime/42938"),
                            title = "Fruits Basket: The Final",
                            thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png")
                        )
                    )
                }
    
                val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                    matchingEntry1.sources.forEach {
                        populate(it, PresentValue(matchingEntry1))
                    }
                    matchingEntry2.sources.forEach {
                        populate(it, PresentValue(matchingEntry2))
                    }
                }
    
                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )
    
                // when
                defaultSearchHandler.findSeason(
                    season = AnimeSeason(
                        year = 2021,
                        season = SPRING,
                    ),
                    "myanimelist.net"
                )
    
                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                    "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                )
            }
        }
    }

    @Nested
    inner class AvailableMetaDataProvidersTests {

        @Test
        fun `return the values from the AnimeCache`() {
            // given
            val entry = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.app/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                ),
                title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                type = OVA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                entry.sources.forEach {
                    populate(it, PresentValue(entry))
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = TestState,
                cache = testCache,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = defaultSearchHandler.availableMetaDataProviders()

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "anidb.net",
                "anilist.co",
                "anime-planet.com",
                "kitsu.app",
                "myanimelist.net",
            )
        }
    }

    @Nested
    inner class AvailableStudiosTests {

        @Test
        fun `return the values from the AnimeCache`() {
            // given
            val entry = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.app/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                ),
                title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                type = OVA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
                studios = hashSetOf(
                    "studio-1",
                    "studio-a",
                    "studio-z",
                ),
            )

            val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                entry.sources.forEach {
                    populate(it, PresentValue(entry))
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = TestState,
                cache = testCache,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = defaultSearchHandler.availableStudios()

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "studio-1",
                "studio-a",
                "studio-z",
            )
        }
    }

    @Nested
    inner class AvailableProducersTests {

        @Test
        fun `return the values from the AnimeCache`() {
            // given
            val entry = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.app/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                ),
                title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                type = OVA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
                producers = hashSetOf(
                    "producer-1",
                    "producer-a",
                    "producer-z",
                ),
            )

            val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                entry.sources.forEach {
                    populate(it, PresentValue(entry))
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = TestState,
                cache = testCache,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = defaultSearchHandler.availableProducers()

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "producer-1",
                "producer-a",
                "producer-z",
            )
        }
    }

    @Nested
    inner class AvailableTagsTests {

        @Test
        fun `return the values from the AnimeCache`() {
            // given
            val entry = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.app/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                ),
                title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                type = OVA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
                tags = hashSetOf(
                    "based on a manga",
                    "comedy",
                    "ensemble cast",
                    "food",
                    "high school",
                    "psychological",
                    "romance",
                    "romantic comedy",
                    "school",
                    "school clubs",
                    "school life",
                    "seinen",
                    "slice of life",
                    "student government",
                    "tsundere",
                )
            )

            val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                entry.sources.forEach {
                    populate(it, PresentValue(entry))
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = TestState,
                cache = testCache,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = defaultSearchHandler.availableTags()

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "based on a manga",
                "comedy",
                "ensemble cast",
                "food",
                "high school",
                "psychological",
                "romance",
                "romantic comedy",
                "school",
                "school clubs",
                "school life",
                "seinen",
                "slice of life",
                "student government",
                "tsundere",
            )
        }
    }

    @Nested
    inner class FindByCriteriaTests {

        @Test
        fun `default - ignore properties and include all entries`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<FindByCriteriaState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val entry1 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                    ),
                    title = "Fruits Basket: The Final",
                    type = TV,
                    episodes = 1,
                    status = FINISHED,
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 2021,
                    ),
                    tags = hashSetOf("my-tag-1", "my-tag-2"),
                )

                val entry2 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15070"),
                        URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                        URI("https://myanimelist.net/anime/40356"),
                    ),
                    title = "Tate no Yuusha no Nariagari Season 2",
                    type = TV,
                    episodes = 0,
                    status = ONGOING,
                    animeSeason = AnimeSeason(
                        season = FALL,
                        year = 2021,
                    ),
                    tags = hashSetOf("my-tag-1"),
                )

                val entry3 = Anime(
                    sources = hashSetOf(
                        URI("https://myanimelist.net/anime/46587"),
                    ),
                    title = "Tenchi Souzou Design-bu Special",
                    type = ONA,
                    episodes = 1,
                    status = UPCOMING,
                    animeSeason = AnimeSeason(
                        season = UNDEFINED,
                        year = 2021,
                    ),
                    tags = hashSetOf("my-tag-2"),
                )

                val entry4 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15807"),
                        URI("https://anilist.co/anime/125368"),
                        URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                        URI("https://kitsu.app/anime/43731"),
                        URI("https://myanimelist.net/anime/43609"),
                    ),
                    title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                    type = OVA,
                    episodes = 1,
                    status = UNKNOWN,
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 2021,
                    ),
                    tags = hashSetOf("totally-different-tag")
                )

                val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                    entry1.sources.forEach {
                        populate(it, PresentValue(entry1))
                    }
                    entry2.sources.forEach {
                        populate(it, PresentValue(entry2))
                    }
                    entry3.sources.forEach {
                        populate(it, PresentValue(entry3))
                    }
                    entry4.sources.forEach {
                        populate(it, PresentValue(entry4))
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                val findByCriteriaConfig = FindByCriteriaConfig(
                    metaDataProvider = "myanimelist.net",
                )

                // when
                defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                    Link("https://myanimelist.net/anime/42938"),
                    Link("https://myanimelist.net/anime/40356"),
                    Link("https://myanimelist.net/anime/46587"),
                    Link("https://myanimelist.net/anime/43609"),
                )
            }
        }

        @Nested
        inner class MetaDataProviderTests {

            @Test
            fun `only returns entries of the requested meta data provider`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig("kitsu.app")

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://kitsu.app/anime/43578"),
                        Link("https://kitsu.app/anime/43731"),
                    )
                }
            }
        }

        @Nested
        inner class ExcludeListsTests {

            @Test
            fun `exclude entries in anime list`() {
                runBlocking {
                    tempDirectory {
                        // given
                        val receivedEvents = mutableListOf<FindByCriteriaState>()
                        val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                        delay(100)

                        val testLocation = tempDir.resolve("test").createDirectory().toAbsolutePath()

                        val testState = object: State by TestState {
                            override fun animeList(): List<AnimeListEntry> = listOf(
                                AnimeListEntry(
                                    link = Link("https://myanimelist.net/anime/42938"),
                                    title = "Fruits Basket: The Final",
                                    episodes = 24,
                                    type = TV,
                                    location = testLocation,
                                )
                            )
                            override fun watchList(): Set<WatchListEntry> = emptySet()
                            override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                        }

                        val matchingEntry1 = Anime(
                            sources = hashSetOf(
                                URI("https://anidb.net/anime/15738"),
                                URI("https://anilist.co/anime/124194"),
                                URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                                URI("https://kitsu.app/anime/43578"),
                                URI("https://myanimelist.net/anime/42938"),
                            ),
                            title = "Fruits Basket: The Final",
                            type = TV,
                            episodes = 1,
                            status = UPCOMING,
                            animeSeason = AnimeSeason(
                                season = SPRING,
                                year = 2021,
                            ),
                            tags = hashSetOf("my-tag-1", "my-tag-2"),
                        )

                        val matchingEntry2 = Anime(
                            sources = hashSetOf(
                                URI("https://anidb.net/anime/15070"),
                                URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                                URI("https://myanimelist.net/anime/40356"),
                            ),
                            title = "Tate no Yuusha no Nariagari Season 2",
                            type = TV,
                            episodes = 0,
                            status = UPCOMING,
                            animeSeason = AnimeSeason(
                                season = FALL,
                                year = 2021,
                            ),
                            tags = hashSetOf("my-tag-1"),
                        )

                        val matchingEntry3 = Anime(
                            sources = hashSetOf(
                                URI("https://myanimelist.net/anime/46587"),
                            ),
                            title = "Tenchi Souzou Design-bu Special",
                            type = ONA,
                            episodes = 1,
                            status = UPCOMING,
                            animeSeason = AnimeSeason(
                                season = UNDEFINED,
                                year = 2021,
                            ),
                            tags = hashSetOf("my-tag-2"),
                        )

                        val notMatchingEntry = Anime(
                            sources = hashSetOf(
                                URI("https://anidb.net/anime/15807"),
                                URI("https://anilist.co/anime/125368"),
                                URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                                URI("https://kitsu.app/anime/43731"),
                                URI("https://myanimelist.net/anime/43609"),
                            ),
                            title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                            type = OVA,
                            episodes = 1,
                            status = UPCOMING,
                            animeSeason = AnimeSeason(
                                season = SPRING,
                                year = 2021,
                            ),
                            tags = hashSetOf("totally-different-tag")
                        )

                        val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                            matchingEntry1.sources.forEach {
                                populate(it, PresentValue(matchingEntry1))
                            }
                            matchingEntry2.sources.forEach {
                                populate(it, PresentValue(matchingEntry2))
                            }
                            matchingEntry3.sources.forEach {
                                populate(it, PresentValue(matchingEntry3))
                            }
                            notMatchingEntry.sources.forEach {
                                populate(it, PresentValue(notMatchingEntry))
                            }
                        }

                        val defaultSearchHandler = DefaultSearchHandler(
                            state = testState,
                            cache = testCache,
                            eventBus = CoroutinesFlowEventBus,
                        )

                        val findByCriteriaConfig = FindByCriteriaConfig(
                            metaDataProvider = "myanimelist.net",
                            tags = setOf("my-tag-1", "my-tag-2"),
                            tagsConjunction = OR,
                        )

                        // when
                        defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                        // then
                        delay(100)
                        eventCollector.cancelAndJoin()
                        assertThat(receivedEvents).hasSize(3) // initial, start, result
                        assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                            "Tate no Yuusha no Nariagari Season 2",
                            "Tenchi Souzou Design-bu Special",
                        )
                    }
                }
            }

            @Test
            fun `exclude entries in watch list`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = setOf(
                            WatchListEntry(
                                link = Link("https://myanimelist.net/anime/42938"),
                                title = "Fruits Basket: The Final",
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png")
                            )
                        )
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val matchingEntry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val matchingEntry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val matchingEntry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val notMatchingEntry = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        matchingEntry1.sources.forEach {
                            populate(it, PresentValue(matchingEntry1))
                        }
                        matchingEntry2.sources.forEach {
                            populate(it, PresentValue(matchingEntry2))
                        }
                        matchingEntry3.sources.forEach {
                            populate(it, PresentValue(matchingEntry3))
                        }
                        notMatchingEntry.sources.forEach {
                            populate(it, PresentValue(notMatchingEntry))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        tags = setOf("my-tag-1", "my-tag-2"),
                        tagsConjunction = OR,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                        "Tate no Yuusha no Nariagari Season 2",
                        "Tenchi Souzou Design-bu Special",
                    )
                }
            }

            @Test
            fun `exclude entries in ignore list`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                            IgnoreListEntry(
                                link = Link("https://myanimelist.net/anime/42938"),
                                title = "Fruits Basket: The Final",
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png")
                            )
                        )
                    }

                    val matchingEntry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val matchingEntry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val matchingEntry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val notMatchingEntry = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        matchingEntry1.sources.forEach {
                            populate(it, PresentValue(matchingEntry1))
                        }
                        matchingEntry2.sources.forEach {
                            populate(it, PresentValue(matchingEntry2))
                        }
                        matchingEntry3.sources.forEach {
                            populate(it, PresentValue(matchingEntry3))
                        }
                        notMatchingEntry.sources.forEach {
                            populate(it, PresentValue(notMatchingEntry))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        tags = setOf("my-tag-1", "my-tag-2"),
                        tagsConjunction = OR,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                        "Tate no Yuusha no Nariagari Season 2",
                        "Tenchi Souzou Design-bu Special",
                    )
                }
            }
        }

        @Nested
        inner class TypeTests {

            @Test
            fun `correctly filters for specific entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 1,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        types = setOf(OVA, ONA),
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/46587"),
                        Link("https://myanimelist.net/anime/43609"),
                    )
                }
            }
        }

        @Nested
        inner class StatusTests {

            @Test
            fun `correctly filters for specific entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 1,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        status = setOf(ONGOING, FINISHED),
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/42938"),
                        Link("https://myanimelist.net/anime/40356"),
                    )
                }
            }
        }

        @Nested
        inner class SeasonTests {

            @Test
            fun `correctly filters for specific entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 1,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        seasons = setOf(FALL, UNDEFINED),
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/40356"),
                        Link("https://myanimelist.net/anime/46587"),
                    )
                }
            }
        }

        @Nested
        inner class EpisodeTests {

            @Test
            fun `correctly filters for min`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        episodes = 24..-1
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/42938"),
                        Link("https://myanimelist.net/anime/40356"),
                    )
                }
            }

            @Test
            fun `correctly filters for max`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        episodes = -1..13
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/43609"),
                        Link("https://myanimelist.net/anime/46587"),
                    )
                }
            }

            @Test
            fun `correctly filters for min and max`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        episodes = 13..30
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/42938"),
                        Link("https://myanimelist.net/anime/46587"),
                    )
                }
            }
        }

        @Nested
        inner class DurationTests {

            @Test
            fun `correctly filters for min`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                        duration = Duration(
                            value = 24,
                            unit = MINUTES,
                        ),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                        duration = Duration(
                            value = 23,
                            unit = MINUTES,
                        ),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                        duration = Duration(
                            value = 10,
                            unit = MINUTES,
                        ),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag"),
                        duration = Duration(
                            value = 90,
                            unit = MINUTES,
                        ),
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        durationInSeconds = 1440..-1
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/42938"),
                        Link("https://myanimelist.net/anime/43609"),
                    )
                }
            }

            @Test
            fun `correctly filters for max`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                        duration = Duration(
                            value = 24,
                            unit = MINUTES,
                        ),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                        duration = Duration(
                            value = 23,
                            unit = MINUTES,
                        ),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                        duration = Duration(
                            value = 10,
                            unit = MINUTES,
                        ),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag"),
                        duration = Duration(
                            value = 90,
                            unit = MINUTES,
                        ),
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        durationInSeconds = -1..1380
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/40356"),
                        Link("https://myanimelist.net/anime/46587"),
                    )
                }
            }

            @Test
            fun `correctly filters for min and max`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                        duration = Duration(
                            value = 24,
                            unit = MINUTES,
                        ),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                        duration = Duration(
                            value = 23,
                            unit = MINUTES,
                        ),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                        duration = Duration(
                            value = 10,
                            unit = MINUTES,
                        ),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag"),
                        duration = Duration(
                            value = 90,
                            unit = MINUTES,
                        ),
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        durationInSeconds = 1200..1800
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/42938"),
                        Link("https://myanimelist.net/anime/40356"),
                    )
                }
            }
        }

        @Nested
        inner class YearTests {

            @Test
            fun `correctly filters for min`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 1990,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2000,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2014,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2024,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        year = 2014..-1
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/46587"),
                        Link("https://myanimelist.net/anime/43609"),
                    )
                }
            }

            @Test
            fun `correctly filters for max`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 1990,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2000,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2014,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2024,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        year = -1..2004
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/42938"),
                        Link("https://myanimelist.net/anime/40356"),
                    )
                }
            }

            @Test
            fun `correctly filters for min and max`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 1990,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2000,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2014,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2024,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        year = 2010..2020
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/46587"),
                    )
                }
            }
        }

        @Nested
        inner class StudiosTests {

            @Test
            fun `AND - find all entries that contain all studios`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val matchingEntry = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        studios = hashSetOf("studio-1", "studio-2"),
                    )

                    val notMatching1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        studios = hashSetOf("studio-1"),
                    )

                    val notMatching2 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        studios = hashSetOf("studio-2"),
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        matchingEntry.sources.forEach {
                            populate(it, PresentValue(matchingEntry))
                        }
                        notMatching1.sources.forEach {
                            populate(it, PresentValue(notMatching1))
                        }
                        notMatching2.sources.forEach {
                            populate(it, PresentValue(notMatching2))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        studios = setOf("studio-1", "studio-2"),
                        studiosConjunction = AND,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                        "Fruits Basket: The Final",
                    )
                }
            }

            @Test
            fun `OR - find all entries that contain at least one of the given studios`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val matchingEntry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        studios = hashSetOf("studio-1", "studio-2"),
                    )

                    val matchingEntry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        studios = hashSetOf("studio-1"),
                    )

                    val matchingEntry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        studios = hashSetOf("studio-2"),
                    )

                    val notMatchingEntry = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        studios = hashSetOf("totally-different-studio")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        matchingEntry1.sources.forEach {
                            populate(it, PresentValue(matchingEntry1))
                        }
                        matchingEntry2.sources.forEach {
                            populate(it, PresentValue(matchingEntry2))
                        }
                        matchingEntry3.sources.forEach {
                            populate(it, PresentValue(matchingEntry3))
                        }
                        notMatchingEntry.sources.forEach {
                            populate(it, PresentValue(notMatchingEntry))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        studios = setOf("studio-1", "studio-2"),
                        studiosConjunction = OR,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                        "Fruits Basket: The Final",
                        "Tate no Yuusha no Nariagari Season 2",
                        "Tenchi Souzou Design-bu Special",
                    )
                }
            }
        }

        @Nested
        inner class ProducersTests {

            @Test
            fun `AND - find all entries that contain all producers`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val matchingEntry = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        producers = hashSetOf("producer-1", "producer-2"),
                    )

                    val notMatching1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        producers = hashSetOf("producer-1"),
                    )

                    val notMatching2 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        producers = hashSetOf("producer-2"),
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        matchingEntry.sources.forEach {
                            populate(it, PresentValue(matchingEntry))
                        }
                        notMatching1.sources.forEach {
                            populate(it, PresentValue(notMatching1))
                        }
                        notMatching2.sources.forEach {
                            populate(it, PresentValue(notMatching2))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        producers = setOf("producer-1", "producer-2"),
                        producersConjunction = AND,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                        "Fruits Basket: The Final",
                    )
                }
            }

            @Test
            fun `OR - find all entries that contain at least one of the given producers`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val matchingEntry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        producers = hashSetOf("producer-1", "producer-2"),
                    )

                    val matchingEntry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        producers = hashSetOf("producer-1"),
                    )

                    val matchingEntry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        producers = hashSetOf("producer-2"),
                    )

                    val notMatchingEntry = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        producers = hashSetOf("totally-different-studio")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        matchingEntry1.sources.forEach {
                            populate(it, PresentValue(matchingEntry1))
                        }
                        matchingEntry2.sources.forEach {
                            populate(it, PresentValue(matchingEntry2))
                        }
                        matchingEntry3.sources.forEach {
                            populate(it, PresentValue(matchingEntry3))
                        }
                        notMatchingEntry.sources.forEach {
                            populate(it, PresentValue(notMatchingEntry))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        producers = setOf("producer-1", "producer-2"),
                        producersConjunction = OR,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                        "Fruits Basket: The Final",
                        "Tate no Yuusha no Nariagari Season 2",
                        "Tenchi Souzou Design-bu Special",
                    )
                }
            }
        }

        @Nested
        inner class TagsTests {

            @Test
            fun `AND - find all entries that contain all tags`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val matchingEntry = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val notMatching1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val notMatching2 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        matchingEntry.sources.forEach {
                            populate(it, PresentValue(matchingEntry))
                        }
                        notMatching1.sources.forEach {
                            populate(it, PresentValue(notMatching1))
                        }
                        notMatching2.sources.forEach {
                            populate(it, PresentValue(notMatching2))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        tags = setOf("my-tag-1", "my-tag-2"),
                        tagsConjunction = AND,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                        "Fruits Basket: The Final",
                    )
                }
            }

            @Test
            fun `OR - find all entries that contain at least one of the given tags`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val matchingEntry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                    )

                    val matchingEntry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 0,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                    )

                    val matchingEntry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                    )

                    val notMatchingEntry = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 1,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag")
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        matchingEntry1.sources.forEach {
                            populate(it, PresentValue(matchingEntry1))
                        }
                        matchingEntry2.sources.forEach {
                            populate(it, PresentValue(matchingEntry2))
                        }
                        matchingEntry3.sources.forEach {
                            populate(it, PresentValue(matchingEntry3))
                        }
                        notMatchingEntry.sources.forEach {
                            populate(it, PresentValue(notMatchingEntry))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        tags = setOf("my-tag-1", "my-tag-2"),
                        tagsConjunction = OR,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.title }).containsExactlyInAnyOrder(
                        "Fruits Basket: The Final",
                        "Tate no Yuusha no Nariagari Season 2",
                        "Tenchi Souzou Design-bu Special",
                    )
                }
            }
        }

        @Nested
        inner class ScoreTests {

            @Test
            fun `include NoScore values if no range has been set`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                        score = NoScore,
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                        score = NoScore,
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                        score = NoScore,
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag"),
                        score = NoScore,
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        score = -1..-1,
                        scoreType = ARITHMETIC_GEOMETRIC_MEAN
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries).hasSize(4)
                }
            }

            @Test
            fun `exclude NoScore values if min has been set`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                        score = NoScore,
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                        score = NoScore,
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                        score = NoScore,
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag"),
                        score = NoScore,
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        score = 5..-1,
                        scoreType = ARITHMETIC_GEOMETRIC_MEAN
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries).isEmpty()
                }
            }

            @Test
            fun `exclude NoScore values if max has been set`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                        score = NoScore,
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                        score = NoScore,
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                        score = NoScore,
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag"),
                        score = NoScore,
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        score = -1..9,
                        scoreType = ARITHMETIC_GEOMETRIC_MEAN
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries).isEmpty()
                }
            }

            @Test
            fun `exclude NoScore values if min and max have been set`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                        score = NoScore,
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                        score = NoScore,
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                        score = NoScore,
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag"),
                        score = NoScore,
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        score = 5..9,
                        scoreType = ARITHMETIC_GEOMETRIC_MEAN
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries).isEmpty()
                }
            }

            @ParameterizedTest
            @EnumSource(value = FindByCriteriaConfig.ScoreType::class)
            fun `correctly filters for min`(value: FindByCriteriaConfig.ScoreType) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 6.0,
                            arithmeticMean = 6.0,
                            median = 6.0,
                        ),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 7.0,
                            arithmeticMean = 7.0,
                            median = 7.0,
                        ),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 4.0,
                            arithmeticMean = 4.0,
                            median = 4.0,
                        ),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 9.0,
                            arithmeticMean = 9.0,
                            median = 9.0,
                        ),
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        score = 7..-1,
                        scoreType = value,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/40356"),
                        Link("https://myanimelist.net/anime/43609"),
                    )
                }
            }

            @ParameterizedTest
            @EnumSource(value = FindByCriteriaConfig.ScoreType::class)
            fun `correctly filters for max`(value: FindByCriteriaConfig.ScoreType) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 6.0,
                            arithmeticMean = 6.0,
                            median = 6.0,
                        ),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 7.0,
                            arithmeticMean = 7.0,
                            median = 7.0,
                        ),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 4.0,
                            arithmeticMean = 4.0,
                            median = 4.0,
                        ),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 9.0,
                            arithmeticMean = 9.0,
                            median = 9.0,
                        ),
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        score = -1..6,
                        scoreType = value,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/42938"),
                        Link("https://myanimelist.net/anime/46587"),
                    )
                }
            }

            @ParameterizedTest
            @EnumSource(value = FindByCriteriaConfig.ScoreType::class)
            fun `correctly filters for min and max`(value: FindByCriteriaConfig.ScoreType) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByCriteriaState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val entry1 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15738"),
                            URI("https://anilist.co/anime/124194"),
                            URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                            URI("https://kitsu.app/anime/43578"),
                            URI("https://myanimelist.net/anime/42938"),
                        ),
                        title = "Fruits Basket: The Final",
                        type = TV,
                        episodes = 24,
                        status = FINISHED,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1", "my-tag-2"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 6.0,
                            arithmeticMean = 6.0,
                            median = 6.0,
                        ),
                    )

                    val entry2 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15070"),
                            URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                            URI("https://myanimelist.net/anime/40356"),
                        ),
                        title = "Tate no Yuusha no Nariagari Season 2",
                        type = TV,
                        episodes = 51,
                        status = ONGOING,
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-1"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 7.0,
                            arithmeticMean = 7.0,
                            median = 7.0,
                        ),
                    )

                    val entry3 = Anime(
                        sources = hashSetOf(
                            URI("https://myanimelist.net/anime/46587"),
                        ),
                        title = "Tenchi Souzou Design-bu Special",
                        type = ONA,
                        episodes = 13,
                        status = UPCOMING,
                        animeSeason = AnimeSeason(
                            season = UNDEFINED,
                            year = 2021,
                        ),
                        tags = hashSetOf("my-tag-2"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 4.0,
                            arithmeticMean = 4.0,
                            median = 4.0,
                        ),
                    )

                    val entry4 = Anime(
                        sources = hashSetOf(
                            URI("https://anidb.net/anime/15807"),
                            URI("https://anilist.co/anime/125368"),
                            URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                            URI("https://kitsu.app/anime/43731"),
                            URI("https://myanimelist.net/anime/43609"),
                        ),
                        title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                        type = OVA,
                        episodes = 12,
                        status = UNKNOWN,
                        animeSeason = AnimeSeason(
                            season = SPRING,
                            year = 2021,
                        ),
                        tags = hashSetOf("totally-different-tag"),
                        score = ScoreValue(
                            arithmeticGeometricMean = 9.0,
                            arithmeticMean = 9.0,
                            median = 9.0,
                        ),
                    )

                    val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                        entry1.sources.forEach {
                            populate(it, PresentValue(entry1))
                        }
                        entry2.sources.forEach {
                            populate(it, PresentValue(entry2))
                        }
                        entry3.sources.forEach {
                            populate(it, PresentValue(entry3))
                        }
                        entry4.sources.forEach {
                            populate(it, PresentValue(entry4))
                        }
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        state = testState,
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val findByCriteriaConfig = FindByCriteriaConfig(
                        metaDataProvider = "myanimelist.net",
                        score = 6..7,
                        scoreType = value,
                    )

                    // when
                    defaultSearchHandler.findByCriteria(findByCriteriaConfig)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    assertThat(receivedEvents.last().entries.map { it.link }).containsExactlyInAnyOrder(
                        Link("https://myanimelist.net/anime/42938"),
                        Link("https://myanimelist.net/anime/40356"),
                    )
                }
            }
        }
    }

    @Nested
    inner class FindAnimeDetailsTests {

        @Test
        fun `successfully find anime`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<FindAnimeDetailsState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findAnimeDetailsState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val entry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                    ),
                    title = "Fruits Basket: The Final",
                    type = TV,
                    episodes = 1,
                    status = UPCOMING,
                    animeSeason = AnimeSeason(
                        season = SPRING,
                        year = 2021,
                    ),
                    tags = hashSetOf("my-tag-1", "my-tag-2"),
                )

                val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                    entry.sources.forEach {
                        populate(it, PresentValue(entry))
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    state = TestState,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultSearchHandler.findAnimeDetails(URI("https://myanimelist.net/anime/42938"))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().entry).isNotNull()
                assertThat(receivedEvents.last().entry!!.title).isEqualTo("Fruits Basket: The Final")
            }
        }

        @Test
        fun `don't return anime, because URI relates to a dead entry`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<FindAnimeDetailsState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findAnimeDetailsState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                    populate(URI("https://myanimelist.net/anime/10001"), DeadEntry())
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    state = TestState,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultSearchHandler.findAnimeDetails(URI("https://myanimelist.net/anime/10001"))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().entry).isNull()
            }
        }
    }

    @Nested
    inner class FindSimilarAnimeTests {

        @Test
        fun `return with finished event if the host is not a supported metadata provider`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<FindSimilarAnimeState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findSimilarAnimeState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testCache = object : AnimeCache by TestAnimeCache {
                    override val availableMetaDataProvider: Set<Hostname>
                        get() = setOf(MyanimelistConfig.hostname())
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    state = TestState,
                )

                // when
                defaultSearchHandler.findSimilarAnime(URI("https://example.org/anime/1535"))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(1) // initial
            }
        }

        @Test
        fun `return with finished event if the uri represents a dead entry`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<FindSimilarAnimeState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findSimilarAnimeState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testCache = object : AnimeCache by TestAnimeCache {
                    override val availableMetaDataProvider: Set<Hostname>
                        get() = setOf(MyanimelistConfig.hostname())

                    override suspend fun fetch(key: URI): CacheEntry<Anime> = DeadEntry()
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    state = TestState,
                )

                // when
                defaultSearchHandler.findSimilarAnime(URI("https://myanimelist.net/anime/10"))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, finished
            }
        }

        @Test
        fun `return 10 anime having the most amount tags which reside in both anime sorted by number of number of matching tags desc`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<FindSimilarAnimeState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findSimilarAnimeState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testState = object : State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val testCache = DefaultAnimeCache(
                    cacheLoader = emptyList(),
                )

                FromRegularFileDeserializer(deserializer = AnimeFromJsonInputStreamDeserializer.instance).deserialize(testResource("search_tests/similar_anime_tests/reduced-anime-offline-database.json")).collect {
                    it.sources.forEach { source ->
                        testCache.populate(source, PresentValue(it))
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    state = testState,
                )

                // when
                defaultSearchHandler.findSimilarAnime(URI("https://myanimelist.net/anime/1"))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3)
                assertThat(receivedEvents.last().entries.map { it.link.uri }).containsExactly(
                    URI("https://myanimelist.net/anime/100"),
                    URI("https://myanimelist.net/anime/101"),
                    URI("https://myanimelist.net/anime/102"),
                    URI("https://myanimelist.net/anime/103"),
                    URI("https://myanimelist.net/anime/104"),
                    URI("https://myanimelist.net/anime/105"),
                    URI("https://myanimelist.net/anime/106"),
                    URI("https://myanimelist.net/anime/107"),
                    URI("https://myanimelist.net/anime/108"),
                    URI("https://myanimelist.net/anime/109"),
                )
            }
        }

        @Test
        fun `result must not contain entries from anime list`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<FindSimilarAnimeState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findSimilarAnimeState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testState = object : State by TestState {
                    override fun animeList(): List<AnimeListEntry> = listOf(
                        AnimeListEntry(
                            title = "Mirai Nikki (TV)",
                            link = Link("https://myanimelist.net/anime/106"),
                            episodes = 26,
                            type = TV,
                            location = Paths.get("."),
                        )
                    )

                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val testCache = DefaultAnimeCache(
                    cacheLoader = emptyList(),
                )

                FromRegularFileDeserializer(deserializer = AnimeFromJsonInputStreamDeserializer.instance).deserialize(testResource("search_tests/similar_anime_tests/reduced-anime-offline-database.json")).collect {
                    it.sources.forEach { source ->
                        testCache.populate(source, PresentValue(it))
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    state = testState,
                )

                // when
                defaultSearchHandler.findSimilarAnime(URI("https://myanimelist.net/anime/1"))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().entries.map { it.link.uri }).containsExactly(
                    URI("https://myanimelist.net/anime/100"),
                    URI("https://myanimelist.net/anime/101"),
                    URI("https://myanimelist.net/anime/102"),
                    URI("https://myanimelist.net/anime/103"),
                    URI("https://myanimelist.net/anime/104"),
                    URI("https://myanimelist.net/anime/105"),
                    // 106 must not be in the result
                    URI("https://myanimelist.net/anime/107"),
                    URI("https://myanimelist.net/anime/108"),
                    URI("https://myanimelist.net/anime/109"),
                    URI("https://myanimelist.net/anime/110"),
                )
            }
        }

        @Test
        fun `result must not contain entries from watch list`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<FindSimilarAnimeState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findSimilarAnimeState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testState = object : State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = setOf(
                        WatchListEntry(
                            title = "Mirai Nikki (TV)",
                            link = Link("https://myanimelist.net/anime/108"),
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/13/33465.jpg"),
                        )
                    )

                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val testCache = DefaultAnimeCache(
                    cacheLoader = emptyList(),
                )

                FromRegularFileDeserializer(deserializer = AnimeFromJsonInputStreamDeserializer.instance).deserialize(testResource("search_tests/similar_anime_tests/reduced-anime-offline-database.json")).collect {
                    it.sources.forEach { source ->
                        testCache.populate(source, PresentValue(it))
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    state = testState,
                )

                // when
                defaultSearchHandler.findSimilarAnime(URI("https://myanimelist.net/anime/1"))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().entries.map { it.link.uri }).containsExactly(
                    URI("https://myanimelist.net/anime/100"),
                    URI("https://myanimelist.net/anime/101"),
                    URI("https://myanimelist.net/anime/102"),
                    URI("https://myanimelist.net/anime/103"),
                    URI("https://myanimelist.net/anime/104"),
                    URI("https://myanimelist.net/anime/105"),
                    URI("https://myanimelist.net/anime/106"),
                    URI("https://myanimelist.net/anime/107"),
                    // 108 must not be in the result
                    URI("https://myanimelist.net/anime/109"),
                    URI("https://myanimelist.net/anime/110"),
                )
            }
        }

        @Test
        fun `result must not contain entries from ignore list`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<FindSimilarAnimeState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findSimilarAnimeState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testState = object : State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                        IgnoreListEntry(
                            title = "Mirai Nikki (TV)",
                            link = Link("https://myanimelist.net/anime/104"),
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/13/33465.jpg"),
                        )
                    )
                }

                val testCache = DefaultAnimeCache(
                    cacheLoader = emptyList(),
                )

                FromRegularFileDeserializer(deserializer = AnimeFromJsonInputStreamDeserializer.instance).deserialize(testResource("search_tests/similar_anime_tests/reduced-anime-offline-database.json")).collect {
                    it.sources.forEach { source ->
                        testCache.populate(source, PresentValue(it))
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    state = testState,
                )

                // when
                defaultSearchHandler.findSimilarAnime(URI("https://myanimelist.net/anime/1"))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().entries.map { it.link.uri }).containsExactly(
                    URI("https://myanimelist.net/anime/100"),
                    URI("https://myanimelist.net/anime/101"),
                    URI("https://myanimelist.net/anime/102"),
                    URI("https://myanimelist.net/anime/103"),
                    // 104 must not be in the result
                    URI("https://myanimelist.net/anime/105"),
                    URI("https://myanimelist.net/anime/106"),
                    URI("https://myanimelist.net/anime/107"),
                    URI("https://myanimelist.net/anime/108"),
                    URI("https://myanimelist.net/anime/109"),
                    URI("https://myanimelist.net/anime/110"),
                )
            }
        }
    }

    @Nested
    inner class FindByTitleTests {

        @Test
        fun `don't do anything if the metaDataProvider is not supported`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<FindByTitleState>()
                val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testCache = object : AnimeCache by TestAnimeCache {
                    override val availableMetaDataProvider: Set<Hostname>
                        get() = setOf(MyanimelistConfig.hostname())
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    state = TestState,
                )

                // when
                defaultSearchHandler.findByTitle("example.org", "test")

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(1) // initial
            }
        }

        @Nested
        inner class AnimeListTests {

            @ParameterizedTest
            @ValueSource(strings = ["test", "TEST", "tEsT"])
            fun `AnimeList - exact match - not case sensitive`(value: String) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/1"),
                        title = "test",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )
                    val notMatching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/2"),
                        title = "other",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = listOf(matching, notMatching)
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", value)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, animelist, end
                    assertThat(receivedEvents.last().animeListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "TEST", "tEsT"])
            fun `AnimeList - search value is contained in the title - not case sensitive`(value: String) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/1"),
                        title = "someTESThere",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )
                    val notMatching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/2"),
                        title = "other",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = listOf(matching, notMatching)
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", value)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, animelist, end
                    assertThat(receivedEvents.last().animeListResults.map { it.title }).containsExactly(
                        "someTESThere",
                    )
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `AnimeList - match with levenshtein distance of 1`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/1"),
                        title = "test",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )
                    val notMatching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/2"),
                        title = "other",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = listOf(matching, notMatching)
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "t3st")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, animelist, end
                    assertThat(receivedEvents.last().animeListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `AnimeList - match with levenshtein distance of 2`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/1"),
                        title = "test",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )
                    val notMatching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/2"),
                        title = "other",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = listOf(matching, notMatching)
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "taste")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, animelist, end
                    assertThat(receivedEvents.last().animeListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `AnimeList - match with levenshtein distance of 3 or more`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/1"),
                        title = "test",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )
                    val notMatching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/2"),
                        title = "other",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = listOf(matching, notMatching)
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "73s7")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `AnimeList - levenshtein distance does not apply to contains logic`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/1"),
                        title = "taste of lemon",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )
                    val notMatching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/2"),
                        title = "other",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = listOf(matching, notMatching)
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "test")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `AnimeList - doesn't return anything for not matching anything`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/1"),
                        title = "test",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )
                    val notMatching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/2"),
                        title = "other",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = listOf(matching, notMatching)
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "different")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `AnimeList - returns multiple matching entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching1 = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/1"),
                        title = "test",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )
                    val matching2 = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/2"),
                        title = "another test",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )
                    val notMatching = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/3"),
                        title = "other",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = listOf(matching1, matching2, notMatching)
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "test")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, animelist, end
                    assertThat(receivedEvents.last().animeListResults.map { it.title }).containsExactly(
                        "test",
                        "another test",
                    )
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `AnimeList - title doesn't match anything, but the source`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = AnimeListEntry(
                        link = Link("https://example.org/anime/255"),
                        title = "test",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )
                    val notMatching = AnimeListEntry(
                        link = Link("https://example.org/anime/324"),
                        title = "other",
                        episodes = 1,
                        type = MOVIE,
                        location = Path("."),
                    )

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = listOf(matching, notMatching)
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "https://example.org/anime/255")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, animelist, end
                    assertThat(receivedEvents.last().animeListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }
        }

        @Nested
        inner class WatchListTests {

            @ParameterizedTest
            @ValueSource(strings = ["test", "TEST", "tEsT"])
            fun `WatchList - exact match - not case sensitive`(value: String) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = WatchListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = WatchListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = setOf(matching, notMatching)
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", value)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, watchlist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "TEST", "tEsT"])
            fun `WatchList - search value is contained in the title - not case sensitive`(value: String) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = WatchListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = WatchListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = setOf(matching, notMatching)
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", value)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, watchlist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `WatchList - match with levenshtein distance of 1`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = WatchListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = WatchListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = setOf(matching, notMatching)
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "t3st")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, watchlist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `WatchList - match with levenshtein distance of 2`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = WatchListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = WatchListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = setOf(matching, notMatching)
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "taste")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, watchlist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `WatchList - match with levenshtein distance of 3 or more`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = WatchListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = WatchListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = setOf(matching, notMatching)
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "73s7")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `WatchList - levenshtein distance does not apply to contains logic`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = WatchListEntry(Anime(
                        title = "taste of lemon",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = WatchListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = setOf(matching, notMatching)
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "test")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `WatchList - doesn't return anything for not matching anything`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = WatchListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = WatchListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = setOf(matching, notMatching)
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "different")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `WatchList - returns multiple matching entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching1 = WatchListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val matching2 = WatchListEntry(Anime(
                        title = "another test",
                        sources = hashSetOf(URI("https://example.org/anime/901")),
                    ))
                    val notMatching = WatchListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = setOf(matching1, matching2, notMatching)
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "test")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, watchlist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults.map { it.title }).containsExactly(
                        "test",
                        "another test",
                    )
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `WatchList - title doesn't match anything, but the source`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = WatchListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = WatchListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = setOf(matching, notMatching)
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "https://example.org/anime/255")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, watchlist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }
        }

        @Nested
        inner class IgnoreListTests {

            @ParameterizedTest
            @ValueSource(strings = ["test", "TEST", "tEsT"])
            fun `IgnoreList - exact match - not case sensitive`(value: String) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = IgnoreListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = IgnoreListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(matching, notMatching)
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", value)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, ignorelist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "TEST", "tEsT"])
            fun `IgnoreList - search value is contained in the title - not case sensitive`(value: String) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = IgnoreListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = IgnoreListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(matching, notMatching)
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", value)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, ignorelist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `IgnoreList - match with levenshtein distance of 1`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = IgnoreListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = IgnoreListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(matching, notMatching)
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "t3st")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, ignorelist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `IgnoreList - match with levenshtein distance of 2`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = IgnoreListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = IgnoreListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(matching, notMatching)
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "taste")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, ignorelist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `IgnoreList - match with levenshtein distance of 3 or more`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = IgnoreListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = IgnoreListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(matching, notMatching)
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "73s7")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `IgnoreList - levenshtein distance does not apply to contains logic`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = IgnoreListEntry(Anime(
                        title = "taste of lemon",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = IgnoreListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(matching, notMatching)
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "test")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `IgnoreList - doesn't return anything for not matching anything`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = IgnoreListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = IgnoreListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(matching, notMatching)
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "different")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `IgnoreList - returns multiple matching entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching1 = IgnoreListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val matching2 = IgnoreListEntry(Anime(
                        title = "another test",
                        sources = hashSetOf(URI("https://example.org/anime/901")),
                    ))
                    val notMatching = IgnoreListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(matching1, matching2, notMatching)
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "test")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, ignorelist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults.map { it.title }).containsExactly(
                        "test",
                        "another test",
                    )
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `IgnoreList - title doesn't match anything, but the source`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")
                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf()
                    }

                    val matching = IgnoreListEntry(Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    ))
                    val notMatching = IgnoreListEntry(Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    ))

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(matching, notMatching)
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "https://example.org/anime/255")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(4) // initial, start, ignorelist, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults.map { it.title }).containsExactly(
                        "test",
                    )
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }
        }

        @Nested
        inner class UnlistedTests {

            @ParameterizedTest
            @ValueSource(strings = ["test", "TEST", "tEsT"])
            fun `Unlisted - exact match - not case sensitive`(value: String) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val matching = Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    )
                    val notMatching = Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    )

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf(matching, notMatching)
                    }

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", value)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, unlisted + end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults.map { it.title }).containsExactly(
                        "test",
                    )
                }
            }

            @ParameterizedTest
            @ValueSource(strings = ["test", "TEST", "tEsT"])
            fun `Unlisted - search value is contained in the title - not case sensitive`(value: String) {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val matching = Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    )
                    val notMatching = Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    )

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf(matching, notMatching)
                    }

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", value)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, unlisted + end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults.map { it.title }).containsExactly(
                        "test",
                    )
                }
            }

            @Test
            fun `Unlisted - match with levenshtein distance of 1`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val matching = Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    )
                    val notMatching = Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    )

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf(matching, notMatching)
                    }

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "t3st")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, unlisted + end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults.map { it.title }).containsExactly(
                        "test",
                    )
                }
            }

            @Test
            fun `Unlisted - match with levenshtein distance of 2`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val matching = Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    )
                    val notMatching = Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    )

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf(matching, notMatching)
                    }

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "taste")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, unlisted + end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults.map { it.title }).containsExactly(
                        "test",
                    )
                }
            }

            @Test
            fun `Unlisted - match with levenshtein distance of 3 or more`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val matching = Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    )
                    val notMatching = Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    )

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf(matching, notMatching)
                    }

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "73s7")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `Unlisted - levenshtein distance does not apply to contains logic`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val matching = Anime(
                        title = "taste of lemon",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    )
                    val notMatching = Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    )

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf(matching, notMatching)
                    }

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "test")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `Unlisted - doesn't return anything for not matching anything`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val matching = Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    )
                    val notMatching = Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    )

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf(matching, notMatching)
                    }

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "different")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults).isEmpty()
                }
            }

            @Test
            fun `Unlisted - returns multiple matching entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val matching1 = Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    )
                    val matching2 = Anime(
                        title = "another test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    )
                    val notMatching = Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    )

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf(matching1, matching2, notMatching)
                    }

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "test")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, unlisted + end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults.map { it.title }).containsExactly(
                        "test",
                        "another test",
                    )
                }
            }

            @Test
            fun `Unlisted - title doesn't match anything, but the source if it doesn't exist in cache, it will be loaded externally`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val matching = Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                    )
                    val notMatching = Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    )

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf(matching, notMatching)
                        override suspend fun fetch(key: URI): CacheEntry<Anime> = PresentValue(matching)
                    }

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "https://example.org/anime/255")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, unlisted + end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults.map { it.title }).containsExactly(
                        "test",
                    )
                }
            }

            @Test
            fun `Unlisted - doesn't match the title, but a synonym`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<FindByTitleState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val matching = Anime(
                        title = "test",
                        sources = hashSetOf(URI("https://example.org/anime/255")),
                        synonyms = hashSetOf("alternative title"),
                    )
                    val notMatching = Anime(
                        title = "other",
                        sources = hashSetOf(URI("https://example.org/anime/324")),
                    )

                    val testCache = object : AnimeCache by TestAnimeCache {
                        override val availableMetaDataProvider: Set<Hostname>
                            get() = setOf("example.org")

                        override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = sequenceOf(matching, notMatching)
                    }

                    val testState = object: State by TestState {
                        override fun animeList(): List<AnimeListEntry> = emptyList()
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val defaultSearchHandler = DefaultSearchHandler(
                        cache = testCache,
                        eventBus = CoroutinesFlowEventBus,
                        state = testState,
                    )

                    // when
                    defaultSearchHandler.findByTitle("example.org", "alternative title")

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, end
                    assertThat(receivedEvents.last().animeListResults).isEmpty()
                    assertThat(receivedEvents.last().watchListResults).isEmpty()
                    assertThat(receivedEvents.last().ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().unlistedResults.map { it.title }).containsExactly(
                        "test",
                    )
                }
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultSearchHandler.instance

            // when
            val result = DefaultSearchHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultSearchHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}