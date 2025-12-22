package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.commands.ReversibleCommand
import io.github.manamiproject.manami.app.commands.TestCommandHistory
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.events.*
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.*
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.SPRING
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType.SPECIAL
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.io.path.Path
import kotlin.test.AfterTest
import kotlin.test.Test

internal class DefaultListHandlerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class AnimeListTests {

        @Test
        fun `return the list of anime list entries from state`() {
            // given
            val entry1 = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/3299"),
                title = "H2O: Footprints in the Sand",
                episodes = 4,
                type = SPECIAL,
                location = Path("some/relative/path/h2o_-_footprints_in_the_sand_special"),
            )
            val entry2 = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = Path("some/relative/path/beck"),
            )

            val state = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = listOf(entry1, entry2)
            }

            val testCache = object: AnimeCache by TestAnimeCache { }
            val testCommandHistory = object: CommandHistory by TestCommandHistory { }

            val defaultListHandler = DefaultListHandler(
                state = state,
                commandHistory = testCommandHistory,
                cache = testCache,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = defaultListHandler.animeList()

            // then
            assertThat(result).containsExactlyInAnyOrder(entry1, entry2)
        }
    }
    
    @Nested
    inner class WatchListTests {

        @Test
        fun `return the list of watch list entries from state`() {
            // given
            val entry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
            )
            val entry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
            )
            val state = object: State by TestState {
                override fun watchList(): Set<WatchListEntry> = setOf(entry1, entry2)
            }

            val testCache = object: AnimeCache by TestAnimeCache { }
            val testCommandHistory = object: CommandHistory by TestCommandHistory { }

            val defaultListHandler = DefaultListHandler(
                state = state,
                commandHistory = testCommandHistory,
                cache = testCache,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = defaultListHandler.watchList()

            // then
            assertThat(result).containsExactlyInAnyOrder(entry1, entry2)
        }
    }

    @Nested
    inner class IgnoreListTests {

        @Test
        fun `return the list of ignore list entries from state`() {
            // given
            val entry1 = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
            )
            val entry2 = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
            )
            val state = object: State by TestState {
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(entry1, entry2)
            }
            val testCache = object: AnimeCache by TestAnimeCache { }
            val testCommandHistory = object: CommandHistory by TestCommandHistory { }

            val defaultListHandler = DefaultListHandler(
                state = state,
                commandHistory = testCommandHistory,
                cache = testCache,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = defaultListHandler.ignoreList()

            // then
            assertThat(result).containsExactlyInAnyOrder(entry1, entry2)
        }
    }

    @Nested
    inner class AddAnimeListEntryTests {

        @Test
        fun `add anime list entries and fire command containing the progress`() {
            runBlocking {
                // given
                val receivedAnimeListStateEvents = mutableListOf<AnimeListState>()
                val animeListStateEventsEventCollector = launch { CoroutinesFlowEventBus.animeListState.collect { event -> receivedAnimeListStateEvents.add(event) } }

                val receivedAnimeListModificationStateEvents = mutableListOf<AnimeListModificationState>()
                val animeListModificationStateEventCollector = launch { CoroutinesFlowEventBus.animeListModificationState.collect { event -> receivedAnimeListModificationStateEvents.add(event) } }
                delay(100)

                val entry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/57"),
                    title = "Beck",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                    episodes = 26,
                    type = TV,
                    location = Path("some/relative/path/beck"),
                )

                val savedEntries = mutableListOf<AnimeListEntry>()
                val state = object: State by TestState {
                    override fun openedFile(): OpenedFile = NoFile
                    override fun animeListEntryExists(anime: AnimeListEntry): Boolean = false
                    override fun createSnapshot(): Snapshot = TestSnapshot
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                        savedEntries.addAll(anime)
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val defaultListHandler = DefaultListHandler(
                    state = state,
                    commandHistory = testCommandHistory,
                    cache = TestAnimeCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.addAnimeListEntry(entry)

                // then
                delay(100)
                animeListStateEventsEventCollector.cancelAndJoin()
                animeListModificationStateEventCollector.cancelAndJoin()

                assertThat(receivedAnimeListStateEvents).hasSize(1) // initial (InternallyState which is mocked, would normally fire the update)
                assertThat(savedEntries).containsExactly(entry)

                assertThat(receivedAnimeListModificationStateEvents).hasSize(1) // initial
                assertThat(receivedAnimeListModificationStateEvents.last().addAnimeEntryData).isNull()
            }
        }

        @Test
        fun `removes the entry from any search states upon adding it to animeList`() {
            runBlocking {
                // given
                val testSearchListEntry = SearchResultAnimeEntry(
                    link = Link("https://myanimelist.net/anime/57"),
                    title = "Beck",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                )

                CoroutinesFlowEventBus.findRelatedAnimeState.update { FindRelatedAnimeState(entries = listOf(testSearchListEntry)) }
                val findRelatedAnimeStateEvents = mutableListOf<FindRelatedAnimeState>()
                val findRelatedAnimeStateEventCollector = launch { CoroutinesFlowEventBus.findRelatedAnimeState.collect { event -> findRelatedAnimeStateEvents.add(event) } }

                CoroutinesFlowEventBus.findByTitleState.update { FindByTitleState(unlistedResults = listOf(testSearchListEntry)) }
                val findByTitleStateEvents = mutableListOf<FindByTitleState>()
                val findByTitleStateEventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> findByTitleStateEvents.add(event) } }

                CoroutinesFlowEventBus.findSeasonState.update { FindSeasonState(entries = listOf(testSearchListEntry)) }
                val findSeasonStateEvents = mutableListOf<FindSeasonState>()
                val findSeasonStateEventCollector = launch { CoroutinesFlowEventBus.findSeasonState.collect { event -> findSeasonStateEvents.add(event) } }

                CoroutinesFlowEventBus.findByCriertiaState.update { FindByCriteriaState(entries = listOf(testSearchListEntry)) }
                val findByCriteriaStateEvents = mutableListOf<FindByCriteriaState>()
                val findByCriteriaStateEventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> findByCriteriaStateEvents.add(event) } }

                CoroutinesFlowEventBus.findSimilarAnimeState.update { FindSimilarAnimeState(entries = listOf(testSearchListEntry)) }
                val findSimilarAnimeStateEvents = mutableListOf<FindSimilarAnimeState>()
                val findSimilarAnimeStateEventCollector = launch { CoroutinesFlowEventBus.findSimilarAnimeState.collect { event -> findSimilarAnimeStateEvents.add(event) } }
                delay(100)

                val entry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/57"),
                    title = "Beck",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                    episodes = 26,
                    type = TV,
                    location = Path("some/relative/path/beck"),
                )

                val state = object: State by TestState {
                    override fun openedFile(): OpenedFile = NoFile
                    override fun animeListEntryExists(anime: AnimeListEntry): Boolean = false
                    override fun createSnapshot(): Snapshot = TestSnapshot
                    override fun animeList(): List<AnimeListEntry> = emptyList()
                    override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {}
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val defaultListHandler = DefaultListHandler(
                    state = state,
                    commandHistory = testCommandHistory,
                    cache = TestAnimeCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.addAnimeListEntry(entry)

                // then
                delay(100)
                findRelatedAnimeStateEventCollector.cancelAndJoin()
                findByTitleStateEventCollector.cancelAndJoin()
                findSeasonStateEventCollector.cancelAndJoin()
                findByCriteriaStateEventCollector.cancelAndJoin()
                findSimilarAnimeStateEventCollector.cancelAndJoin()

                assertThat(findRelatedAnimeStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findByTitleStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findSeasonStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findByCriteriaStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findSimilarAnimeStateEvents).hasSize(2) // initial (including entry), update
            }
        }
    }

    @Nested
    inner class FindAnimeDetailsForAddingAnEntryTests {

        @Test
        fun `successfully find anime`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<AnimeListModificationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.animeListModificationState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val entry = Anime(
                    sources = hashSetOf(
                        URI("https://anidb.net/anime/15738"),
                        URI("https://anilist.co/anime/124194"),
                        URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                        URI("https://kitsu.app/anime/43578"),
                        URI("https://myanimelist.net/anime/42938"),
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

                val defaultSearchHandler = DefaultListHandler(
                    state = TestState,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                )

                // when
                defaultSearchHandler.findAnimeDetailsForAddingAnEntry(URI("https://myanimelist.net/anime/42938"))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().addAnimeEntryData).isNotNull()
                assertThat(receivedEvents.last().addAnimeEntryData!!.title).isEqualTo("Fruits Basket: The Final")
                assertThat(receivedEvents.last().addAnimeEntryData!!.title).isEqualTo("Fruits Basket: The Final")
            }
        }

        @Test
        fun `don't return anime, because URI relates to a dead entry`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<AnimeListModificationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.animeListModificationState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                    populate(URI("https://myanimelist.net/anime/10001"), DeadEntry())
                }

                val defaultSearchHandler = DefaultListHandler(
                    state = TestState,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                )

                // when
                defaultSearchHandler.findAnimeDetailsForAddingAnEntry(URI("https://myanimelist.net/anime/10001"))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last().addAnimeEntryData).isNull()
            }
        }
    }

    @Nested
    inner class FindAnimeListEntryForEditingAnEntryTests {

        @Test
        fun `correctly updates state` () {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<AnimeListModificationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.animeListModificationState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val animeEntry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/3299"),
                    title = "H2O: Footprints in the Sand",
                    episodes = 4,
                    type = SPECIAL,
                    location = Path("some/relative/path/h2o_-_footprints_in_the_sand_special"),
                )

                val defaultSearchHandler = DefaultListHandler(
                    state = TestState,
                    cache = TestAnimeCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                )

                // when
                defaultSearchHandler.prepareAnimeListEntryForEditingAnEntry(animeEntry)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(2) // initial, setting entry
                assertThat(receivedEvents.last().editAnimeListEntryData).isEqualTo(animeEntry)
            }
        }
    }

    @Nested
    inner class AddWatchListEntryTests {

        @Test
        fun `add watch list entries and fire command containing the progress`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<WatchListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val entry1 = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/37989")),
                    title = "Golden Kamuy 2nd Season",
                    type = TV,
                    episodes = 12,
                    status = FINISHED,
                    animeSeason = AnimeSeason(),
                    picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                    duration = Duration(23, MINUTES),
                )
                val entry2 = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/40059")),
                    title = "Golden Kamuy 3rd Season",
                    type = TV,
                    episodes = 12,
                    status = ONGOING,
                    animeSeason = AnimeSeason(),
                    picture = URI("https://cdn.myanimelist.net/images/anime/1763/108108.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
                    duration = Duration(23, MINUTES),
                )

                val savedEntries = mutableListOf<WatchListEntry>()
                val state = object: State by TestState {
                    override fun createSnapshot(): Snapshot = TestSnapshot
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                        savedEntries.addAll(anime)
                    }
                }

                val testCache = object: AnimeCache by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return when(key) {
                            entry1.sources.first() -> PresentValue(entry1)
                            entry2.sources.first() -> PresentValue(entry2)
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val defaultListHandler = DefaultListHandler(
                    state = state,
                    commandHistory = testCommandHistory,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.addWatchListEntry(setOf(entry1.sources.first(), entry2.sources.first()))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, stop
                assertThat(savedEntries).containsExactlyInAnyOrder(WatchListEntry(entry1), WatchListEntry(entry2))
            }
        }

        @Test
        fun `don't do anything with entries for which the cache does not return anything, but the update events must indicate that we actually processed it`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<WatchListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val entry = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/37989")),
                    title = "Golden Kamuy 2nd Season",
                    type = TV,
                    episodes = 12,
                    status = FINISHED,
                    animeSeason = AnimeSeason(),
                    picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                    duration = Duration(23, MINUTES),
                )
                val deadEntry = URI("https://myanimelist.net/anime/10001")

                val savedEntries = mutableListOf<WatchListEntry>()
                val state = object: State by TestState {
                    override fun createSnapshot(): Snapshot = TestSnapshot
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                        savedEntries.addAll(anime)
                    }
                }

                val testCache = object: AnimeCache by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return when(key) {
                            entry.sources.first() -> PresentValue(entry)
                            deadEntry -> DeadEntry()
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val defaultListHandler = DefaultListHandler(
                    state = state,
                    commandHistory = testCommandHistory,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.addWatchListEntry(setOf(deadEntry, entry.sources.first()))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(savedEntries).containsExactlyInAnyOrder(WatchListEntry(entry))
                assertThat(receivedEvents).hasSize(3) // initial, start, stop
            }
        }

        @Test
        fun `removes the entry from any search states upon adding it to watchList`() {
            runBlocking {
                // given
                val testAnime = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/37989")),
                    title = "Golden Kamuy 2nd Season",
                    type = TV,
                    episodes = 12,
                    status = FINISHED,
                    animeSeason = AnimeSeason(),
                    picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                    duration = Duration(23, MINUTES),
                )

                val testSearchListEntry = SearchResultAnimeEntry(testAnime)

                CoroutinesFlowEventBus.findRelatedAnimeState.update { FindRelatedAnimeState(entries = listOf(testSearchListEntry)) }
                val findRelatedAnimeStateEvents = mutableListOf<FindRelatedAnimeState>()
                val findRelatedAnimeStateEventCollector = launch { CoroutinesFlowEventBus.findRelatedAnimeState.collect { event -> findRelatedAnimeStateEvents.add(event) } }

                CoroutinesFlowEventBus.findByTitleState.update { FindByTitleState(unlistedResults = listOf(testSearchListEntry)) }
                val findByTitleStateEvents = mutableListOf<FindByTitleState>()
                val findByTitleStateEventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> findByTitleStateEvents.add(event) } }

                CoroutinesFlowEventBus.findSeasonState.update { FindSeasonState(entries = listOf(testSearchListEntry)) }
                val findSeasonStateEvents = mutableListOf<FindSeasonState>()
                val findSeasonStateEventCollector = launch { CoroutinesFlowEventBus.findSeasonState.collect { event -> findSeasonStateEvents.add(event) } }

                CoroutinesFlowEventBus.findByCriertiaState.update { FindByCriteriaState(entries = listOf(testSearchListEntry)) }
                val findByCriteriaStateEvents = mutableListOf<FindByCriteriaState>()
                val findByCriteriaStateEventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> findByCriteriaStateEvents.add(event) } }

                CoroutinesFlowEventBus.findSimilarAnimeState.update { FindSimilarAnimeState(entries = listOf(testSearchListEntry)) }
                val findSimilarAnimeStateEvents = mutableListOf<FindSimilarAnimeState>()
                val findSimilarAnimeStateEventCollector = launch { CoroutinesFlowEventBus.findSimilarAnimeState.collect { event -> findSimilarAnimeStateEvents.add(event) } }
                delay(100)

                val state = object: State by TestState {
                    override fun openedFile(): OpenedFile = NoFile
                    override fun animeListEntryExists(anime: AnimeListEntry): Boolean = false
                    override fun createSnapshot(): Snapshot = TestSnapshot
                    override fun watchList(): Set<WatchListEntry> = emptySet()
                    override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {}
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val testWatchListEntry = WatchListEntry(testAnime)

                val testCache = object: AnimeCache by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return when(key) {
                            testWatchListEntry.link.uri -> PresentValue(testAnime)
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val defaultListHandler = DefaultListHandler(
                    state = state,
                    commandHistory = testCommandHistory,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.addWatchListEntry(setOf(testWatchListEntry.link.uri))

                // then
                delay(100)
                findRelatedAnimeStateEventCollector.cancelAndJoin()
                findByTitleStateEventCollector.cancelAndJoin()
                findSeasonStateEventCollector.cancelAndJoin()
                findByCriteriaStateEventCollector.cancelAndJoin()
                findSimilarAnimeStateEventCollector.cancelAndJoin()

                assertThat(findRelatedAnimeStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findByTitleStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findSeasonStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findByCriteriaStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findSimilarAnimeStateEvents).hasSize(2) // initial (including entry), update
            }
        }
    }

    @Nested
    inner class AddIgnoreListEntryTests {

        @Test
        fun `add ignore list entries and fire command containing the progress`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<IgnoreListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val entry1 = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/37989")),
                    title = "Golden Kamuy 2nd Season",
                    type = TV,
                    episodes = 12,
                    status = FINISHED,
                    animeSeason = AnimeSeason(),
                    picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                    duration = Duration(23, MINUTES),
                )
                val entry2 = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/40059")),
                    title = "Golden Kamuy 3rd Season",
                    type = TV,
                    episodes = 12,
                    status = ONGOING,
                    animeSeason = AnimeSeason(),
                    picture = URI("https://cdn.myanimelist.net/images/anime/1763/108108.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
                    duration = Duration(23, MINUTES),
                )

                val savedEntries = mutableListOf<IgnoreListEntry>()
                val state = object: State by TestState {
                    override fun createSnapshot(): Snapshot = TestSnapshot
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                        savedEntries.addAll(anime)
                    }
                }

                val testCache = object: AnimeCache by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return when(key) {
                            entry1.sources.first() -> PresentValue(entry1)
                            entry2.sources.first() -> PresentValue(entry2)
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val defaultListHandler = DefaultListHandler(
                    state = state,
                    commandHistory = testCommandHistory,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.addIgnoreListEntry(setOf(entry1.sources.first(), entry2.sources.first()))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(savedEntries).containsExactlyInAnyOrder(IgnoreListEntry(entry1), IgnoreListEntry(entry2))
                assertThat(receivedEvents).hasSize(3) // initial, start, stop
            }
        }

        @Test
        fun `don't do anything with entries for which the cache does not return anything, but the update events must indicate that we actually processed it`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<IgnoreListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val entry = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/37989")),
                    title = "Golden Kamuy 2nd Season",
                    type = TV,
                    episodes = 12,
                    status = FINISHED,
                    animeSeason = AnimeSeason(),
                    picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                    duration = Duration(23, MINUTES),
                )
                val deadEntry = URI("https://myanimelist.net/anime/10001")

                val savedEntries = mutableListOf<IgnoreListEntry>()
                val state = object: State by TestState {
                    override fun createSnapshot(): Snapshot = TestSnapshot
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                        savedEntries.addAll(anime)
                    }
                }

                val testCache = object: AnimeCache by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return when(key) {
                            entry.sources.first() -> PresentValue(entry)
                            deadEntry -> DeadEntry()
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val defaultListHandler = DefaultListHandler(
                    state = state,
                    commandHistory = testCommandHistory,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.addIgnoreListEntry(setOf(deadEntry, entry.sources.first()))

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(savedEntries).containsExactlyInAnyOrder(IgnoreListEntry(entry))
                assertThat(receivedEvents).hasSize(3) // initial, start, stop
            }
        }

        @Test
        fun `removes the entry from any search states upon adding it to ignoreList`() {
            runBlocking {
                // given
                val testAnime = Anime(
                    sources = hashSetOf(URI("https://myanimelist.net/anime/37989")),
                    title = "Golden Kamuy 2nd Season",
                    type = TV,
                    episodes = 12,
                    status = FINISHED,
                    animeSeason = AnimeSeason(),
                    picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                    duration = Duration(23, MINUTES),
                )

                val testSearchListEntry = SearchResultAnimeEntry(testAnime)

                CoroutinesFlowEventBus.findRelatedAnimeState.update { FindRelatedAnimeState(entries = listOf(testSearchListEntry)) }
                val findRelatedAnimeStateEvents = mutableListOf<FindRelatedAnimeState>()
                val findRelatedAnimeStateEventCollector = launch { CoroutinesFlowEventBus.findRelatedAnimeState.collect { event -> findRelatedAnimeStateEvents.add(event) } }

                CoroutinesFlowEventBus.findByTitleState.update { FindByTitleState(unlistedResults = listOf(testSearchListEntry)) }
                val findByTitleStateEvents = mutableListOf<FindByTitleState>()
                val findByTitleStateEventCollector = launch { CoroutinesFlowEventBus.findByTitleState.collect { event -> findByTitleStateEvents.add(event) } }

                CoroutinesFlowEventBus.findSeasonState.update { FindSeasonState(entries = listOf(testSearchListEntry)) }
                val findSeasonStateEvents = mutableListOf<FindSeasonState>()
                val findSeasonStateEventCollector = launch { CoroutinesFlowEventBus.findSeasonState.collect { event -> findSeasonStateEvents.add(event) } }

                CoroutinesFlowEventBus.findByCriertiaState.update { FindByCriteriaState(entries = listOf(testSearchListEntry)) }
                val findByCriteriaStateEvents = mutableListOf<FindByCriteriaState>()
                val findByCriteriaStateEventCollector = launch { CoroutinesFlowEventBus.findByCriertiaState.collect { event -> findByCriteriaStateEvents.add(event) } }

                CoroutinesFlowEventBus.findSimilarAnimeState.update { FindSimilarAnimeState(entries = listOf(testSearchListEntry)) }
                val findSimilarAnimeStateEvents = mutableListOf<FindSimilarAnimeState>()
                val findSimilarAnimeStateEventCollector = launch { CoroutinesFlowEventBus.findSimilarAnimeState.collect { event -> findSimilarAnimeStateEvents.add(event) } }
                delay(100)

                val state = object: State by TestState {
                    override fun openedFile(): OpenedFile = NoFile
                    override fun animeListEntryExists(anime: AnimeListEntry): Boolean = false
                    override fun createSnapshot(): Snapshot = TestSnapshot
                    override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
                    override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {}
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val testIgnoreListEntry = IgnoreListEntry(testAnime)

                val testCache = object: AnimeCache by TestAnimeCache {
                    override suspend fun fetch(key: URI): CacheEntry<Anime> {
                        return when(key) {
                            testIgnoreListEntry.link.uri -> PresentValue(testAnime)
                            else -> shouldNotBeInvoked()
                        }
                    }
                }

                val defaultListHandler = DefaultListHandler(
                    state = state,
                    commandHistory = testCommandHistory,
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.addIgnoreListEntry(setOf(testIgnoreListEntry.link.uri))

                // then
                delay(100)
                findRelatedAnimeStateEventCollector.cancelAndJoin()
                findByTitleStateEventCollector.cancelAndJoin()
                findSeasonStateEventCollector.cancelAndJoin()
                findByCriteriaStateEventCollector.cancelAndJoin()
                findSimilarAnimeStateEventCollector.cancelAndJoin()

                assertThat(findRelatedAnimeStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findByTitleStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findSeasonStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findByCriteriaStateEvents).hasSize(2) // initial (including entry), update
                assertThat(findSimilarAnimeStateEvents).hasSize(2) // initial (including entry), update
            }
        }
    }

    @Nested
    inner class RemoveAnimeListEntryTests {

        @Test
        fun  `remove anime list entry`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<AnimeListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.animeListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val expectedEntry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/57"),
                    title = "Beck",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                    episodes = 26,
                    type = TV,
                    location = Path("some/relative/path/beck"),
                )

                var resultingEntry: AnimeListEntry? = null
                val testState = object: State by TestState {
                    override fun animeList(): List<AnimeListEntry> = listOf(expectedEntry)
                    override fun animeListEntryExists(anime: AnimeListEntry): Boolean = true
                    override fun createSnapshot(): Snapshot = StateSnapshot()
                    override fun removeAnimeListEntry(entry: AnimeListEntry) {
                        resultingEntry = entry
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val defaultListHandler = DefaultListHandler(
                    state = testState,
                    commandHistory = testCommandHistory,
                    cache = TestAnimeCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.removeAnimeListEntry(expectedEntry)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(1) // initial
                assertThat(resultingEntry).isEqualTo(expectedEntry)
            }
        }
    }

    @Nested
    inner class RemoveWatchListEntryTests {

        @Test
        fun `remove watch list entry`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<WatchListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val expectedEntry = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                )

                var resultingEntry: WatchListEntry? = null
                val testState = object: State by TestState {
                    override fun createSnapshot(): Snapshot = StateSnapshot()
                    override fun watchList(): Set<WatchListEntry> = setOf(expectedEntry)
                    override fun removeWatchListEntry(entry: WatchListEntry) {
                        resultingEntry = entry
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val defaultListHandler = DefaultListHandler(
                    state = testState,
                    commandHistory = testCommandHistory,
                    cache = TestAnimeCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.removeWatchListEntry(expectedEntry)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(1) // initial
                assertThat(resultingEntry).isEqualTo(expectedEntry)
            }
        }
    }

    @Nested
    inner class RemoveIgnoreListEntryTests {

        @Test
        fun `remove ignore list entry`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<IgnoreListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val expectedEntry = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                )

                var resultingEntry: IgnoreListEntry? = null
                val testState = object: State by TestState {
                    override fun createSnapshot(): Snapshot = StateSnapshot()
                    override fun ignoreList(): Set<IgnoreListEntry> = setOf(expectedEntry)
                    override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
                        resultingEntry = entry
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) { }
                }

                val defaultListHandler = DefaultListHandler(
                    state = testState,
                    commandHistory = testCommandHistory,
                    cache = TestAnimeCache,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultListHandler.removeIgnoreListEntry(expectedEntry)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(1) // initial
                assertThat(resultingEntry).isEqualTo(expectedEntry)
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultListHandler.instance

            // when
            val result = DefaultListHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultListHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}