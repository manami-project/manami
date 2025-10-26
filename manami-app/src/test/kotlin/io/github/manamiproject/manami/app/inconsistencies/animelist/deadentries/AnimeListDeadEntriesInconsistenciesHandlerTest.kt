package io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.io.path.Path

internal class AnimeListDeadEntriesInconsistenciesHandlerTest {

    @Nested
    inner class IsExecutableTests {

        @Test
        fun `is executable if the config explicitly activates the option`() {
            // given
            val inconsistencyHandler = AnimeListDeadEntriesInconsistenciesHandler(
                state = TestState,
                cache = TestAnimeCache,
            )

            val isExecutableConfig = InconsistenciesSearchConfig(
                checkAnimeListDeadEntries = true,
            )

            // when
            val result = inconsistencyHandler.isExecutable(isExecutableConfig)

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `is not executable if the config doesn't explicitly activates the option`() {
            // given
            val inconsistencyHandler = AnimeListDeadEntriesInconsistenciesHandler(
                state = TestState,
                cache = TestAnimeCache,
            )

            val isNotExecutableConfig = InconsistenciesSearchConfig(
                checkAnimeListDeadEntries = false,
            )

            // when
            val result = inconsistencyHandler.isExecutable(isNotExecutableConfig)

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class CalculateWorkloadTests {

        @Test
        fun `workload is computed by the number of anime list entries having a link`() {
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
                        AnimeListEntry(
                            link = NoLink,
                            title = "Ame-iro Cocoa: Rainy Color e Youkoso!",
                            type = TV,
                            episodes = 12,
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1065/111717t.jpg"),
                            location = Path("."),
                        ),
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/37747"),
                            title = "Ame-iro Cocoa: Side G",
                            type = TV,
                            episodes = 12,
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1394/111379t.jpg"),
                            location = Path("."),
                        ),
                    )
                }
            }

            val inconsistencyHandler = AnimeListDeadEntriesInconsistenciesHandler(
                state = testState,
                cache = TestAnimeCache,
            )

            // when
            val result = inconsistencyHandler.calculateWorkload()

            // then
            assertThat(result).isEqualTo(2)
        }
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

                val inconsistencyHandler = AnimeListDeadEntriesInconsistenciesHandler(
                    state = testState,
                    cache = testCache,
                )

                // when
                val result = inconsistencyHandler.execute()

                // then
                assertThat(result.entries).isEmpty()
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
                assertThat(result.entries).isEmpty()
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
                assertThat(result.entries).containsExactly(deadEntry)
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