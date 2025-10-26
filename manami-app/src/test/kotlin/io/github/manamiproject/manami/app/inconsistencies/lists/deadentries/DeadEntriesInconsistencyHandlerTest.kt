package io.github.manamiproject.manami.app.inconsistencies.lists.deadentries

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.FALL
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI

internal class DeadEntriesInconsistencyHandlerTest {

    @Nested
    inner class IsExecutableTests {

        @Test
        fun `is executable if the config explicitly activates the option`() {
            // given
            val inconsistencyHandler = DeadEntriesInconsistencyHandler(
                state = TestState,
                cache = TestAnimeCache,
            )

            val isExecutableConfig = InconsistenciesSearchConfig(
                checkDeadEntries = true,
            )

            // when
            val result = inconsistencyHandler.isExecutable(isExecutableConfig)

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `is not executable if the config doesn't explicitly activates the option`() {
            // given
            val inconsistencyHandler = DeadEntriesInconsistencyHandler(
                state = TestState,
                cache = TestAnimeCache,
            )

            val isNotExecutableConfig = InconsistenciesSearchConfig(
                checkDeadEntries = false,
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
        fun `workload is computed by size of watch- and ignoreList`() {
            // given
            val testState = object: State by TestState {
                override fun watchList(): Set<WatchListEntry> = setOf(
                    WatchListEntry(
                        link = Link("https://myanimelist.net/anime/5114"),
                        title = "Fullmetal Alchemist: Brotherhood",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                    )
                )
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/31139"),
                        title = "Ame-iro Cocoa: Rainy Color e Youkoso!",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1065/111717t.jpg"),
                    ),
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/37747"),
                        title = "Ame-iro Cocoa: Side G",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1394/111379t.jpg"),
                    )
                )
            }

            val inconsistencyHandler = DeadEntriesInconsistencyHandler(
                state = testState,
            )

            // when
            val result = inconsistencyHandler.calculateWorkload()

            // then
            assertThat(result).isEqualTo(3)
        }
    }

    @Nested
    inner class ExecuteTests {

        @Nested
        inner class WatchListTests {

            @Test
            fun `entries without cache entry appear in result`() {
                runBlocking {
                    // given
                    val testState = object: State by TestState {
                        override fun watchList(): Set<WatchListEntry> = setOf(
                            WatchListEntry(
                                link = Link("https://myanimelist.net/anime/10001"),
                                title = "Dead entry",
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
                            )
                        )
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                        override suspend fun fetch(key: URI): CacheEntry<Anime> = DeadEntry()
                    }

                    val inconsistencyHandler = DeadEntriesInconsistencyHandler(
                        state = testState,
                        cache = testCache,
                    )

                    // when
                    val result = inconsistencyHandler.execute()

                    // then
                    assertThat(result.watchListResults).hasSize(1)
                    assertThat(result.watchListResults.first()).isEqualTo(
                        WatchListEntry(
                            link = Link("https://myanimelist.net/anime/10001"),
                            title = "Dead entry",
                            thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
                        )
                    )
                    assertThat(result.ignoreListResults).isEmpty()
                }
            }

            @Test
            fun `do not include entry if cache returns an entry`() {
                runBlocking {
                    // given
                    val testState = object: State by TestState {
                        override fun watchList(): Set<WatchListEntry> = setOf(
                            WatchListEntry(
                                link = Link("https://myanimelist.net/anime/31646"),
                                title = "3-gatsu no Lion",
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
                            )
                        )
                        override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    }

                    val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                        override suspend fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                            Anime(
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
                        )
                    }

                    val inconsistencyHandler = DeadEntriesInconsistencyHandler(
                        state = testState,
                        cache = testCache,
                    )

                    // when
                    val result = inconsistencyHandler.execute()

                    // then
                    assertThat(result.watchListResults).isEmpty()
                    assertThat(result.ignoreListResults).isEmpty()
                }
            }
        }

        @Nested
        inner class IgnoreListTests {

            @Test
            fun `entries without cache entry appear in result`() {
                runBlocking {
                    // given
                    val testState = object: State by TestState {
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                            IgnoreListEntry(
                                link = Link("https://myanimelist.net/anime/28981"),
                                title = "Ame-iro Cocoa",
                                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                            )
                        )
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                    }

                    val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                        override suspend fun fetch(key: URI): CacheEntry<Anime> = DeadEntry()
                    }

                    val inconsistencyHandler = DeadEntriesInconsistencyHandler(
                        state = testState,
                        cache = testCache,
                    )

                    // when
                    val result = inconsistencyHandler.execute()

                    // then
                    assertThat(result.watchListResults).isEmpty()
                    assertThat(result.ignoreListResults).hasSize(1)
                    assertThat(result.ignoreListResults.first()).isEqualTo(
                        IgnoreListEntry(
                            link = Link("https://myanimelist.net/anime/28981"),
                            title = "Ame-iro Cocoa",
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                        )
                    )
                }
            }

            @Test
            fun `do not include entry if cache returns an entry`() {
                runBlocking {
                    // given
                    val testState = object: State by TestState {
                        override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                            IgnoreListEntry(
                                link = Link("https://myanimelist.net/anime/28981"),
                                title = "Ame-iro Cocoa",
                                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                            )
                        )
                        override fun watchList(): Set<WatchListEntry> = emptySet()
                    }

                    val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                        override suspend fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                            Anime(
                                sources = hashSetOf(
                                    URI("https://myanimelist.net/anime/28981"),
                                ),
                                title = "Ame-iro Cocoa",
                                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                            )
                        )
                    }

                    val inconsistencyHandler = DeadEntriesInconsistencyHandler(
                        state = testState,
                        cache = testCache,
                    )

                    // when
                    val result = inconsistencyHandler.execute()

                    // then
                    assertThat(result.watchListResults).isEmpty()
                    assertThat(result.ignoreListResults).isEmpty()
                }
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DeadEntriesInconsistencyHandler.instance

            // when
            val result = DeadEntriesInconsistencyHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(DeadEntriesInconsistencyHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}