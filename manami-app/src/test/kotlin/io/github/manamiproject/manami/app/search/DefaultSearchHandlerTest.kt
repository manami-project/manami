package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.TestEventBus
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
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
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.serde.json.deserializer.AnimeFromJsonInputStreamDeserializer
import io.github.manamiproject.modb.serde.json.deserializer.FromRegularFileDeserializer
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Paths
import kotlin.io.path.createDirectory

internal class DefaultSearchHandlerTest {

    @Nested
    inner class FindSeasonTests {

        @Test
        fun `find all entries that match the specific season and meta data provider`() {
            // given
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
                    URI("https://notify.moe/anime/YiySZ9OMg"),
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
                    URI("https://notify.moe/anime/_RdVrLpGR"),
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
                    URI("https://notify.moe/anime/rBaaLj2Wg"),
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
                    URI("https://notify.moe/anime/3I2v2FmiR"),
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

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = testState,
                cache = testCache,
                eventBus = testEventBus,
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
            assertThat(receivedEvents).hasSize(3)
            assertThat(receivedEvents.filterIsInstance<AnimeSeasonEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Fruits Basket: The Final",
                "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
            )
            assertThat(receivedEvents.last()).isInstanceOf(AnimeSeasonSearchFinishedEvent::class.java)
        }

        @Test
        fun `exclude entries in animelist`() {
            tempDirectory {
                // given
                val matchingEntry1 = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                        URI("https://notify.moe/anime/YiySZ9OMg"),
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
                        URI("https://notify.moe/anime/_RdVrLpGR"),
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

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = testEventBus,
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
                assertThat(receivedEvents.filterIsInstance<AnimeSeasonEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                    "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                )
            }
        }

        @Test
        fun `exclude entries in watchlist`() {
            // given
            val matchingEntry1 = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15738"),
                    URI("https://anilist.co/anime/124194"),
                    URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                    URI("https://kitsu.app/anime/43578"),
                    URI("https://myanimelist.net/anime/42938"),
                    URI("https://notify.moe/anime/YiySZ9OMg"),
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
                    URI("https://notify.moe/anime/_RdVrLpGR"),
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

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = testState,
                cache = testCache,
                eventBus = testEventBus,
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
            assertThat(receivedEvents.filterIsInstance<AnimeSeasonEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
            )
        }

        @Test
        fun `exclude entries in ignorelist`() {
            // given
            val matchingEntry1 = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15738"),
                    URI("https://anilist.co/anime/124194"),
                    URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                    URI("https://kitsu.app/anime/43578"),
                    URI("https://myanimelist.net/anime/42938"),
                    URI("https://notify.moe/anime/YiySZ9OMg"),
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
                    URI("https://notify.moe/anime/_RdVrLpGR"),
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

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = testState,
                cache = testCache,
                eventBus = testEventBus,
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
            assertThat(receivedEvents.filterIsInstance<AnimeSeasonEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
            )
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
                    URI("https://notify.moe/anime/_RdVrLpGR"),
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
                eventBus = TestEventBus,
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
                "notify.moe",
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
                    URI("https://notify.moe/anime/_RdVrLpGR"),
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
                eventBus = TestEventBus,
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
    inner class FindByTagsTests {

        @Test
        fun `find all entries that contain all tags`() {
            // given
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
                    URI("https://notify.moe/anime/YiySZ9OMg"),
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
                    URI("https://notify.moe/anime/rBaaLj2Wg"),
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

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = testState,
                cache = testCache,
                eventBus = testEventBus,
            )

            // when
            defaultSearchHandler.findByTag(
                tags = setOf("my-tag-1", "my-tag-2"),
                metaDataProvider = "myanimelist.net",
                searchType = AND,
            )

            // then
            assertThat(receivedEvents).hasSize(2)
            assertThat(receivedEvents.filterIsInstance<AnimeSearchEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Fruits Basket: The Final",
            )
            assertThat(receivedEvents.last()).isInstanceOf(AnimeSearchFinishedEvent::class.java)
        }

        @Test
        fun `find all entries that contain at least one of the given tags`() {
            // given
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
                    URI("https://notify.moe/anime/YiySZ9OMg"),
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
                    URI("https://notify.moe/anime/rBaaLj2Wg"),
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
                    URI("https://notify.moe/anime/_RdVrLpGR"),
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

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = testState,
                cache = testCache,
                eventBus = testEventBus,
            )

            // when
            defaultSearchHandler.findByTag(
                tags = setOf("my-tag-1", "my-tag-2"),
                metaDataProvider = "myanimelist.net",
                searchType = OR,
            )

            // then
            assertThat(receivedEvents).hasSize(4)
            assertThat(receivedEvents.filterIsInstance<AnimeSearchEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Fruits Basket: The Final",
                "Tate no Yuusha no Nariagari Season 2",
                "Tenchi Souzou Design-bu Special",
            )
            assertThat(receivedEvents.last()).isInstanceOf(AnimeSearchFinishedEvent::class.java)
        }

        @Test
        fun `exclude entries in animelist`() {
            tempDirectory {
                // given
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
                        URI("https://notify.moe/anime/YiySZ9OMg"),
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
                        URI("https://notify.moe/anime/rBaaLj2Wg"),
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
                        URI("https://notify.moe/anime/_RdVrLpGR"),
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

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = testEventBus,
                )

                // when
                defaultSearchHandler.findByTag(
                    tags = setOf("my-tag-1", "my-tag-2"),
                    metaDataProvider = "myanimelist.net",
                    searchType = OR,
                )

                // then
                assertThat(receivedEvents).hasSize(3)
                assertThat(receivedEvents.filterIsInstance<AnimeSearchEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                    "Tate no Yuusha no Nariagari Season 2",
                    "Tenchi Souzou Design-bu Special",
                )
                assertThat(receivedEvents.last()).isInstanceOf(AnimeSearchFinishedEvent::class.java)
            }
        }

        @Test
        fun `exclude entries in watchlist`() {
            // given
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
                    URI("https://notify.moe/anime/YiySZ9OMg"),
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
                    URI("https://notify.moe/anime/rBaaLj2Wg"),
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
                    URI("https://notify.moe/anime/_RdVrLpGR"),
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

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = testState,
                cache = testCache,
                eventBus = testEventBus,
            )

            // when
            defaultSearchHandler.findByTag(
                tags = setOf("my-tag-1", "my-tag-2"),
                metaDataProvider = "myanimelist.net",
                searchType = OR,
            )

            // then
            assertThat(receivedEvents).hasSize(3)
            assertThat(receivedEvents.filterIsInstance<AnimeSearchEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Tate no Yuusha no Nariagari Season 2",
                "Tenchi Souzou Design-bu Special",
            )
            assertThat(receivedEvents.last()).isInstanceOf(AnimeSearchFinishedEvent::class.java)
        }

        @Test
        fun `exclude entries in ignorelist`() {
            // given
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
                    URI("https://notify.moe/anime/YiySZ9OMg"),
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
                    URI("https://notify.moe/anime/rBaaLj2Wg"),
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
                    URI("https://notify.moe/anime/_RdVrLpGR"),
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

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = testState,
                cache = testCache,
                eventBus = testEventBus,
            )

            // when
            defaultSearchHandler.findByTag(
                tags = setOf("my-tag-1", "my-tag-2"),
                metaDataProvider = "myanimelist.net",
                searchType = OR,
            )

            // then
            assertThat(receivedEvents).hasSize(3)
            assertThat(receivedEvents.filterIsInstance<AnimeSearchEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Tate no Yuusha no Nariagari Season 2",
                "Tenchi Souzou Design-bu Special",
            )
            assertThat(receivedEvents.last()).isInstanceOf(AnimeSearchFinishedEvent::class.java)
        }

        @Test
        fun `filter by status - default is all available`() {
            tempDirectory {
                // given
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val finishedEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                        URI("https://notify.moe/anime/YiySZ9OMg"),
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

                val ongoingEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15070"),
                        URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                        URI("https://myanimelist.net/anime/40356"),
                        URI("https://notify.moe/anime/rBaaLj2Wg"),
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

                val upcomingEntry = Anime(
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

                val unknownEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15807"),
                        URI("https://anilist.co/anime/125368"),
                        URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                        URI("https://kitsu.app/anime/43731"),
                        URI("https://myanimelist.net/anime/43609"),
                        URI("https://notify.moe/anime/_RdVrLpGR"),
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
                    finishedEntry.sources.forEach {
                        populate(it, PresentValue(finishedEntry))
                    }
                    ongoingEntry.sources.forEach {
                        populate(it, PresentValue(ongoingEntry))
                    }
                    upcomingEntry.sources.forEach {
                        populate(it, PresentValue(upcomingEntry))
                    }
                    unknownEntry.sources.forEach {
                        populate(it, PresentValue(unknownEntry))
                    }
                }

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = testEventBus,
                )

                // when
                defaultSearchHandler.findByTag(
                    tags = emptySet(),
                    metaDataProvider = "myanimelist.net",
                    searchType = OR,
                )

                // then
                assertThat(receivedEvents).hasSize(5)
                assertThat(receivedEvents.filterIsInstance<AnimeSearchEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                    "Fruits Basket: The Final",
                    "Tate no Yuusha no Nariagari Season 2",
                    "Tenchi Souzou Design-bu Special",
                    "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                )
                assertThat(receivedEvents.last()).isInstanceOf(AnimeSearchFinishedEvent::class.java)
            }
        }

        @Test
        fun `filter by status FINISHED`() {
            tempDirectory {
                // given
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val finishedEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                        URI("https://notify.moe/anime/YiySZ9OMg"),
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

                val ongoingEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15070"),
                        URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                        URI("https://myanimelist.net/anime/40356"),
                        URI("https://notify.moe/anime/rBaaLj2Wg"),
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

                val upcomingEntry = Anime(
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

                val unknownEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15807"),
                        URI("https://anilist.co/anime/125368"),
                        URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                        URI("https://kitsu.app/anime/43731"),
                        URI("https://myanimelist.net/anime/43609"),
                        URI("https://notify.moe/anime/_RdVrLpGR"),
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
                    finishedEntry.sources.forEach {
                        populate(it, PresentValue(finishedEntry))
                    }
                    ongoingEntry.sources.forEach {
                        populate(it, PresentValue(ongoingEntry))
                    }
                    upcomingEntry.sources.forEach {
                        populate(it, PresentValue(upcomingEntry))
                    }
                    unknownEntry.sources.forEach {
                        populate(it, PresentValue(unknownEntry))
                    }
                }

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = testEventBus,
                )

                // when
                defaultSearchHandler.findByTag(
                    tags = emptySet(),
                    metaDataProvider = "myanimelist.net",
                    searchType = OR,
                    status = setOf(FINISHED)
                )

                // then
                assertThat(receivedEvents).hasSize(2)
                assertThat(receivedEvents.filterIsInstance<AnimeSearchEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                    "Fruits Basket: The Final",
                )
                assertThat(receivedEvents.last()).isInstanceOf(AnimeSearchFinishedEvent::class.java)
            }
        }

        @Test
        fun `filter by status ONGOING`() {
            tempDirectory {
                // given
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val finishedEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                        URI("https://notify.moe/anime/YiySZ9OMg"),
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

                val ongoingEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15070"),
                        URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                        URI("https://myanimelist.net/anime/40356"),
                        URI("https://notify.moe/anime/rBaaLj2Wg"),
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

                val upcomingEntry = Anime(
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

                val unknownEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15807"),
                        URI("https://anilist.co/anime/125368"),
                        URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                        URI("https://kitsu.app/anime/43731"),
                        URI("https://myanimelist.net/anime/43609"),
                        URI("https://notify.moe/anime/_RdVrLpGR"),
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
                    finishedEntry.sources.forEach {
                        populate(it, PresentValue(finishedEntry))
                    }
                    ongoingEntry.sources.forEach {
                        populate(it, PresentValue(ongoingEntry))
                    }
                    upcomingEntry.sources.forEach {
                        populate(it, PresentValue(upcomingEntry))
                    }
                    unknownEntry.sources.forEach {
                        populate(it, PresentValue(unknownEntry))
                    }
                }

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = testEventBus,
                )

                // when
                defaultSearchHandler.findByTag(
                    tags = emptySet(),
                    metaDataProvider = "myanimelist.net",
                    searchType = OR,
                    status = setOf(ONGOING)
                )

                // then
                assertThat(receivedEvents).hasSize(2)
                assertThat(receivedEvents.filterIsInstance<AnimeSearchEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                    "Tate no Yuusha no Nariagari Season 2",
                )
                assertThat(receivedEvents.last()).isInstanceOf(AnimeSearchFinishedEvent::class.java)
            }
        }

        @Test
        fun `filter by status UPCOMING`() {
            tempDirectory {
                // given
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val finishedEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                        URI("https://notify.moe/anime/YiySZ9OMg"),
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

                val ongoingEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15070"),
                        URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                        URI("https://myanimelist.net/anime/40356"),
                        URI("https://notify.moe/anime/rBaaLj2Wg"),
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

                val upcomingEntry = Anime(
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

                val unknownEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15807"),
                        URI("https://anilist.co/anime/125368"),
                        URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                        URI("https://kitsu.app/anime/43731"),
                        URI("https://myanimelist.net/anime/43609"),
                        URI("https://notify.moe/anime/_RdVrLpGR"),
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
                    finishedEntry.sources.forEach {
                        populate(it, PresentValue(finishedEntry))
                    }
                    ongoingEntry.sources.forEach {
                        populate(it, PresentValue(ongoingEntry))
                    }
                    upcomingEntry.sources.forEach {
                        populate(it, PresentValue(upcomingEntry))
                    }
                    unknownEntry.sources.forEach {
                        populate(it, PresentValue(unknownEntry))
                    }
                }

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = testEventBus,
                )

                // when
                defaultSearchHandler.findByTag(
                    tags = emptySet(),
                    metaDataProvider = "myanimelist.net",
                    searchType = OR,
                    status = setOf(UPCOMING)
                )

                // then
                assertThat(receivedEvents).hasSize(2)
                assertThat(receivedEvents.filterIsInstance<AnimeSearchEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                    "Tenchi Souzou Design-bu Special",
                )
                assertThat(receivedEvents.last()).isInstanceOf(AnimeSearchFinishedEvent::class.java)
            }
        }

        @Test
        fun `filter by status UNKNOWN`() {
            tempDirectory {
                // given
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val finishedEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
                        URI("https://notify.moe/anime/YiySZ9OMg"),
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

                val ongoingEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15070"),
                        URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                        URI("https://myanimelist.net/anime/40356"),
                        URI("https://notify.moe/anime/rBaaLj2Wg"),
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

                val upcomingEntry = Anime(
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

                val unknownEntry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15807"),
                        URI("https://anilist.co/anime/125368"),
                        URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                        URI("https://kitsu.app/anime/43731"),
                        URI("https://myanimelist.net/anime/43609"),
                        URI("https://notify.moe/anime/_RdVrLpGR"),
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
                    finishedEntry.sources.forEach {
                        populate(it, PresentValue(finishedEntry))
                    }
                    ongoingEntry.sources.forEach {
                        populate(it, PresentValue(ongoingEntry))
                    }
                    upcomingEntry.sources.forEach {
                        populate(it, PresentValue(upcomingEntry))
                    }
                    unknownEntry.sources.forEach {
                        populate(it, PresentValue(unknownEntry))
                    }
                }

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    state = testState,
                    cache = testCache,
                    eventBus = testEventBus,
                )

                // when
                defaultSearchHandler.findByTag(
                    tags = emptySet(),
                    metaDataProvider = "myanimelist.net",
                    searchType = OR,
                    status = setOf(UNKNOWN)
                )

                // then
                assertThat(receivedEvents).hasSize(2)
                assertThat(receivedEvents.filterIsInstance<AnimeSearchEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                    "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                )
                assertThat(receivedEvents.last()).isInstanceOf(AnimeSearchFinishedEvent::class.java)
            }
        }
    }

    @Nested
    inner class FindByUriTests {

        @Test
        fun `successfully find anime`() {
            // given
            val entry = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15738"),
                    URI("https://anilist.co/anime/124194"),
                    URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                    URI("https://kitsu.app/anime/43578"),
                    URI("https://myanimelist.net/anime/42938"),
                    URI("https://notify.moe/anime/YiySZ9OMg"),
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

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = TestState,
                cache = testCache,
                eventBus = testEventBus,
            )

            // when
            defaultSearchHandler.find(URI("https://myanimelist.net/anime/42938"))

            // then
            assertThat(receivedEvents).hasSize(2)
            assertThat(receivedEvents.filterIsInstance<AnimeEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Fruits Basket: The Final",
            )
            assertThat(receivedEvents.last()).isInstanceOf(AnimeEntryFinishedEvent::class.java)
        }

        @Test
        fun `don't return anime, because URI relates to a dead entry`() {
            // given
            val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                populate(URI("https://myanimelist.net/anime/10001"), DeadEntry())
            }

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                state = TestState,
                cache = testCache,
                eventBus = testEventBus,
            )

            // when
            defaultSearchHandler.find(URI("https://myanimelist.net/anime/10001"))

            // then
            assertThat(receivedEvents).hasSize(1)
            assertThat(receivedEvents.first()).isInstanceOf(AnimeEntryFinishedEvent::class.java)
        }
    }

    @Nested
    inner class FindSimilarAnimeTests {

        @Test
        fun `return with finished event if the host is not a supported metadata provider`() {
            // given
            val testCache = object : AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname())
            }

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                cache = testCache,
                eventBus = testEventBus,
                state = TestState,
            )

            // when
            defaultSearchHandler.findSimilarAnime(URI("https://example.org/anime/1535"))

            // then
            assertThat(receivedEvents).containsExactly(SimilarAnimeSearchFinishedEvent)
        }

        @Test
        fun `return with finished event if the uri represents a dead entry`() {
            // given
            val testCache = object : AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname())

                override fun fetch(key: URI): CacheEntry<Anime> = DeadEntry()
            }

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultSearchHandler = DefaultSearchHandler(
                cache = testCache,
                eventBus = testEventBus,
                state = TestState,
            )

            // when
            defaultSearchHandler.findSimilarAnime(URI("https://myanimelist.net/anime/10"))

            // then
            assertThat(receivedEvents).containsExactly(SimilarAnimeSearchFinishedEvent)
        }

        @Test
        fun `return 10 anime having the most amount tags which reside in both anime sorted by number of number of matching tags desc`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object : EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }

                    override fun subscribe(subscriber: Any) {}
                }

                val testState = object : State by TestState {
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val testCache = DefaultAnimeCache(
                    cacheLoader = emptyList(),
                    eventBus = testEventBus,
                )

                FromRegularFileDeserializer(deserializer = AnimeFromJsonInputStreamDeserializer.instance).deserialize(testResource("search_tests/similar_anime_tests/reduced-anime-offline-database.json")).collect {
                    it.sources.forEach { source ->
                        testCache.populate(source, PresentValue(it))
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    eventBus = testEventBus,
                    state = testState,
                )

                // when
                defaultSearchHandler.findSimilarAnime(URI("https://myanimelist.net/anime/1"))

                // then
                val receivedEvent: Event? = receivedEvents.find { it is SimilarAnimeFoundEvent }
                val event = receivedEvent as SimilarAnimeFoundEvent

                assertThat(event.entries.map { it.sources.first() }).containsExactly(
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
                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object : EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }

                    override fun subscribe(subscriber: Any) {}
                }

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
                    eventBus = testEventBus,
                )

                FromRegularFileDeserializer(deserializer = AnimeFromJsonInputStreamDeserializer.instance).deserialize(testResource("search_tests/similar_anime_tests/reduced-anime-offline-database.json")).collect {
                    it.sources.forEach { source ->
                        testCache.populate(source, PresentValue(it))
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    eventBus = testEventBus,
                    state = testState,
                )

                // when
                defaultSearchHandler.findSimilarAnime(URI("https://myanimelist.net/anime/1"))

                // then
                val receivedEvent: Event? = receivedEvents.find { it is SimilarAnimeFoundEvent }
                val event = receivedEvent as SimilarAnimeFoundEvent

                assertThat(event.entries.map { it.sources.first() }).containsExactly(
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
                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object : EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }

                    override fun subscribe(subscriber: Any) {}
                }

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
                    eventBus = testEventBus,
                )

                FromRegularFileDeserializer(deserializer = AnimeFromJsonInputStreamDeserializer.instance).deserialize(testResource("search_tests/similar_anime_tests/reduced-anime-offline-database.json")).collect {
                    it.sources.forEach { source ->
                        testCache.populate(source, PresentValue(it))
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    eventBus = testEventBus,
                    state = testState,
                )

                // when
                defaultSearchHandler.findSimilarAnime(URI("https://myanimelist.net/anime/1"))

                // then
                val receivedEvent: Event? = receivedEvents.find { it is SimilarAnimeFoundEvent }
                val event = receivedEvent as SimilarAnimeFoundEvent

                assertThat(event.entries.map { it.sources.first() }).containsExactly(
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
                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object : EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }

                    override fun subscribe(subscriber: Any) {}
                }

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
                    eventBus = testEventBus,
                )

                FromRegularFileDeserializer(deserializer = AnimeFromJsonInputStreamDeserializer.instance).deserialize(testResource("search_tests/similar_anime_tests/reduced-anime-offline-database.json")).collect {
                    it.sources.forEach { source ->
                        testCache.populate(source, PresentValue(it))
                    }
                }

                val defaultSearchHandler = DefaultSearchHandler(
                    cache = testCache,
                    eventBus = testEventBus,
                    state = testState,
                )

                // when
                defaultSearchHandler.findSimilarAnime(URI("https://myanimelist.net/anime/1"))

                // then
                val receivedEvent: Event? = receivedEvents.find { it is SimilarAnimeFoundEvent }
                val event = receivedEvent as SimilarAnimeFoundEvent

                assertThat(event.entries.map { it.sources.first() }).containsExactly(
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