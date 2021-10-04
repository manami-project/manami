package io.github.manamiproject.manami.app.inconsistencies.metadata

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.cache.TestAnimeCache
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.app.inconsistencies.deadentries.DeadEntriesInconsistencyHandler
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.collections.SortedList
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.FALL
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI

internal class MetaDataInconsistencyHandlerTest {

    @Test
    fun `is executable if the config explicitly activates the option`() {
        // given
        val inconsistencyHandler = MetaDataInconsistencyHandler(
            state = TestState,
            cache = TestAnimeCache,
        )

        val isExecutableConfig = InconsistenciesSearchConfig(
            checkMetaData = true
        )
        val isNotExecutableConfig = InconsistenciesSearchConfig(
            checkMetaData = false
        )

        // when
        val resultTrue = inconsistencyHandler.isExecutable(isExecutableConfig)
        val resultFalse = inconsistencyHandler.isExecutable(isNotExecutableConfig)

        // then
        assertThat(resultTrue).isTrue()
        assertThat(resultFalse).isFalse()
    }

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
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1065/111717t.jpg")
                ),
                IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/37747"),
                    title = "Ame-iro Cocoa: Side G",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1394/111379t.jpg")
                )
            )
        }

        val inconsistencyHandler = MetaDataInconsistencyHandler(
            state = testState,
        )

        // when
        val result = inconsistencyHandler.calculateWorkload()

        // then
        assertThat(result).isEqualTo(3)
    }

    @Nested
    inner class WatchListTests {

        @Test
        fun `entries without cache entry cannot appear in result`() {
            // given
            val testState = object: State by TestState {
                override fun watchList(): Set<WatchListEntry> = setOf(
                    WatchListEntry(
                        link = Link("https://myanimelist.net/anime/10001"),
                        title = "Dead entry",
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                    )
                )
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = Empty()
            }

            val inconsistencyHandler = MetaDataInconsistencyHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.watchListResults).isEmpty()
            assertThat(result.ignoreListResults).isEmpty()
        }

        @Test
        fun `do not include entry if current entry and entry from cache have different links`() {
            // given
            val testState = object: State by TestState {
                override fun watchList(): Set<WatchListEntry> = setOf(
                    WatchListEntry(
                        link = Link("https://myanimelist.net/anime/10001"),
                        title = "Dead entry",
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                    )
                )
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        sources = SortedList(
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
                        relatedAnime = SortedList(
                            URI("https://myanimelist.net/anime/28789"),
                            URI("https://myanimelist.net/anime/34611"),
                            URI("https://myanimelist.net/anime/34647"),
                            URI("https://myanimelist.net/anime/35180"),
                            URI("https://myanimelist.net/anime/38154"),
                        ),
                    )
                )
            }

            val inconsistencyHandler = MetaDataInconsistencyHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.watchListResults).isEmpty()
            assertThat(result.ignoreListResults).isEmpty()
        }

        @Test
        fun `do not include entry if current entry and entry from cache do not differ`() {
            // given
            val testState = object: State by TestState {
                override fun watchList(): Set<WatchListEntry> = setOf(
                    WatchListEntry(
                        link = Link("https://myanimelist.net/anime/31646"),
                        title = "3-gatsu no Lion",
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                    )
                )
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        sources = SortedList(
                            URI("https://myanimelist.net/anime/31646"),
                        ),
                        _title = "3-gatsu no Lion",
                        type = TV,
                        episodes = 22,
                        status = FINISHED,
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2016,
                        ),
                        relatedAnime = SortedList(
                            URI("https://myanimelist.net/anime/28789"),
                            URI("https://myanimelist.net/anime/34611"),
                            URI("https://myanimelist.net/anime/34647"),
                            URI("https://myanimelist.net/anime/35180"),
                            URI("https://myanimelist.net/anime/38154"),
                        ),
                    )
                )
            }

            val inconsistencyHandler = MetaDataInconsistencyHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.watchListResults).isEmpty()
            assertThat(result.ignoreListResults).isEmpty()
        }

        @Test
        fun `include entry if title differ`() {
            // given
            val testState = object: State by TestState {
                override fun watchList(): Set<WatchListEntry> = setOf(
                    WatchListEntry(
                        link = Link("https://myanimelist.net/anime/31646"),
                        title = "sangatsu no Lion",
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                    )
                )
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        sources = SortedList(
                            URI("https://myanimelist.net/anime/31646"),
                        ),
                        _title = "3-gatsu no Lion",
                        type = TV,
                        episodes = 22,
                        status = FINISHED,
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2016,
                        ),
                        relatedAnime = SortedList(
                            URI("https://myanimelist.net/anime/28789"),
                            URI("https://myanimelist.net/anime/34611"),
                            URI("https://myanimelist.net/anime/34647"),
                            URI("https://myanimelist.net/anime/35180"),
                            URI("https://myanimelist.net/anime/38154"),
                        ),
                    )
                )
            }

            val inconsistencyHandler = MetaDataInconsistencyHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.watchListResults.size).isOne()
            val entry = result.watchListResults.first()
            assertThat(entry.currentEntry).isEqualTo(
                WatchListEntry(
                    link = Link("https://myanimelist.net/anime/31646"),
                    title = "sangatsu no Lion",
                    thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                )
            )
            assertThat(entry.newEntry).isEqualTo(
                WatchListEntry(
                    link = Link("https://myanimelist.net/anime/31646"),
                    title = "3-gatsu no Lion",
                    thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                )
            )
            assertThat(result.ignoreListResults).isEmpty()
        }

        @Test
        fun `include entry if thumbnails differ`() {
            // given
            val testState = object: State by TestState {
                override fun watchList(): Set<WatchListEntry> = setOf(
                    WatchListEntry(
                        link = Link("https://myanimelist.net/anime/31646"),
                        title = "3-gatsu no Lion",
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                    )
                )
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        sources = SortedList(
                            URI("https://myanimelist.net/anime/31646"),
                        ),
                        _title = "3-gatsu no Lion",
                        type = TV,
                        episodes = 22,
                        status = FINISHED,
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/6/82898t.jpg"),
                        animeSeason = AnimeSeason(
                            season = FALL,
                            year = 2016,
                        ),
                        relatedAnime = SortedList(
                            URI("https://myanimelist.net/anime/28789"),
                            URI("https://myanimelist.net/anime/34611"),
                            URI("https://myanimelist.net/anime/34647"),
                            URI("https://myanimelist.net/anime/35180"),
                            URI("https://myanimelist.net/anime/38154"),
                        ),
                    )
                )
            }

            val inconsistencyHandler = MetaDataInconsistencyHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.watchListResults.size).isOne()
            val entry = result.watchListResults.first()
            assertThat(entry.currentEntry).isEqualTo(
                WatchListEntry(
                    link = Link("https://myanimelist.net/anime/31646"),
                    title = "3-gatsu no Lion",
                    thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                )
            )
            assertThat(entry.newEntry).isEqualTo(
                WatchListEntry(
                    link = Link("https://myanimelist.net/anime/31646"),
                    title = "3-gatsu no Lion",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/6/82898t.jpg"),
                )
            )
            assertThat(result.ignoreListResults).isEmpty()
        }
    }

    @Nested
    inner class IgnoreListTests {

        @Test
        fun `entries without cache entry cannot appear in result`() {
            // given
            val testState = object: State by TestState {
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/28981"),
                        title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                    )
                )
                override fun watchList(): Set<WatchListEntry> = emptySet()
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = Empty()
            }

            val inconsistencyHandler = MetaDataInconsistencyHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.watchListResults).isEmpty()
            assertThat(result.ignoreListResults).isEmpty()
        }

        @Test
        fun `do not include entry if current entry and entry from cache have different links`() {
            // given
            val testState = object: State by TestState {
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/10001"),
                        title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                    )
                )
                override fun watchList(): Set<WatchListEntry> = emptySet()
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        sources = SortedList(
                            URI("https://myanimelist.net/anime/28981"),
                        ),
                        _title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                    )
                )
            }

            val inconsistencyHandler = MetaDataInconsistencyHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.watchListResults).isEmpty()
            assertThat(result.ignoreListResults).isEmpty()
        }

        @Test
        fun `do not include entry if current entry and entry from cache do not differ`() {
            // given
            val testState = object: State by TestState {
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/28981"),
                        title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                    )
                )
                override fun watchList(): Set<WatchListEntry> = emptySet()
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        sources = SortedList(
                            URI("https://myanimelist.net/anime/28981"),
                        ),
                        _title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                    )
                )
            }

            val inconsistencyHandler = MetaDataInconsistencyHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.watchListResults).isEmpty()
            assertThat(result.ignoreListResults).isEmpty()
        }

        @Test
        fun `include entry if title differ`() {
            // given
            val testState = object: State by TestState {
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/28981"),
                        title = "Ameiro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                    )
                )
                override fun watchList(): Set<WatchListEntry> = emptySet()
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        sources = SortedList(
                            URI("https://myanimelist.net/anime/28981"),
                        ),
                        _title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                    )
                )
            }

            val inconsistencyHandler = MetaDataInconsistencyHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.watchListResults).isEmpty()

            assertThat(result.ignoreListResults.size).isOne()
            val entry = result.ignoreListResults.first()
            assertThat(entry.currentEntry).isEqualTo(
                IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/28981"),
                    title = "Ameiro Cocoa",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                )
            )
            assertThat(entry.newEntry).isEqualTo(
                IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/28981"),
                    title = "Ame-iro Cocoa",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                )
            )
        }

        @Test
        fun `include entry if thumbnails differ`() {
            // given
            val testState = object: State by TestState {
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/28981"),
                        title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif")
                    )
                )
                override fun watchList(): Set<WatchListEntry> = emptySet()
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        sources = SortedList(
                            URI("https://myanimelist.net/anime/28981"),
                        ),
                        _title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                    )
                )
            }

            val inconsistencyHandler = MetaDataInconsistencyHandler(
                state = testState,
                cache = testCache,
            )

            // when
            val result = inconsistencyHandler.execute()

            // then
            assertThat(result.watchListResults).isEmpty()

            assertThat(result.ignoreListResults.size).isOne()
            val entry = result.ignoreListResults.first()
            assertThat(entry.currentEntry).isEqualTo(
                IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/28981"),
                    title = "Ame-iro Cocoa",
                    thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif")
                )
            )
            assertThat(entry.newEntry).isEqualTo(
                IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/28981"),
                    title = "Ame-iro Cocoa",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                )
            )
        }
    }
}