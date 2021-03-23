package io.github.manamiproject.manami.app.search

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.cache.TestCacheLoader
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.TestEventBus
import io.github.manamiproject.modb.core.collections.SortedList
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Status.UPCOMING
import io.github.manamiproject.modb.core.models.Anime.Type.*
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.net.URI

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
                sources = SortedList(
                    URI("https://anidb.net/anime/15738"),
                    URI("https://anilist.co/anime/124194"),
                    URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                    URI("https://kitsu.io/anime/43578"),
                    URI("https://myanimelist.net/anime/42938"),
                    URI("https://notify.moe/anime/YiySZ9OMg"),
                ),
                _title = "Fruits Basket: The Final",
                type = TV,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val matchingEntry2 = Anime(
                sources = SortedList(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.io/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                    URI("https://notify.moe/anime/_RdVrLpGR"),
                ),
                _title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                type = OVA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val notMatching1 = Anime(
                sources = SortedList(
                    URI("https://anidb.net/anime/15070"),
                    URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                    URI("https://myanimelist.net/anime/40356"),
                    URI("https://notify.moe/anime/rBaaLj2Wg"),
                ),
                _title = "Tate no Yuusha no Nariagari Season 2",
                type = TV,
                episodes = 0,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2021,
                ),
            )

            val notMatching2 = Anime(
                sources = SortedList(
                    URI("https://myanimelist.net/anime/46587"),
                ),
                _title = "Tenchi Souzou Design-bu Special",
                type = ONA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = UNDEFINED,
                    year = 2021
                ),
            )

            val notMatching3 = Anime(
                sources = SortedList(
                    URI("https://kitsu.io/anime/40614"),
                    URI("https://myanimelist.net/anime/34705"),
                    URI("https://notify.moe/anime/3I2v2FmiR"),
                ),
                _title = "Tejina Shi",
                type = Movie,
                episodes = 1,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = UNDEFINED,
                    year = 0
                ),
            )

            val testCache = AnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
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
            sleep(1000)
            assertThat(receivedEvents).hasSize(3)

            assertThat(receivedEvents.filterIsInstance<AnimeSeasonEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Fruits Basket: The Final",
                "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA"
            )

            assertThat(receivedEvents.last()).isInstanceOf(AnimeSeasonSearchFinishedEvent::class.java)
        }

        @Test
        fun `exclude entries in animelist`() {
            // given
            val matchingEntry1 = Anime(
                sources = SortedList(
                    URI("https://anidb.net/anime/15738"),
                    URI("https://anilist.co/anime/124194"),
                    URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                    URI("https://kitsu.io/anime/43578"),
                    URI("https://myanimelist.net/anime/42938"),
                    URI("https://notify.moe/anime/YiySZ9OMg"),
                ),
                _title = "Fruits Basket: The Final",
                type = TV,
                episodes = 24,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val matchingEntry2 = Anime(
                sources = SortedList(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.io/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                    URI("https://notify.moe/anime/_RdVrLpGR"),
                ),
                _title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                type = OVA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = listOf(
                    AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/42938"),
                        title = "Fruits Basket: The Final",
                        episodes = 24,
                        type = TV,
                        location = URI("/test"),
                    )
                )
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val testCache = AnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
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
            sleep(1000)
            assertThat(receivedEvents.filterIsInstance<AnimeSeasonEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA"
            )
        }

        @Test
        fun `exclude entries in watchlist`() {
            // given
            val matchingEntry1 = Anime(
                sources = SortedList(
                    URI("https://anidb.net/anime/15738"),
                    URI("https://anilist.co/anime/124194"),
                    URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                    URI("https://kitsu.io/anime/43578"),
                    URI("https://myanimelist.net/anime/42938"),
                    URI("https://notify.moe/anime/YiySZ9OMg"),
                ),
                _title = "Fruits Basket: The Final",
                type = TV,
                episodes = 24,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val matchingEntry2 = Anime(
                sources = SortedList(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.io/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                    URI("https://notify.moe/anime/_RdVrLpGR"),
                ),
                _title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
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
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif")
                    )
                )
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val testCache = AnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
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
            sleep(1000)
            assertThat(receivedEvents.filterIsInstance<AnimeSeasonEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA"
            )
        }

        @Test
        fun `exclude entries in ignorelist`() {
            // given
            val matchingEntry1 = Anime(
                sources = SortedList(
                    URI("https://anidb.net/anime/15738"),
                    URI("https://anilist.co/anime/124194"),
                    URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                    URI("https://kitsu.io/anime/43578"),
                    URI("https://myanimelist.net/anime/42938"),
                    URI("https://notify.moe/anime/YiySZ9OMg"),
                ),
                _title = "Fruits Basket: The Final",
                type = TV,
                episodes = 24,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val matchingEntry2 = Anime(
                sources = SortedList(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.io/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                    URI("https://notify.moe/anime/_RdVrLpGR"),
                ),
                _title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
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
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif")
                    )
                )
            }

            val testCache = AnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
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
            sleep(1000)
            assertThat(receivedEvents.filterIsInstance<AnimeSeasonEntryFoundEvent>().map { it.anime.title }).containsExactlyInAnyOrder(
                "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA"
            )
        }
    }

    @Nested
    inner class AvailableMetaDataProvidersTests {

        @Test
        fun `return the values from the AnimeCache`() {
            // given
            val entry = Anime(
                sources = SortedList(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.io/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                    URI("https://notify.moe/anime/_RdVrLpGR"),
                ),
                _title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                type = OVA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val testCache = AnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
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
                "kitsu.io",
                "myanimelist.net",
                "notify.moe",
            )
        }
    }
}