package io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.InconsistenciesState
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.io.path.Path
import kotlin.test.AfterTest
import kotlin.test.Test

internal class AnimeListDeadEntriesInconsistenciesHandlerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class ExecuteTests {

        @Test
        fun `don't update state if there is nothing to report`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<InconsistenciesState>()
                val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return emptyList()
                    }
                }

                val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> = DeadEntry()
                }

                val inconsistencyHandler = AnimeListDeadEntriesInconsistenciesHandler(
                    state = testState,
                    cache = testCache,
                )

                // when
                val result = inconsistencyHandler.execute()

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(1) // initial
                assertThat(result).isEmpty()
            }
        }

        @Test
        fun `don't include anime if there is an entry returned from the cache`() {
            runBlocking {
                // given
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            AnimeListEntry(
                                link = Link("https://myanimelist.net/anime/5114"),
                                title = "Fullmetal Alchemist: Brotherhood",
                                type = TV,
                                episodes = 64,
                                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                                location = Path("."),
                            ),
                        )
                    }
                }

                val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return PresentValue(
                            Anime(
                                sources = hashSetOf(URI("https://myanimelist.net/anime/5114")),
                                title = "Fullmetal Alchemist: Brotherhood",
                                type = TV,
                                episodes = 64,
                                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                            )
                        )
                    }
                }

                val inconsistencyHandler = AnimeListDeadEntriesInconsistenciesHandler(
                    state = testState,
                    cache = testCache,
                )

                // when
                val result = inconsistencyHandler.execute()

                // then
                assertThat(result).isEmpty()
            }
        }

        @Test
        fun `include anime if nothing is returned from the cache based on the link`() {
            runBlocking {
                // given
                val deadEntry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/10"),
                    title = "Dead Entry",
                    type = TV,
                    episodes = 64,
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
                    location = Path("."),
                )

                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            deadEntry,
                        )
                    }
                }

                val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> = DeadEntry()
                }

                val inconsistencyHandler = AnimeListDeadEntriesInconsistenciesHandler(
                    state = testState,
                    cache = testCache,
                )

                // when
                val result = inconsistencyHandler.execute()

                // then
                assertThat(result).containsExactly(deadEntry)
            }
        }

        @Test
        fun `update state for any findings`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<InconsistenciesState>()
                val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val deadEntry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/10"),
                    title = "Dead Entry",
                    type = TV,
                    episodes = 64,
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
                    location = Path("."),
                )

                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            deadEntry,
                        )
                    }
                }

                val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> = DeadEntry()
                }

                val inconsistencyHandler = AnimeListDeadEntriesInconsistenciesHandler(
                    state = testState,
                    cache = testCache,
                )

                // when
                val result = inconsistencyHandler.execute()

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(2) // initial, update
                assertThat(result).containsExactly(deadEntry)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = AnimeListDeadEntriesInconsistenciesHandler.instance

            // when
            val result = AnimeListDeadEntriesInconsistenciesHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(AnimeListDeadEntriesInconsistenciesHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}