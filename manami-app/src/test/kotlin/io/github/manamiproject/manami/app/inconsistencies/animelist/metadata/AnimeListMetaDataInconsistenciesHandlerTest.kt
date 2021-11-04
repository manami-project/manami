package io.github.manamiproject.manami.app.inconsistencies.animelist.metadata

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.collections.SortedList
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.Anime.Type.UNKNOWN
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI

internal class AnimeListMetaDataInconsistenciesHandlerTest {

    @Nested
    inner class IsExecutableTests {

        @Test
        fun `is executable if the config explicitly activates the option`() {
            // given
            val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
                state = TestState,
                cache = TestAnimeCache,
            )

            val isExecutableConfig = InconsistenciesSearchConfig(
                checkAnimeListMetaData = true
            )

            // when
            val result = inconsistencyHandler.isExecutable(isExecutableConfig)

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `is not executable if the config doesn't explicitly activates the option`() {
            // given
            val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
                state = TestState,
                cache = TestAnimeCache,
            )

            val isNotExecutableConfig = InconsistenciesSearchConfig(
                checkAnimeListMetaData = false
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
                            location = URI(".")
                        ),
                        AnimeListEntry(
                            link = NoLink,
                            title = "Ame-iro Cocoa: Rainy Color e Youkoso!",
                            type = TV,
                            episodes = 12,
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1065/111717t.jpg"),
                            location = URI("."),
                        ),
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/37747"),
                            title = "Ame-iro Cocoa: Side G",
                            type = TV,
                            episodes = 12,
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1394/111379t.jpg"),
                            location = URI("."),
                        )
                    )
                }
            }

            val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
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
            // given
            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> {
                    return listOf(
                        AnimeListEntry(
                            link = NoLink,
                            title = "No Link Entry",
                            type = TV,
                            episodes = 64,
                            thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                            location = URI(".")
                        ),
                    )
                }
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = Empty()
            }

            val inconsistencyHandler = AnimeListMetaDataInconsistenciesHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.entries).isEmpty()
        }

        @Test
        fun `don't include anime if anime list entry and entry from cache are equal`() {
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
                            location = URI(".")
                        ),
                    )
                }
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
                    return PresentValue(
                        Anime(
                            sources = SortedList(URI("https://myanimelist.net/anime/5114")),
                            _title = "Fullmetal Alchemist: Brotherhood",
                            type = TV,
                            episodes = 64,
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
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
            assertThat(result.entries).isEmpty()
        }

        @Test
        fun `include anime if title differs`() {
            // given
            val testAnimeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist Brotherhood (TV)",
                type = TV,
                episodes = 64,
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                location = URI(".")
            )
            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> {
                    return listOf(
                        testAnimeListEntry,
                    )
                }
            }

            val testAnime = Anime(
                sources = SortedList(URI("https://myanimelist.net/anime/5114")),
                _title = "Fullmetal Alchemist: Brotherhood",
                type = TV,
                episodes = 64,
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
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
            assertThat(result.entries).isNotEmpty()

            val entry = result.entries.first()
            assertThat(entry.currentEntry).isEqualTo(testAnimeListEntry)
            assertThat(entry.replacementEntry).isEqualTo(testAnimeListEntry.copy(title = testAnime.title))
        }

        @Test
        fun `include anime if episodes differs`() {
            // given
            val testAnimeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                type = TV,
                episodes = 65,
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                location = URI(".")
            )
            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> {
                    return listOf(
                        testAnimeListEntry,
                    )
                }
            }

            val testAnime = Anime(
                sources = SortedList(URI("https://myanimelist.net/anime/5114")),
                _title = "Fullmetal Alchemist: Brotherhood",
                type = TV,
                episodes = 64,
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
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
            assertThat(result.entries).isNotEmpty()

            val entry = result.entries.first()
            assertThat(entry.currentEntry).isEqualTo(testAnimeListEntry)
            assertThat(entry.replacementEntry).isEqualTo(testAnimeListEntry.copy(episodes = testAnime.episodes))
        }

        @Test
        fun `include anime if type differs`() {
            // given
            val testAnimeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                type = UNKNOWN,
                episodes = 64,
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                location = URI(".")
            )
            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> {
                    return listOf(
                        testAnimeListEntry,
                    )
                }
            }

            val testAnime = Anime(
                sources = SortedList(URI("https://myanimelist.net/anime/5114")),
                _title = "Fullmetal Alchemist: Brotherhood",
                type = TV,
                episodes = 64,
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
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
            assertThat(result.entries).isNotEmpty()

            val entry = result.entries.first()
            assertThat(entry.currentEntry).isEqualTo(testAnimeListEntry)
            assertThat(entry.replacementEntry).isEqualTo(testAnimeListEntry.copy(type = testAnime.type))
        }

        @Test
        fun `include anime if thumbnail differs`() {
            // given
            val testAnimeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                type = TV,
                episodes = 64,
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/000/1111t.jpg"),
                location = URI(".")
            )
            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> {
                    return listOf(
                        testAnimeListEntry,
                    )
                }
            }

            val testAnime = Anime(
                sources = SortedList(URI("https://myanimelist.net/anime/5114")),
                _title = "Fullmetal Alchemist: Brotherhood",
                type = TV,
                episodes = 64,
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
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
            assertThat(result.entries).isNotEmpty()

            val entry = result.entries.first()
            assertThat(entry.currentEntry).isEqualTo(testAnimeListEntry)
            assertThat(entry.replacementEntry).isEqualTo(testAnimeListEntry.copy(thumbnail = testAnime.thumbnail))
        }
    }
}