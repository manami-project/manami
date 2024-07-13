package io.github.manamiproject.manami.app.relatedanime

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.cache.TestAnimeCache
import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.EventListType.ANIME_LIST
import io.github.manamiproject.manami.app.events.EventListType.IGNORE_LIST
import io.github.manamiproject.manami.app.events.TestEventBus
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Type.SPECIAL
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.FALL
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.WINTER
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.io.path.createDirectory

internal class DefaultRelatedAnimeHandlerTest {

    @Nested
    inner class FindRelatedAnimeForAnimeListTests {

        @Test
        fun `find related anime for entries in animelist`() {
            tempDirectory {
                // given
                val testLocation = tempDir.resolve("test1").createDirectory().toAbsolutePath()

                val testCache = object: AnimeCache by TestAnimeCache {
                    override fun fetch(key: URI): CacheEntry<Anime> {
                        return when(key) {
                            URI("https://myanimelist.net/anime/31646") -> PresentValue(anime1)
                            URI("https://myanimelist.net/anime/35180") -> PresentValue(anime2)
                            URI("https://myanimelist.net/anime/28789") -> PresentValue(anime3)
                            URI("https://myanimelist.net/anime/34647") -> PresentValue(anime4)
                            URI("https://myanimelist.net/anime/38154") -> PresentValue(anime5)
                            URI("https://myanimelist.net/anime/34611") -> PresentValue(anime6)
                            URI("https://myanimelist.net/anime/38864") -> PresentValue(anime7)
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = listOf(
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/35180"),
                            title = "3-gatsu no Lion 2nd Season",
                            episodes = 22,
                            type = TV,
                            location = testLocation,
                        )
                    )
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultRelatedAnimeHandler = DefaultRelatedAnimeHandler(
                    cache = testCache,
                    state = testState,
                    eventBus = testEventBus,
                )

                // when
                defaultRelatedAnimeHandler.findRelatedAnimeForAnimeList()

                // then
                val animeFoundEvents = receivedEvents.filterIsInstance<RelatedAnimeFoundEvent>()
                assertThat(animeFoundEvents).hasSize(6)
                assertThat(animeFoundEvents.map { it.listType }.distinct()).containsExactly(ANIME_LIST)

                val foundEntries = animeFoundEvents.map { it.anime.sources.first() }
                assertThat(foundEntries).containsExactlyInAnyOrder(
                    anime1.sources.first(),
                    anime3.sources.first(),
                    anime4.sources.first(),
                    anime5.sources.first(),
                    anime6.sources.first(),
                    anime7.sources.first(),
                )

                val statusEvents = receivedEvents.filterIsInstance<RelatedAnimeStatusEvent>()
                assertThat(statusEvents).hasSize(7)
                assertThat(statusEvents.map { it.listType }.distinct()).containsExactly(ANIME_LIST)

                assertThat(receivedEvents.last()).isInstanceOf(RelatedAnimeFinishedEvent::class.java)
                assertThat((receivedEvents.last() as RelatedAnimeFinishedEvent).listType).isEqualTo(ANIME_LIST)
            }
        }

        @Test
        fun `find related anime for entries in animelist and exclude entries in watchlist`() {
            tempDirectory {
                // given
                val testLocation = tempDir.resolve("test").createDirectory().toAbsolutePath()

                val testCache = object: AnimeCache by TestAnimeCache {
                    override fun fetch(key: URI): CacheEntry<Anime> {
                        return when(key) {
                            URI("https://myanimelist.net/anime/31646") -> PresentValue(anime1)
                            URI("https://myanimelist.net/anime/35180") -> PresentValue(anime2)
                            URI("https://myanimelist.net/anime/28789") -> PresentValue(anime3)
                            URI("https://myanimelist.net/anime/34647") -> PresentValue(anime4)
                            URI("https://myanimelist.net/anime/38154") -> PresentValue(anime5)
                            URI("https://myanimelist.net/anime/34611") -> PresentValue(anime6)
                            URI("https://myanimelist.net/anime/38864") -> PresentValue(anime7)
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = listOf(
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/35180"),
                            title = "3-gatsu no Lion 2nd Season",
                            episodes = 22,
                            type = TV,
                            location = testLocation,
                        )
                    )
                    override fun watchList(): Set<WatchListEntry> = setOf(WatchListEntry(anime5), WatchListEntry(anime7))
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                }

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultRelatedAnimeHandler = DefaultRelatedAnimeHandler(
                    cache = testCache,
                    state = testState,
                    eventBus = testEventBus,
                )

                // when
                defaultRelatedAnimeHandler.findRelatedAnimeForAnimeList()

                // then
                val animeFoundEvents = receivedEvents.filterIsInstance<RelatedAnimeFoundEvent>()
                assertThat(animeFoundEvents).hasSize(4)

                val foundEntries = animeFoundEvents.map { it.anime.sources.first() }
                assertThat(foundEntries).containsExactlyInAnyOrder(
                    anime1.sources.first(),
                    anime3.sources.first(),
                    anime4.sources.first(),
                    anime6.sources.first(),
                )
            }
        }

        @Test
        fun `find related anime for entries in animelist and exclude entries in ignorelist`() {
            tempDirectory {
                // given
                val testLocation = tempDir.resolve("test").createDirectory().toAbsolutePath()

                val testCache = object: AnimeCache by TestAnimeCache {
                    override fun fetch(key: URI): CacheEntry<Anime> {
                        return when(key) {
                            URI("https://myanimelist.net/anime/31646") -> PresentValue(anime1)
                            URI("https://myanimelist.net/anime/35180") -> PresentValue(anime2)
                            URI("https://myanimelist.net/anime/28789") -> PresentValue(anime3)
                            URI("https://myanimelist.net/anime/34647") -> PresentValue(anime4)
                            URI("https://myanimelist.net/anime/38154") -> PresentValue(anime5)
                            URI("https://myanimelist.net/anime/34611") -> PresentValue(anime6)
                            URI("https://myanimelist.net/anime/38864") -> PresentValue(anime7)
                            else -> shouldNotBeInvoked()
                        }
                    }
                }
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = listOf(
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/35180"),
                            title = "3-gatsu no Lion 2nd Season",
                            episodes = 22,
                            type = TV,
                            location = testLocation,
                        )
                    )

                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = setOf(IgnoreListEntry(anime3), IgnoreListEntry(anime4))
                }

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultRelatedAnimeHandler = DefaultRelatedAnimeHandler(
                    cache = testCache,
                    state = testState,
                    eventBus = testEventBus,
                )

                // when
                defaultRelatedAnimeHandler.findRelatedAnimeForAnimeList()

                // then
                val animeFoundEvents = receivedEvents.filterIsInstance<RelatedAnimeFoundEvent>()
                assertThat(animeFoundEvents).hasSize(4)

                val foundEntries = animeFoundEvents.map { it.anime.sources.first() }
                assertThat(foundEntries).containsExactlyInAnyOrder(
                    anime1.sources.first(),
                    anime5.sources.first(),
                    anime6.sources.first(),
                    anime7.sources.first(),
                )
            }
        }
    }

    @Nested
    inner class FindRelatedAnimeForIgnoreListTests {

        @Test
        fun `find related anime for entries in ignorelist`() {
            // given
            val testCache = object: AnimeCache by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
                    return when(key) {
                        URI("https://myanimelist.net/anime/31646") -> PresentValue(anime1)
                        URI("https://myanimelist.net/anime/35180") -> PresentValue(anime2)
                        URI("https://myanimelist.net/anime/28789") -> PresentValue(anime3)
                        URI("https://myanimelist.net/anime/34647") -> PresentValue(anime4)
                        URI("https://myanimelist.net/anime/38154") -> PresentValue(anime5)
                        URI("https://myanimelist.net/anime/34611") -> PresentValue(anime6)
                        URI("https://myanimelist.net/anime/38864") -> PresentValue(anime7)
                        else -> shouldNotBeInvoked()
                    }
                }
            }

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(IgnoreListEntry(anime2))
            }

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultRelatedAnimeHandler = DefaultRelatedAnimeHandler(
                cache = testCache,
                state = testState,
                eventBus = testEventBus,
            )

            // when
            defaultRelatedAnimeHandler.findRelatedAnimeForIgnoreList()

            // then
            val animeFoundEvents = receivedEvents.filterIsInstance<RelatedAnimeFoundEvent>()
            assertThat(animeFoundEvents).hasSize(6)
            assertThat(animeFoundEvents.map { it.listType }.distinct()).containsExactly(IGNORE_LIST)

            val foundEntries = animeFoundEvents.map { it.anime.sources.first() }
            assertThat(foundEntries).containsExactlyInAnyOrder(
                anime1.sources.first(),
                anime3.sources.first(),
                anime4.sources.first(),
                anime5.sources.first(),
                anime6.sources.first(),
                anime7.sources.first(),
            )

            val statusEvents = receivedEvents.filterIsInstance<RelatedAnimeStatusEvent>()
            assertThat(statusEvents).hasSize(7)
            assertThat(statusEvents.map { it.listType }.distinct()).containsExactly(IGNORE_LIST)

            assertThat(receivedEvents.last()).isInstanceOf(RelatedAnimeFinishedEvent::class.java)
            assertThat((receivedEvents.last() as RelatedAnimeFinishedEvent).listType).isEqualTo(IGNORE_LIST)
        }

        @Test
        fun `find related anime for entries in ignorelist and exclude entries in watchlist`() {
            // given
            val testCache = object: AnimeCache by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
                    return when(key) {
                        URI("https://myanimelist.net/anime/31646") -> PresentValue(anime1)
                        URI("https://myanimelist.net/anime/35180") -> PresentValue(anime2)
                        URI("https://myanimelist.net/anime/28789") -> PresentValue(anime3)
                        URI("https://myanimelist.net/anime/34647") -> PresentValue(anime4)
                        URI("https://myanimelist.net/anime/38154") -> PresentValue(anime5)
                        URI("https://myanimelist.net/anime/34611") -> PresentValue(anime6)
                        URI("https://myanimelist.net/anime/38864") -> PresentValue(anime7)
                        else -> shouldNotBeInvoked()
                    }
                }
            }

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun watchList(): Set<WatchListEntry> = setOf(WatchListEntry(anime5), WatchListEntry(anime7))
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(IgnoreListEntry(anime2))
            }

            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val defaultRelatedAnimeHandler = DefaultRelatedAnimeHandler(
                cache = testCache,
                state = testState,
                eventBus = testEventBus,
            )

            // when
            defaultRelatedAnimeHandler.findRelatedAnimeForIgnoreList()

            // then
            val animeFoundEvents = receivedEvents.filterIsInstance<RelatedAnimeFoundEvent>()
            assertThat(animeFoundEvents).hasSize(4)

            val foundEntries = animeFoundEvents.map { it.anime.sources.first() }
            assertThat(foundEntries).containsExactlyInAnyOrder(
                anime1.sources.first(),
                anime3.sources.first(),
                anime4.sources.first(),
                anime6.sources.first(),
            )
        }

        @Test
        fun `find related anime for entries in ignorelist and exclude entries in animelist`() {
            tempDirectory {
                // given
                val testLocation1 = tempDir.resolve("test1").createDirectory().toAbsolutePath()
                val testLocation2 = tempDir.resolve("test2").createDirectory().toAbsolutePath()

                val testCache = object: AnimeCache by TestAnimeCache {
                    override fun fetch(key: URI): CacheEntry<Anime> {
                        return when(key) {
                            URI("https://myanimelist.net/anime/31646") -> PresentValue(anime1)
                            URI("https://myanimelist.net/anime/35180") -> PresentValue(anime2)
                            URI("https://myanimelist.net/anime/28789") -> PresentValue(anime3)
                            URI("https://myanimelist.net/anime/34647") -> PresentValue(anime4)
                            URI("https://myanimelist.net/anime/38154") -> PresentValue(anime5)
                            URI("https://myanimelist.net/anime/34611") -> PresentValue(anime6)
                            URI("https://myanimelist.net/anime/38864") -> PresentValue(anime7)
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = listOf(
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/28789"),
                            title = "3-gatsu no Lion meets Bump of Chicken",
                            episodes = 1,
                            type = SPECIAL,
                            location = testLocation1,
                        ),
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/34647"),
                            title = "3-gatsu no Lion Recap",
                            episodes = 1,
                            type = SPECIAL,
                            location = testLocation2,
                        )
                    )
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun ignoreList(): Set<IgnoreListEntry> = setOf(IgnoreListEntry(anime2))
                }

                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val defaultRelatedAnimeHandler = DefaultRelatedAnimeHandler(
                    cache = testCache,
                    state = testState,
                    eventBus = testEventBus,
                )

                // when
                defaultRelatedAnimeHandler.findRelatedAnimeForIgnoreList()

                // then
                val animeFoundEvents = receivedEvents.filterIsInstance<RelatedAnimeFoundEvent>()
                assertThat(animeFoundEvents).hasSize(4)

                val foundEntries = animeFoundEvents.map { it.anime.sources.first() }
                assertThat(foundEntries).containsExactlyInAnyOrder(
                    anime1.sources.first(),
                    anime5.sources.first(),
                    anime6.sources.first(),
                    anime7.sources.first(),
                )
            }
        }
    }

    val anime1 = Anime(
        sources = hashSetOf(
            URI("https://myanimelist.net/anime/31646"),
        ),
        _title = "3-gatsu no Lion",
        type = TV,
        episodes = 22,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2016,
        ),
        relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/28789"),
            URI("https://myanimelist.net/anime/34611"),
            URI("https://myanimelist.net/anime/34647"),
            URI("https://myanimelist.net/anime/35180"),
            URI("https://myanimelist.net/anime/38154"),
        ),
    )

    val anime2 = Anime(
        sources = hashSetOf(
            URI("https://myanimelist.net/anime/35180"),
        ),
        _title = "3-gatsu no Lion 2nd Season",
        type = TV,
        episodes = 22,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2017,
        ),
        relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/31646"),
            URI("https://myanimelist.net/anime/38864"),
        ),
    )

    val anime3 = Anime(
        sources = hashSetOf(
            URI("https://myanimelist.net/anime/28789"),
        ),
        _title = "3-gatsu no Lion meets Bump of Chicken",
        type = SPECIAL,
        episodes = 1,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2014,
        ),
        relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/31646"),
            URI("https://myanimelist.net/anime/34611"),
        ),
    )

    val anime4 = Anime(
        sources = hashSetOf(
            URI("https://myanimelist.net/anime/34647"),
        ),
        _title = "3-gatsu no Lion Recap",
        type = SPECIAL,
        episodes = 1,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2016,
        ),
        relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/31646"),
        ),
    )

    val anime5 = Anime(
        sources = hashSetOf(
            URI("https://myanimelist.net/anime/38154"),
        ),
        _title = "3-gatsu no Lion: Ugoku! Nya Shogi",
        type = SPECIAL,
        episodes = 10,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = WINTER,
            year = 2017,
        ),
        relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/31646"),
        ),
    )

    val anime6 = Anime(
        sources = hashSetOf(
            URI("https://myanimelist.net/anime/34611"),
        ),
        _title = "Answer (2016)",
        type = SPECIAL,
        episodes = 1,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2016,
        ),
        relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/28789"),
            URI("https://myanimelist.net/anime/31646"),
        ),
    )

    val anime7 = Anime(
        sources = hashSetOf(
            URI("https://myanimelist.net/anime/38864"),
        ),
        _title = "Lion meets HachiClo",
        type = SPECIAL,
        episodes = 2,
        status = FINISHED,
        animeSeason = AnimeSeason(
            season = FALL,
            year = 2018,
        ),
        relatedAnime = hashSetOf(
            URI("https://myanimelist.net/anime/35180"),
        ),
    )
}