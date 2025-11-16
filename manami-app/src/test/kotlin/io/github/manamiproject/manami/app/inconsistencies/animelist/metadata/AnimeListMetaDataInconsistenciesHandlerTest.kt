package io.github.manamiproject.manami.app.inconsistencies.animelist.metadata

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.InconsistenciesState
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.AnimeType.UNKNOWN
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.io.path.Path
import kotlin.test.AfterTest

internal class AnimeListMetaDataInconsistenciesHandlerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class ExecuteTests {

        @Test
        fun `ignore entry if link is empty`() {
            runBlocking {
                // given
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            AnimeListEntry(
                                link = NoLink,
                                title = "No Link Entry",
                                type = TV,
                                episodes = 64,
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
                                location = Path("."),
                            ),
                        )
                    }
                }

                val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> = DeadEntry()
                }

                val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
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
        fun `don't include anime if anime list entry and entry from cache are equal`() {
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
                                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
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
                                picture = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                            )
                        )
                    }
                }

                val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
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
        fun `don't update state if there is nothing to report`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<InconsistenciesState>()
                val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            AnimeListEntry(
                                link = Link("https://myanimelist.net/anime/5114"),
                                title = "Fullmetal Alchemist: Brotherhood",
                                type = TV,
                                episodes = 64,
                                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
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
                                picture = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                            )
                        )
                    }
                }

                val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
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
        fun `include anime if title differs`() {
            runBlocking {
                // given
                val testAnimeListEntry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist Brotherhood (TV)",
                    type = TV,
                    episodes = 64,
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                    location = Path("."),
                )
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            testAnimeListEntry,
                        )
                    }
                }

                val testAnime = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/5114")),
                    title = "Fullmetal Alchemist: Brotherhood",
                    type = TV,
                    episodes = 64,
                    picture = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                )
                val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return PresentValue(
                            testAnime
                        )
                    }
                }

                val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
                    state = testState,
                    cache = testCache,
                )

                // when
                val result = inconsistencyHandler.execute()

                // then
                assertThat(result).isNotEmpty()

                val entry = result.first()
                assertThat(entry.currentEntry).isEqualTo(testAnimeListEntry)
                assertThat(entry.replacementEntry).isEqualTo(testAnimeListEntry.copy(title = testAnime.title))
            }
        }

        @Test
        fun `include anime if episodes differs`() {
            runBlocking {
                // given
                val testAnimeListEntry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    type = TV,
                    episodes = 65,
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                    location = Path("."),
                )
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            testAnimeListEntry,
                        )
                    }
                }

                val testAnime = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/5114")),
                    title = "Fullmetal Alchemist: Brotherhood",
                    type = TV,
                    episodes = 64,
                    picture = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                )
                val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return PresentValue(
                            testAnime
                        )
                    }
                }

                val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
                    state = testState,
                    cache = testCache,
                )

                // when
                val result = inconsistencyHandler.execute()

                // then
                assertThat(result).isNotEmpty()

                val entry = result.first()
                assertThat(entry.currentEntry).isEqualTo(testAnimeListEntry)
                assertThat(entry.replacementEntry).isEqualTo(testAnimeListEntry.copy(episodes = testAnime.episodes))
            }
        }

        @Test
        fun `include anime if type differs`() {
            runBlocking {
                // given
                val testAnimeListEntry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    type = UNKNOWN,
                    episodes = 64,
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                    location = Path("."),
                )
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            testAnimeListEntry,
                        )
                    }
                }

                val testAnime = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/5114")),
                    title = "Fullmetal Alchemist: Brotherhood",
                    type = TV,
                    episodes = 64,
                    picture = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                )
                val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return PresentValue(
                            testAnime
                        )
                    }
                }

                val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
                    state = testState,
                    cache = testCache,
                )

                // when
                val result = inconsistencyHandler.execute()

                // then
                assertThat(result).isNotEmpty()

                val entry = result.first()
                assertThat(entry.currentEntry).isEqualTo(testAnimeListEntry)
                assertThat(entry.replacementEntry).isEqualTo(testAnimeListEntry.copy(type = testAnime.type))
            }
        }

        @Test
        fun `include anime if thumbnail differs`() {
            runBlocking {
                // given
                val testAnimeListEntry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    type = TV,
                    episodes = 64,
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/000/1111.jpg"),
                    location = Path("."),
                )
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            testAnimeListEntry,
                        )
                    }
                }

                val testAnime = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/5114")),
                    title = "Fullmetal Alchemist: Brotherhood",
                    type = TV,
                    episodes = 64,
                    picture = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                )
                val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return PresentValue(
                            testAnime
                        )
                    }
                }

                val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
                    state = testState,
                    cache = testCache,
                )

                // when
                val result = inconsistencyHandler.execute()

                // then
                assertThat(result).isNotEmpty()

                val entry = result.first()
                assertThat(entry.currentEntry).isEqualTo(testAnimeListEntry)
                assertThat(entry.replacementEntry).isEqualTo(testAnimeListEntry.copy(thumbnail = testAnime.picture))
            }
        }

        @Test
        fun `update state for any findings`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<InconsistenciesState>()
                val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testAnimeListEntry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist Brotherhood (TV)",
                    type = TV,
                    episodes = 64,
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                    location = Path("."),
                )
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            testAnimeListEntry,
                        )
                    }
                }

                val testAnime = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/5114")),
                    title = "Fullmetal Alchemist: Brotherhood",
                    type = TV,
                    episodes = 64,
                    picture = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
                )
                val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return PresentValue(
                            testAnime
                        )
                    }
                }

                val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
                    state = testState,
                    cache = testCache,
                )

                // when
                val result = inconsistencyHandler.execute()

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(2) // initial, update

                assertThat(result).isNotEmpty()

                val entry = result.first()
                assertThat(entry.currentEntry).isEqualTo(testAnimeListEntry)
                assertThat(entry.replacementEntry).isEqualTo(testAnimeListEntry.copy(title = testAnime.title))
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = AnimeListMetaDataInconsistenciesHandler.instance

            // when
            val result = AnimeListMetaDataInconsistenciesHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(AnimeListMetaDataInconsistenciesHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}