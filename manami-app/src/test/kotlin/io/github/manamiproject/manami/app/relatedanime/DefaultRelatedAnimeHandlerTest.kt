package io.github.manamiproject.manami.app.relatedanime

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.cache.TestAnimeCache
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.FindRelatedAnimeState
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.FALL
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.WINTER
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType.SPECIAL
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import java.net.URI
import kotlin.io.path.createDirectory
import kotlin.test.AfterTest

internal class DefaultRelatedAnimeHandlerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class FindRelatedAnimeTests {

        @Test
        fun `find related anime for entries in animelist`() {
            runBlocking {
                tempDirectory {
                    // given
                    val receivedEvents = mutableListOf<FindRelatedAnimeState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findRelatedAnimeState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testLocation = tempDir.resolve("test1").createDirectory().toAbsolutePath()

                    val testCache = object: AnimeCache by TestAnimeCache {
                        override suspend fun fetch(key: URI): CacheEntry<Anime> {
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

                    val defaultRelatedAnimeHandler = DefaultRelatedAnimeHandler(
                        cache = testCache,
                        state = testState,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    // when
                    defaultRelatedAnimeHandler.findRelatedAnime(listOf(URI("https://myanimelist.net/anime/35180")))

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    val foundEntries = CoroutinesFlowEventBus.findRelatedAnimeState.value.entries.map { anime -> anime.link.uri }
                    assertThat(foundEntries).containsExactlyInAnyOrder(
                        anime1.sources.first(),
                        anime3.sources.first(),
                        anime4.sources.first(),
                        anime5.sources.first(),
                        anime6.sources.first(),
                        anime7.sources.first(),
                    )
                }
            }
        }

        @Test
        fun `find related anime for entries in animelist and exclude entries in watchlist`() {
            runBlocking {
                tempDirectory {
                    // given
                    val receivedEvents = mutableListOf<FindRelatedAnimeState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findRelatedAnimeState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testLocation = tempDir.resolve("test").createDirectory().toAbsolutePath()

                    val testCache = object: AnimeCache by TestAnimeCache {
                        override suspend fun fetch(key: URI): CacheEntry<Anime> {
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

                    val defaultRelatedAnimeHandler = DefaultRelatedAnimeHandler(
                        cache = testCache,
                        state = testState,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    // when
                    defaultRelatedAnimeHandler.findRelatedAnime(listOf(URI("https://myanimelist.net/anime/35180")))

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    val foundEntries = CoroutinesFlowEventBus.findRelatedAnimeState.value.entries.map { anime -> anime.link.uri }
                    assertThat(foundEntries).containsExactlyInAnyOrder(
                        anime1.sources.first(),
                        anime3.sources.first(),
                        anime4.sources.first(),
                        anime6.sources.first(),
                    )
                }
            }
        }

        @Test
        fun `find related anime for entries in animelist and exclude entries in ignorelist`() {
            runBlocking {
                tempDirectory {
                    // given
                    val receivedEvents = mutableListOf<FindRelatedAnimeState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.findRelatedAnimeState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testLocation = tempDir.resolve("test").createDirectory().toAbsolutePath()

                    val testCache = object: AnimeCache by TestAnimeCache {
                        override suspend fun fetch(key: URI): CacheEntry<Anime> {
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

                    val defaultRelatedAnimeHandler = DefaultRelatedAnimeHandler(
                        cache = testCache,
                        state = testState,
                        eventBus = CoroutinesFlowEventBus,
                    )

                    // when
                    defaultRelatedAnimeHandler.findRelatedAnime(listOf(URI("https://myanimelist.net/anime/35180")))

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(3) // initial, start, result
                    val foundEntries = CoroutinesFlowEventBus.findRelatedAnimeState.value.entries.map { anime -> anime.link.uri }
                    assertThat(foundEntries).containsExactlyInAnyOrder(
                        anime1.sources.first(),
                        anime5.sources.first(),
                        anime6.sources.first(),
                        anime7.sources.first(),
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
            val previous = DefaultRelatedAnimeHandler.instance

            // when
            val result = DefaultRelatedAnimeHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultRelatedAnimeHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }

    val anime1 = Anime(
        sources = hashSetOf(
            URI("https://myanimelist.net/anime/31646"),
        ),
        title = "3-gatsu no Lion",
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
        title = "3-gatsu no Lion 2nd Season",
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
        title = "3-gatsu no Lion meets Bump of Chicken",
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
        title = "3-gatsu no Lion Recap",
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
        title = "3-gatsu no Lion: Ugoku! Nya Shogi",
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
        title = "Answer (2016)",
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
        title = "Lion meets HachiClo",
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