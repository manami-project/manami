package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.cache.TestAnimeCache
import io.github.manamiproject.manami.app.commands.ReversibleCommand
import io.github.manamiproject.manami.app.commands.TestCommandHistory
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.InconsistenciesState
import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.AnimeListEpisodesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.EpisodeDiff
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataDiff
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistencyHandler
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataDiff
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistencyHandler
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.NoFile
import io.github.manamiproject.manami.app.state.OpenedFile
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import io.github.manamiproject.modb.core.anime.AnimeType
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

internal class DefaultInconsistenciesHandlerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class MetaDataInconsistenciesTests {

        @Nested
        inner class NotificationTests {

            @Test
            fun `post event for differing watch list entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testMetaDataInconsistencyHandler = object :
                        InconsistencyHandler<MetaDataInconsistenciesResult> by TestMetaDataInconsistencyHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): MetaDataInconsistenciesResult = MetaDataInconsistenciesResult(
                            watchListResults = listOf(
                                MetaDataDiff(
                                    currentEntry = WatchListEntry(
                                        link = Link("https://myanimelist.net/anime/5114"),
                                        title = "FMB",
                                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                                    ),
                                    newEntry = WatchListEntry(
                                        link = Link("https://myanimelist.net/anime/5114"),
                                        title = "Fullmetal Alchemist: Brotherhood",
                                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                                    ),
                                )
                            )
                        )
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = TestState,
                        cache = TestAnimeCache,
                        commandHistory = TestCommandHistory,
                        inconsistencyHandlers = listOf(testMetaDataInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = false,
                        checkMetaData = true,
                    )

                    // when
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(2) // initial, update
                    assertThat(receivedEvents.last().metaDataInconsistencies.ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().metaDataInconsistencies.watchListResults).hasSize(1)
                }
            }

            @Test
            fun `post event for differing ignore list entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testMetaDataInconsistencyHandler = object :
                        InconsistencyHandler<MetaDataInconsistenciesResult> by TestMetaDataInconsistencyHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): MetaDataInconsistenciesResult = MetaDataInconsistenciesResult(
                            ignoreListResults = listOf(
                                MetaDataDiff(
                                    currentEntry = IgnoreListEntry(
                                        link = Link("https://myanimelist.net/anime/28981"),
                                        title = "Ameiro Cocoa",
                                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                                    ),
                                    newEntry = IgnoreListEntry(
                                        link = Link("https://myanimelist.net/anime/28981"),
                                        title = "Ame-iro Cocoa",
                                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                                    ),
                                )
                            )
                        )
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = TestState,
                        cache = TestAnimeCache,
                        commandHistory = TestCommandHistory,
                        inconsistencyHandlers = listOf(testMetaDataInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = false,
                        checkMetaData = true,
                    )

                    // when
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(2) // initial, update
                    assertThat(receivedEvents.last().metaDataInconsistencies.ignoreListResults).hasSize(1)
                    assertThat(receivedEvents.last().metaDataInconsistencies.watchListResults).isEmpty()
                }
            }
        }

        @Nested
        inner class FixMetaDataInconsistenciesTests {

            @Test
            fun `do nothing if there are no findings`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector =
                        launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = TestState,
                        cache = TestAnimeCache,
                        commandHistory = TestCommandHistory,
                        inconsistencyHandlers = listOf(MetaDataInconsistencyHandler()),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    // when
                    defaultInconsistenciesHandler.fixMetaDataInconsistencies()

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(1) // initial
                }
            }

            @Test
            fun `replace current watch list entry with the new instance`() {
                runBlocking {
                    // given
                    val metaDataDiff = MetaDataDiff(
                        currentEntry = WatchListEntry(
                            link = Link("https://myanimelist.net/anime/5114"),
                            title = "FMB",
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                        ),
                        newEntry = WatchListEntry(
                            link = Link("https://myanimelist.net/anime/5114"),
                            title = "Fullmetal Alchemist: Brotherhood",
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                        ),
                    )
                    val testMetaDataInconsistencyHandler = object: InconsistencyHandler<MetaDataInconsistenciesResult> by TestMetaDataInconsistencyHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): MetaDataInconsistenciesResult = MetaDataInconsistenciesResult(
                            watchListResults = listOf(metaDataDiff)
                        )
                    }

                    val removedEntries = mutableListOf<WatchListEntry>()
                    val addedEntries = mutableListOf<WatchListEntry>()
                    val testState = object: State by TestState {
                        override fun createSnapshot(): Snapshot = StateSnapshot()
                        override fun removeWatchListEntry(entry: WatchListEntry) {
                            removedEntries.add(entry)
                        }
                        override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                            addedEntries.addAll(anime)
                        }
                    }

                    val testCommandHistory = object: CommandHistory by TestCommandHistory {
                        override fun push(command: ReversibleCommand) {}
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = testState,
                        cache = TestAnimeCache,
                        commandHistory = testCommandHistory,
                        inconsistencyHandlers = listOf(testMetaDataInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = false,
                        checkMetaData = true,
                    )
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // when
                    defaultInconsistenciesHandler.fixMetaDataInconsistencies()

                    // then
                    assertThat(removedEntries).containsExactly(metaDataDiff.currentEntry)
                    assertThat(addedEntries).containsExactly(metaDataDiff.newEntry)
                }
            }

            @Test
            fun `replace current ignore list entry with the new instance`() {
                runBlocking {
                    // given
                    val metaDataDiff = MetaDataDiff(
                        currentEntry = IgnoreListEntry(
                            link = Link("https://myanimelist.net/anime/28981"),
                            title = "Ameiro Cocoa",
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                        ),
                        newEntry = IgnoreListEntry(
                            link = Link("https://myanimelist.net/anime/28981"),
                            title = "Ame-iro Cocoa",
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                        ),
                    )
                    val testMetaDataInconsistencyHandler = object: InconsistencyHandler<MetaDataInconsistenciesResult> by TestMetaDataInconsistencyHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): MetaDataInconsistenciesResult = MetaDataInconsistenciesResult(
                            ignoreListResults = listOf(metaDataDiff)
                        )
                    }

                    val removedEntries = mutableListOf<IgnoreListEntry>()
                    val addedEntries = mutableListOf<IgnoreListEntry>()
                    val testState = object: State by TestState {
                        override fun createSnapshot(): Snapshot = StateSnapshot()
                        override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
                            removedEntries.add(entry)
                        }
                        override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                            addedEntries.addAll(anime)
                        }
                    }

                    val testCommandHistory = object: CommandHistory by TestCommandHistory {
                        override fun push(command: ReversibleCommand) {}
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = testState,
                        cache = TestAnimeCache,
                        commandHistory = testCommandHistory,
                        inconsistencyHandlers = listOf(testMetaDataInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = false,
                        checkMetaData = true,
                    )
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // when
                    defaultInconsistenciesHandler.fixMetaDataInconsistencies()

                    // then
                    assertThat(removedEntries).containsExactly(metaDataDiff.currentEntry)
                    assertThat(addedEntries).containsExactly(metaDataDiff.newEntry)
                }
            }
        }
    }

    @Nested
    inner class DeadEntriesTests {

        @Nested
        inner class NotificationTests {

            @Test
            fun `post event for differing watch list entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testDeadEntriesInconsistencyHandler = object: InconsistencyHandler<DeadEntriesInconsistenciesResult> by TestDeadEntriesInconsistencyHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): DeadEntriesInconsistenciesResult = DeadEntriesInconsistenciesResult(
                            watchListResults = listOf(
                                WatchListEntry(
                                    link = Link("https://myanimelist.net/anime/5114"),
                                    title = "Fullmetal Alchemist: Brotherhood",
                                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                                ),
                            )
                        )
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = TestState,
                        cache = TestAnimeCache,
                        commandHistory = TestCommandHistory,
                        inconsistencyHandlers = listOf(testDeadEntriesInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = true,
                        checkMetaData = false,
                    )

                    // when
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(2) // initial, update
                    assertThat(receivedEvents.last().deadEntryInconsistencies.ignoreListResults).isEmpty()
                    assertThat(receivedEvents.last().deadEntryInconsistencies.watchListResults).hasSize(1)
                }
            }

            @Test
            fun `post event for differing ignore list entries`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testDeadEntriesInconsistencyHandler = object: InconsistencyHandler<DeadEntriesInconsistenciesResult> by TestDeadEntriesInconsistencyHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): DeadEntriesInconsistenciesResult = DeadEntriesInconsistenciesResult(
                            ignoreListResults = listOf(
                                IgnoreListEntry(
                                    link = Link("https://myanimelist.net/anime/28981"),
                                    title = "Ame-iro Cocoa",
                                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                                ),
                            )
                        )
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = TestState,
                        cache = TestAnimeCache,
                        commandHistory = TestCommandHistory,
                        inconsistencyHandlers = listOf(testDeadEntriesInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = true,
                        checkMetaData = false,
                    )

                    // when
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(2) // initial, update
                    assertThat(receivedEvents.last().deadEntryInconsistencies.ignoreListResults).hasSize(1)
                    assertThat(receivedEvents.last().deadEntryInconsistencies.watchListResults).isEmpty()
                }
            }
        }

        @Nested
        inner class FixDeadEntryInconsistenciesTests {

            @Test
            fun `do nothing if there are no findings`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = TestState,
                        cache = TestAnimeCache,
                        commandHistory = TestCommandHistory,
                        inconsistencyHandlers = listOf(DeadEntriesInconsistencyHandler()),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    // when
                    defaultInconsistenciesHandler.fixDeadEntryInconsistencies()

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(1) // initial
                }
            }

            @Test
            fun `remove entry from watch list`() {
                runBlocking {
                    // given
                    val watchListEntry = WatchListEntry(
                        link = Link("https://myanimelist.net/anime/5114"),
                        title = "Fullmetal Alchemist: Brotherhood",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                    )
                    val testDeadEntriesInconsistencyHandler = object: InconsistencyHandler<DeadEntriesInconsistenciesResult> by TestDeadEntriesInconsistencyHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): DeadEntriesInconsistenciesResult = DeadEntriesInconsistenciesResult(
                            watchListResults = listOf(watchListEntry)
                        )
                    }

                    val removedEntries = mutableListOf<WatchListEntry>()
                    val testState = object: State by TestState {
                        override fun createSnapshot(): Snapshot = StateSnapshot()
                        override fun removeWatchListEntry(entry: WatchListEntry) {
                            removedEntries.add(entry)
                        }
                    }

                    val testCommandHistory = object: CommandHistory by TestCommandHistory {
                        override fun push(command: ReversibleCommand) {}
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = testState,
                        cache = TestAnimeCache,
                        commandHistory = testCommandHistory,
                        inconsistencyHandlers = listOf(testDeadEntriesInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = true,
                        checkMetaData = false,
                    )
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // when
                    defaultInconsistenciesHandler.fixDeadEntryInconsistencies()

                    // then
                    assertThat(removedEntries).containsExactly(watchListEntry)
                }
            }

            @Test
            fun `remove entry from ignore list`() {
                runBlocking {
                    // given
                    val ignoreListEntry = IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/28981"),
                        title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                    )
                    val testDeadEntriesInconsistencyHandler = object: InconsistencyHandler<DeadEntriesInconsistenciesResult> by TestDeadEntriesInconsistencyHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): DeadEntriesInconsistenciesResult = DeadEntriesInconsistenciesResult(
                            ignoreListResults = listOf(ignoreListEntry)
                        )
                    }

                    val removedEntries = mutableListOf<IgnoreListEntry>()
                    val testState = object: State by TestState {
                        override fun createSnapshot(): Snapshot = StateSnapshot()
                        override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
                            removedEntries.add(entry)
                        }
                    }

                    val testCommandHistory = object: CommandHistory by TestCommandHistory {
                        override fun push(command: ReversibleCommand) {}
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = testState,
                        cache = TestAnimeCache,
                        commandHistory = testCommandHistory,
                        inconsistencyHandlers = listOf(testDeadEntriesInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = true,
                        checkMetaData = false,
                    )
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // when
                    defaultInconsistenciesHandler.fixDeadEntryInconsistencies()

                    // then
                    assertThat(removedEntries).containsExactly(ignoreListEntry)
                }
            }
        }
    }

    @Nested
    inner class AnimeListEntryMetaDataInconsistenciesTests {

        @Nested
        inner class FixAnimeListEntryMetaDataInconsistenciesTests {

            @Test
            fun `do nothing if both entries are identical`() {
                runBlocking {
                    // given
                    val currentEntry = AnimeListEntry(
                        title = "test",
                        link = Link(URI("https://example.org/anime/1")),
                        episodes = 1,
                        type = AnimeType.UNKNOWN,
                        location = Path(""),
                    )
                    val replacementEntry = currentEntry.copy()

                    val animeListMetaDataDiff = AnimeListMetaDataDiff(
                        currentEntry = currentEntry,
                        replacementEntry = replacementEntry,
                    )

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = TestState,
                        cache = TestAnimeCache,
                        commandHistory = TestCommandHistory,
                        inconsistencyHandlers = listOf(MetaDataInconsistencyHandler()),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    // when
                    defaultInconsistenciesHandler.fixAnimeListEntryMetaDataInconsistencies(animeListMetaDataDiff)
                }
            }

            @Test
            fun `replace current anime list entry with the new instance`() {
                runBlocking {
                    // given
                    val currentEntry = AnimeListEntry(
                        title = "test",
                        link = Link(URI("https://example.org/anime/1")),
                        episodes = 1,
                        type = AnimeType.UNKNOWN,
                        location = Path(""),
                    )
                    val replacementEntry = currentEntry.copy(
                        location = Path("./test"),
                    )

                    val animeListMetaDataDiff = AnimeListMetaDataDiff(
                        currentEntry = currentEntry,
                        replacementEntry = replacementEntry,
                    )

                    val removedEntries = mutableListOf<AnimeListEntry>()
                    val addedEntries = mutableListOf<AnimeListEntry>()
                    val testState = object: State by TestState {
                        override fun openedFile(): OpenedFile = NoFile
                        override fun createSnapshot(): Snapshot = StateSnapshot()
                        override fun animeListEntryExists(anime: AnimeListEntry): Boolean = true
                        override fun removeAnimeListEntry(entry: AnimeListEntry) {
                            removedEntries.add(entry)
                        }
                        override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                            addedEntries.addAll(anime)
                        }
                    }

                    val testCommandHistory = object: CommandHistory by TestCommandHistory {
                        override fun push(command: ReversibleCommand) {}
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = testState,
                        cache = TestAnimeCache,
                        commandHistory = testCommandHistory,
                        inconsistencyHandlers = listOf(MetaDataInconsistencyHandler()),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    // when
                    defaultInconsistenciesHandler.fixAnimeListEntryMetaDataInconsistencies(animeListMetaDataDiff)

                    // then
                    assertThat(removedEntries).hasSize(1)
                    assertThat(addedEntries).hasSize(1)
                    assertThat(removedEntries.first()).isEqualTo(currentEntry)
                    assertThat(addedEntries.first()).isEqualTo(replacementEntry)
                }
            }
        }

        @Nested
        inner class NotificationTests {

            @Test
            fun `post event for AnimeListMetaDataInconsistenciesResult`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val currentEntry = AnimeListEntry(
                        title = "test",
                        link = Link(URI("https://example.org/anime/1")),
                        episodes = 1,
                        type = AnimeType.UNKNOWN,
                        location = Path(""),
                    )

                    val testInconsistencyHandler = object : InconsistencyHandler<AnimeListMetaDataInconsistenciesResult> by TestAnimeListMetaDataInconsistencyHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): AnimeListMetaDataInconsistenciesResult = AnimeListMetaDataInconsistenciesResult(
                            entries = listOf(
                                AnimeListMetaDataDiff(
                                    currentEntry = currentEntry,
                                    replacementEntry = currentEntry.copy(type = AnimeType.TV),
                                ),
                            ),
                        )
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = TestState,
                        cache = TestAnimeCache,
                        commandHistory = TestCommandHistory,
                        inconsistencyHandlers = listOf(testInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = false,
                        checkMetaData = true,
                    )

                    // when
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(2) // initial, update
                    assertThat(receivedEvents.last().animeListMetaDataInconsistencies.entries).hasSize(1)
                }
            }

            @Test
            fun `post event for AnimeListEpisodesInconsistenciesResult`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val currentEntry = AnimeListEntry(
                        title = "test",
                        link = Link(URI("https://example.org/anime/1")),
                        episodes = 1,
                        type = AnimeType.UNKNOWN,
                        location = Path(""),
                    )

                    val testInconsistencyHandler = object : InconsistencyHandler<AnimeListEpisodesInconsistenciesResult> by TestAnimeListEpisodesInconsistenciesHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): AnimeListEpisodesInconsistenciesResult = AnimeListEpisodesInconsistenciesResult(
                            entries = listOf(
                                EpisodeDiff(
                                    animeListEntry = currentEntry,
                                    numberOfFiles = 2,
                                ),
                            ),
                        )
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = TestState,
                        cache = TestAnimeCache,
                        commandHistory = TestCommandHistory,
                        inconsistencyHandlers = listOf(testInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = false,
                        checkMetaData = true,
                    )

                    // when
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(2) // initial, update
                    assertThat(receivedEvents.last().animeListEpisodesInconsistencies.entries).hasSize(1)
                }
            }

            @Test
            fun `post event for AnimeListDeadEntriesInconsistenciesResult`() {
                runBlocking {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val currentEntry = AnimeListEntry(
                        title = "test",
                        link = Link(URI("https://example.org/anime/1")),
                        episodes = 1,
                        type = AnimeType.UNKNOWN,
                        location = Path(""),
                    )

                    val testInconsistencyHandler = object : InconsistencyHandler<AnimeListDeadEntriesInconsistenciesResult> by TestAnimeListDeadEntriesInconsistenciesHandler {
                        override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                        override fun calculateWorkload(): Int = 1
                        override suspend fun execute(): AnimeListDeadEntriesInconsistenciesResult = AnimeListDeadEntriesInconsistenciesResult(
                            entries = listOf(currentEntry),
                        )
                    }

                    val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                        state = TestState,
                        cache = TestAnimeCache,
                        commandHistory = TestCommandHistory,
                        inconsistencyHandlers = listOf(testInconsistencyHandler),
                        eventBus = CoroutinesFlowEventBus,
                    )

                    val config = InconsistenciesSearchConfig(
                        checkDeadEntries = false,
                        checkMetaData = true,
                    )

                    // when
                    defaultInconsistenciesHandler.findInconsistencies(config)

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(2) // initial, update
                    assertThat(receivedEvents.last().animeListDeadEntriesInconsistencies.entries).containsExactly(currentEntry)
                }
            }
        }
    }

    @Nested
    inner class FindInconsistenciesTests {

        @Test
        fun `do nothing if option hasn't been activated`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<InconsistenciesState>()
                val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testInconsistencyHandler = object: InconsistencyHandler<String> by TestInconsistencyHandler {
                    override fun calculateWorkload(): Int = 10
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = false
                }

                val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                    state = TestState,
                    cache = TestAnimeCache,
                    commandHistory = TestCommandHistory,
                    inconsistencyHandlers = listOf(testInconsistencyHandler),
                    eventBus = CoroutinesFlowEventBus,
                )

                val config = InconsistenciesSearchConfig()

                // when
                defaultInconsistenciesHandler.findInconsistencies(config)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(1) // initial
            }
        }

        @Test
        fun `do nothing if there is no workload`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<InconsistenciesState>()
                val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val testInconsistencyHandler = object: InconsistencyHandler<String> by TestInconsistencyHandler {
                    override fun calculateWorkload(): Int = 0
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                }

                val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                    state = TestState,
                    cache = TestAnimeCache,
                    commandHistory = TestCommandHistory,
                    inconsistencyHandlers = listOf(testInconsistencyHandler),
                    eventBus = CoroutinesFlowEventBus,
                )

                val config = InconsistenciesSearchConfig()

                // when
                defaultInconsistenciesHandler.findInconsistencies(config)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(1) // initial
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultInconsistenciesHandler.instance

            // when
            val result = DefaultInconsistenciesHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultInconsistenciesHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}