package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.cache.TestAnimeCache
import io.github.manamiproject.manami.app.inconsistencies.deadentries.DeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.deadentries.DeadEntriesInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.deadentries.DeadEntriesInconsistencyHandler
import io.github.manamiproject.manami.app.inconsistencies.metadata.MetaDataDiff
import io.github.manamiproject.manami.app.inconsistencies.metadata.MetaDataInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.metadata.MetaDataInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.metadata.MetaDataInconsistencyHandler
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.state.commands.ReversibleCommand
import io.github.manamiproject.manami.app.state.commands.TestCommandHistory
import io.github.manamiproject.manami.app.state.commands.history.CommandHistory
import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.TestEventBus
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import org.assertj.core.api.Assertions.`in`
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI

internal class DefaultInconsistenciesHandlerTest {

    @Nested
    inner class FindInconsistenciesTests {

        @Test
        fun `do nothing if option hasn't been activated`() {
            // given
            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun subscribe(subscriber: Any) {}
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val testInconsistencyHandler = object: InconsistencyHandler<String> by TestInconsistencyHandler {
                override fun calculateWorkload(): Int = 10
                override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = false
            }

            val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                state = TestState,
                cache = TestAnimeCache,
                commandHistory = TestCommandHistory,
                inconsistencyHandlers = listOf(testInconsistencyHandler),
                eventBus = testEventBus
            )

            val config = InconsistenciesSearchConfig()

            // when
            defaultInconsistenciesHandler.findInconsistencies(config)

            // then
            assertThat(receivedEvents).hasSize(1)
            assertThat(receivedEvents.first()).isInstanceOf(InconsistenciesCheckFinishedEvent::class.java)
        }

        @Test
        fun `do nothing if there is no workload`() {
            // given
            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun subscribe(subscriber: Any) {}
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val testInconsistencyHandler = object: InconsistencyHandler<String> by TestInconsistencyHandler {
                override fun calculateWorkload(): Int = 0
                override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
            }

            val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                state = TestState,
                cache = TestAnimeCache,
                commandHistory = TestCommandHistory,
                inconsistencyHandlers = listOf(testInconsistencyHandler),
                eventBus = testEventBus
            )

            val config = InconsistenciesSearchConfig()

            // when
            defaultInconsistenciesHandler.findInconsistencies(config)

            // then
            assertThat(receivedEvents).hasSize(1)
            assertThat(receivedEvents.first()).isInstanceOf(InconsistenciesCheckFinishedEvent::class.java)
        }

        @Test
        fun `successfully execute handler and send progress event`() {
            // given
            val receivedEvents = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun subscribe(subscriber: Any) {}
                override fun post(event: Event) {
                    receivedEvents.add(event)
                }
            }

            val testInconsistencyHandler = object: InconsistencyHandler<String> by TestInconsistencyHandler {
                override fun calculateWorkload(): Int = 1
                override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                override fun execute(progressUpdate: (Int) -> Unit): String {
                    progressUpdate.invoke(1)
                    return "here is the result"
                }
            }

            val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                state = TestState,
                cache = TestAnimeCache,
                commandHistory = TestCommandHistory,
                inconsistencyHandlers = listOf(testInconsistencyHandler),
                eventBus = testEventBus
            )

            val config = InconsistenciesSearchConfig()

            // when
            defaultInconsistenciesHandler.findInconsistencies(config)

            // then
            assertThat(receivedEvents).hasSize(2)
            assertThat(receivedEvents.first()).isInstanceOf(InconsistenciesProgressEvent::class.java)
            assertThat(receivedEvents[1]).isInstanceOf(InconsistenciesCheckFinishedEvent::class.java)
        }
    }

    @Nested
    inner class MetaDataInconsistenciesTests {

        @Nested
        inner class NotificationTests {

            @Test
            fun `post event for differing watch list entries`() {
                // given
                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val testMetaDataInconsistencyHandler = object: InconsistencyHandler<MetaDataInconsistenciesResult> by TestMetaDataInconsistencyHandler {
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                    override fun calculateWorkload(): Int = 1
                    override fun execute(progressUpdate: (Int) -> Unit): MetaDataInconsistenciesResult = MetaDataInconsistenciesResult(
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
                    eventBus = testEventBus,
                )

                val config = InconsistenciesSearchConfig(
                    checkDeadEntries = false,
                    checkMetaData = true,
                )

                // when
                defaultInconsistenciesHandler.findInconsistencies(config)

                // then
                assertThat(receivedEvents).hasSize(2)
                assertThat(receivedEvents.first()).isInstanceOf(MetaDataInconsistenciesResultEvent::class.java)
                assertThat((receivedEvents.first() as MetaDataInconsistenciesResultEvent).numberOfAffectedEntries).isOne()
                assertThat(receivedEvents.last()).isInstanceOf(InconsistenciesCheckFinishedEvent::class.java)
            }

            @Test
            fun `post event for differing ignore list entries`() {
                // given
                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val testMetaDataInconsistencyHandler = object: InconsistencyHandler<MetaDataInconsistenciesResult> by TestMetaDataInconsistencyHandler {
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                    override fun calculateWorkload(): Int = 1
                    override fun execute(progressUpdate: (Int) -> Unit): MetaDataInconsistenciesResult = MetaDataInconsistenciesResult(
                        ignoreListResults = listOf(
                            MetaDataDiff(
                                currentEntry = IgnoreListEntry(
                                    link = Link("https://myanimelist.net/anime/28981"),
                                    title = "Ameiro Cocoa",
                                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                                ),
                                newEntry = IgnoreListEntry(
                                    link = Link("https://myanimelist.net/anime/28981"),
                                    title = "Ame-iro Cocoa",
                                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
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
                    eventBus = testEventBus,
                )

                val config = InconsistenciesSearchConfig(
                    checkDeadEntries = false,
                    checkMetaData = true,
                )

                // when
                defaultInconsistenciesHandler.findInconsistencies(config)

                // then
                assertThat(receivedEvents).hasSize(2)
                assertThat(receivedEvents.first()).isInstanceOf(MetaDataInconsistenciesResultEvent::class.java)
                assertThat((receivedEvents.first() as MetaDataInconsistenciesResultEvent).numberOfAffectedEntries).isOne()
                assertThat(receivedEvents.last()).isInstanceOf(InconsistenciesCheckFinishedEvent::class.java)
            }

            @Test
            fun `number of affected entries is the sum of affected watch list entries and ignore list entries`() {
                // given
                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val testMetaDataInconsistencyHandler = object: InconsistencyHandler<MetaDataInconsistenciesResult> by TestMetaDataInconsistencyHandler {
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                    override fun calculateWorkload(): Int = 2
                    override fun execute(progressUpdate: (Int) -> Unit): MetaDataInconsistenciesResult = MetaDataInconsistenciesResult(
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
                        ),
                        ignoreListResults = listOf(
                            MetaDataDiff(
                                currentEntry = IgnoreListEntry(
                                    link = Link("https://myanimelist.net/anime/28981"),
                                    title = "Ameiro Cocoa",
                                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                                ),
                                newEntry = IgnoreListEntry(
                                    link = Link("https://myanimelist.net/anime/28981"),
                                    title = "Ame-iro Cocoa",
                                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
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
                    eventBus = testEventBus,
                )

                val config = InconsistenciesSearchConfig(
                    checkDeadEntries = false,
                    checkMetaData = true,
                )

                // when
                defaultInconsistenciesHandler.findInconsistencies(config)

                // then
                assertThat(receivedEvents).hasSize(2)
                assertThat(receivedEvents.first()).isInstanceOf(MetaDataInconsistenciesResultEvent::class.java)
                assertThat((receivedEvents.first() as MetaDataInconsistenciesResultEvent).numberOfAffectedEntries).isEqualTo(2)
                assertThat(receivedEvents.last()).isInstanceOf(InconsistenciesCheckFinishedEvent::class.java)
            }
        }

        @Nested
        inner class FixMetaDataInconsistenciesTests {

            @Test
            fun `do nothing if there are no findings`() {
                // given
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                }

                val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                    state = TestState,
                    cache = TestAnimeCache,
                    commandHistory = TestCommandHistory,
                    inconsistencyHandlers = listOf(MetaDataInconsistencyHandler()),
                    eventBus = testEventBus,
                )

                // when
                defaultInconsistenciesHandler.fixMetaDataInconsistencies()
            }

            @Test
            fun `replace current watch list entry with the new instance`() {
                // given
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                    override fun post(event: Event) {}
                }

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
                    override fun execute(progressUpdate: (Int) -> Unit): MetaDataInconsistenciesResult = MetaDataInconsistenciesResult(
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
                    eventBus = testEventBus,
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

            @Test
            fun `replace current ignore list entry with the new instance`() {
                // given
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                    override fun post(event: Event) {}
                }

                val metaDataDiff = MetaDataDiff(
                    currentEntry = IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/28981"),
                        title = "Ameiro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                    ),
                    newEntry = IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/28981"),
                        title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                    ),
                )
                val testMetaDataInconsistencyHandler = object: InconsistencyHandler<MetaDataInconsistenciesResult> by TestMetaDataInconsistencyHandler {
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                    override fun calculateWorkload(): Int = 1
                    override fun execute(progressUpdate: (Int) -> Unit): MetaDataInconsistenciesResult = MetaDataInconsistenciesResult(
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
                    eventBus = testEventBus,
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

    @Nested
    inner class DeadEntriesTests {

        @Nested
        inner class NotificationTests {

            @Test
            fun `post event for differing watch list entries`() {
                // given
                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val testDeadEntriesInconsistencyHandler = object: InconsistencyHandler<DeadEntriesInconsistenciesResult> by TestDeadEntriesInconsistencyHandler {
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                    override fun calculateWorkload(): Int = 1
                    override fun execute(progressUpdate: (Int) -> Unit): DeadEntriesInconsistenciesResult = DeadEntriesInconsistenciesResult(
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
                    eventBus = testEventBus,
                )

                val config = InconsistenciesSearchConfig(
                    checkDeadEntries = true,
                    checkMetaData = false,
                )

                // when
                defaultInconsistenciesHandler.findInconsistencies(config)

                // then
                assertThat(receivedEvents).hasSize(2)
                assertThat(receivedEvents.first()).isInstanceOf(DeadEntriesInconsistenciesResultEvent::class.java)
                assertThat((receivedEvents.first() as DeadEntriesInconsistenciesResultEvent).numberOfAffectedEntries).isOne()
                assertThat(receivedEvents.last()).isInstanceOf(InconsistenciesCheckFinishedEvent::class.java)
            }

            @Test
            fun `post event for differing ignore list entries`() {
                // given
                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val testDeadEntriesInconsistencyHandler = object: InconsistencyHandler<DeadEntriesInconsistenciesResult> by TestDeadEntriesInconsistencyHandler {
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                    override fun calculateWorkload(): Int = 1
                    override fun execute(progressUpdate: (Int) -> Unit): DeadEntriesInconsistenciesResult = DeadEntriesInconsistenciesResult(
                        ignoreListResults = listOf(
                            IgnoreListEntry(
                                link = Link("https://myanimelist.net/anime/28981"),
                                title = "Ame-iro Cocoa",
                                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                            ),
                        )
                    )
                }

                val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                    state = TestState,
                    cache = TestAnimeCache,
                    commandHistory = TestCommandHistory,
                    inconsistencyHandlers = listOf(testDeadEntriesInconsistencyHandler),
                    eventBus = testEventBus
                )

                val config = InconsistenciesSearchConfig(
                    checkDeadEntries = true,
                    checkMetaData = false,
                )

                // when
                defaultInconsistenciesHandler.findInconsistencies(config)

                // then
                assertThat(receivedEvents).hasSize(2)
                assertThat(receivedEvents.first()).isInstanceOf(DeadEntriesInconsistenciesResultEvent::class.java)
                assertThat((receivedEvents.first() as DeadEntriesInconsistenciesResultEvent).numberOfAffectedEntries).isOne()
                assertThat(receivedEvents.last()).isInstanceOf(InconsistenciesCheckFinishedEvent::class.java)
            }

            @Test
            fun `number of affected entries is the sum of affected watch list entries and ignore list entries`() {
                // given
                val receivedEvents = mutableListOf<Event>()
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                    override fun post(event: Event) {
                        receivedEvents.add(event)
                    }
                }

                val testDeadEntriesInconsistencyHandler = object: InconsistencyHandler<DeadEntriesInconsistenciesResult> by TestDeadEntriesInconsistencyHandler {
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                    override fun calculateWorkload(): Int = 2
                    override fun execute(progressUpdate: (Int) -> Unit): DeadEntriesInconsistenciesResult = DeadEntriesInconsistenciesResult(
                        watchListResults = listOf(
                            WatchListEntry(
                                link = Link("https://myanimelist.net/anime/5114"),
                                title = "Fullmetal Alchemist: Brotherhood",
                                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                            ),
                        ),
                        ignoreListResults = listOf(
                            IgnoreListEntry(
                                link = Link("https://myanimelist.net/anime/28981"),
                                title = "Ame-iro Cocoa",
                                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                            ),
                        )
                    )
                }

                val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                    state = TestState,
                    cache = TestAnimeCache,
                    commandHistory = TestCommandHistory,
                    inconsistencyHandlers = listOf(testDeadEntriesInconsistencyHandler),
                    eventBus = testEventBus
                )

                val config = InconsistenciesSearchConfig(
                    checkDeadEntries = true,
                    checkMetaData = false,
                )

                // when
                defaultInconsistenciesHandler.findInconsistencies(config)

                // then
                assertThat(receivedEvents).hasSize(2)
                assertThat(receivedEvents.first()).isInstanceOf(DeadEntriesInconsistenciesResultEvent::class.java)
                assertThat((receivedEvents.first() as DeadEntriesInconsistenciesResultEvent).numberOfAffectedEntries).isEqualTo(2)
                assertThat(receivedEvents.last()).isInstanceOf(InconsistenciesCheckFinishedEvent::class.java)
            }
        }

        @Nested
        inner class FixDeadEntryInconsistenciesTests {

            @Test
            fun `do nothing if there are no findings`() {
                // given
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                }

                val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                    state = TestState,
                    cache = TestAnimeCache,
                    commandHistory = TestCommandHistory,
                    inconsistencyHandlers = listOf(DeadEntriesInconsistencyHandler()),
                    eventBus = testEventBus,
                )

                // when
                defaultInconsistenciesHandler.fixDeadEntryInconsistencies()
            }

            @Test
            fun `remove entry from watch list`() {
                // given
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                    override fun post(event: Event) {}
                }

                val watchListEntry = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                )
                val testDeadEntriesInconsistencyHandler = object: InconsistencyHandler<DeadEntriesInconsistenciesResult> by TestDeadEntriesInconsistencyHandler {
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                    override fun calculateWorkload(): Int = 1
                    override fun execute(progressUpdate: (Int) -> Unit): DeadEntriesInconsistenciesResult = DeadEntriesInconsistenciesResult(
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
                    eventBus = testEventBus,
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

            @Test
            fun `remove entry from ignore list`() {
                // given
                val testEventBus = object: EventBus by TestEventBus {
                    override fun subscribe(subscriber: Any) {}
                    override fun post(event: Event) {}
                }

                val ignoreListEntry = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/28981"),
                    title = "Ame-iro Cocoa",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
                )
                val testDeadEntriesInconsistencyHandler = object: InconsistencyHandler<DeadEntriesInconsistenciesResult> by TestDeadEntriesInconsistencyHandler {
                    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = true
                    override fun calculateWorkload(): Int = 1
                    override fun execute(progressUpdate: (Int) -> Unit): DeadEntriesInconsistenciesResult = DeadEntriesInconsistenciesResult(
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
                    eventBus = testEventBus,
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