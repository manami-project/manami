package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.cache.TestAnimeCache
import io.github.manamiproject.manami.app.lists.ignorelist.AddIgnoreListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.AddWatchListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestSnapshot
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.state.commands.ReversibleCommand
import io.github.manamiproject.manami.app.state.commands.TestCommandHistory
import io.github.manamiproject.manami.app.state.commands.history.CommandHistory
import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.TestEventBus
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.net.URI

internal class DefaultListHandlerTest {

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

        val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache { }
        val testCommandHistory = object: CommandHistory by TestCommandHistory { }
        val testEventBus = object: EventBus by TestEventBus {
            override fun post(event: Event) { }
        }

        val defaultListHandler = DefaultListHandler(
            state = state,
            commandHistory = testCommandHistory,
            cache = testCache,
            eventBus = testEventBus,
        )

        // when
        val result = defaultListHandler.watchList()

        // then
        assertThat(result).containsExactlyInAnyOrder(entry1, entry2)
    }

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
        val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache { }
        val testCommandHistory = object: CommandHistory by TestCommandHistory { }
        val testEventBus = object: EventBus by TestEventBus {
            override fun post(event: Event) { }
        }

        val defaultListHandler = DefaultListHandler(
            state = state,
            commandHistory = testCommandHistory,
            cache = testCache,
            eventBus = testEventBus,
        )

        // when
        val result = defaultListHandler.ignoreList()

        // then
        assertThat(result).containsExactlyInAnyOrder(entry1, entry2)
    }

    @Nested
    inner class AddWatchListEntryTests {

        @Test
        fun `add watch list entries and fire command containing the progress`() {
            // given
            val entry1 = Anime(
                _title = "Golden Kamuy 2nd Season",
                type = Anime.Type.TV,
                episodes = 12,
                status = Anime.Status.FINISHED,
                animeSeason = AnimeSeason(),
                picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                duration = Duration(23, Duration.TimeUnit.MINUTES)
            ).apply {
                addSources(URI("https://myanimelist.net/anime/37989"))
            }
            val entry2 = Anime(
                _title = "Golden Kamuy 3rd Season",
                type = Anime.Type.TV,
                episodes = 12,
                status = Anime.Status.CURRENTLY,
                animeSeason = AnimeSeason(),
                picture = URI("https://cdn.myanimelist.net/images/anime/1763/108108.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
                duration = Duration(23, Duration.TimeUnit.MINUTES)
            ).apply {
                addSources(URI("https://myanimelist.net/anime/40059"))
            }

            val savedEntries = mutableListOf<WatchListEntry>()
            val state = object: State by TestState {
                override fun createSnapshot(): Snapshot = TestSnapshot
                override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                    savedEntries.addAll(anime)
                }
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
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

            val eventsReceived = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) { eventsReceived.add(event) }
            }

            val defaultListHandler = DefaultListHandler(
                state = state,
                commandHistory = testCommandHistory,
                cache = testCache,
                eventBus = testEventBus,
            )

            // when
            defaultListHandler.addWatchListEntry(setOf(entry1.sources.first(), entry2.sources.first()))

            // then
            sleep(1000)
            assertThat(savedEntries).containsExactlyInAnyOrder(WatchListEntry(entry1), WatchListEntry(entry2))
            assertThat(eventsReceived).hasSize(2)
            assertThat(eventsReceived.first()).isEqualTo(AddWatchListStatusUpdateEvent(1, 2))
            assertThat(eventsReceived.last()).isEqualTo(AddWatchListStatusUpdateEvent(2, 2))
        }

        @Test
        fun `don't do anything with entries for which the cache does not return anything, but the update events must indicate that we actually processed it`() {
            // given
            val entry = Anime(
                _title = "Golden Kamuy 2nd Season",
                type = Anime.Type.TV,
                episodes = 12,
                status = Anime.Status.FINISHED,
                animeSeason = AnimeSeason(),
                picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                duration = Duration(23, Duration.TimeUnit.MINUTES)
            ).apply {
                addSources(URI("https://myanimelist.net/anime/37989"))
            }
            val deadEntry = URI("https://myanimelist.net/anime/1001")

            val savedEntries = mutableListOf<WatchListEntry>()
            val state = object: State by TestState {
                override fun createSnapshot(): Snapshot = TestSnapshot
                override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                    savedEntries.addAll(anime)
                }
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
                    return when(key) {
                        entry.sources.first() -> PresentValue(entry)
                        deadEntry -> Empty()
                        else -> shouldNotBeInvoked()
                    }
                }
            }

            val testCommandHistory = object: CommandHistory by TestCommandHistory {
                override fun push(command: ReversibleCommand) { }
            }

            val eventsReceived = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) { eventsReceived.add(event) }
            }

            val defaultListHandler = DefaultListHandler(
                state = state,
                commandHistory = testCommandHistory,
                cache = testCache,
                eventBus = testEventBus,
            )

            // when
            defaultListHandler.addWatchListEntry(setOf(deadEntry, entry.sources.first()))

            // then
            sleep(1000)
            assertThat(savedEntries).containsExactlyInAnyOrder(WatchListEntry(entry))
            assertThat(eventsReceived).hasSize(2)
            assertThat(eventsReceived.first()).isEqualTo(AddWatchListStatusUpdateEvent(1, 2))
            assertThat(eventsReceived.last()).isEqualTo(AddWatchListStatusUpdateEvent(2, 2))
        }
    }

    @Nested
    inner class AddIgnoreListEntryTests {

        @Test
        fun `add ignore list entries and fire command containing the progress`() {
            // given
            val entry1 = Anime(
                _title = "Golden Kamuy 2nd Season",
                type = Anime.Type.TV,
                episodes = 12,
                status = Anime.Status.FINISHED,
                animeSeason = AnimeSeason(),
                picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                duration = Duration(23, Duration.TimeUnit.MINUTES)
            ).apply {
                addSources(URI("https://myanimelist.net/anime/37989"))
            }
            val entry2 = Anime(
                _title = "Golden Kamuy 3rd Season",
                type = Anime.Type.TV,
                episodes = 12,
                status = Anime.Status.CURRENTLY,
                animeSeason = AnimeSeason(),
                picture = URI("https://cdn.myanimelist.net/images/anime/1763/108108.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
                duration = Duration(23, Duration.TimeUnit.MINUTES)
            ).apply {
                addSources(URI("https://myanimelist.net/anime/40059"))
            }

            val savedEntries = mutableListOf<IgnoreListEntry>()
            val state = object: State by TestState {
                override fun createSnapshot(): Snapshot = TestSnapshot
                override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                    savedEntries.addAll(anime)
                }
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
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

            val eventsReceived = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) { eventsReceived.add(event) }
            }

            val defaultListHandler = DefaultListHandler(
                state = state,
                commandHistory = testCommandHistory,
                cache = testCache,
                eventBus = testEventBus,
            )

            // when
            defaultListHandler.addIgnoreListEntry(setOf(entry1.sources.first(), entry2.sources.first()))

            // then
            sleep(1000)
            assertThat(savedEntries).containsExactlyInAnyOrder(IgnoreListEntry(entry1), IgnoreListEntry(entry2))
            assertThat(eventsReceived).hasSize(2)
            assertThat(eventsReceived.first()).isEqualTo(AddIgnoreListStatusUpdateEvent(1, 2))
            assertThat(eventsReceived.last()).isEqualTo(AddIgnoreListStatusUpdateEvent(2, 2))
        }

        @Test
        fun `don't do anything with entries for which the cache does not return anything, but the update events must indicate that we actually processed it`() {
            // given
            val entry = Anime(
                _title = "Golden Kamuy 2nd Season",
                type = Anime.Type.TV,
                episodes = 12,
                status = Anime.Status.FINISHED,
                animeSeason = AnimeSeason(),
                picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                duration = Duration(23, Duration.TimeUnit.MINUTES)
            ).apply {
                addSources(URI("https://myanimelist.net/anime/37989"))
            }
            val deadEntry = URI("https://myanimelist.net/anime/1001")

            val savedEntries = mutableListOf<IgnoreListEntry>()
            val state = object: State by TestState {
                override fun createSnapshot(): Snapshot = TestSnapshot
                override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                    savedEntries.addAll(anime)
                }
            }

            val testCache = object: Cache<URI, CacheEntry<Anime>> by TestAnimeCache {
                override fun fetch(key: URI): CacheEntry<Anime> {
                    return when(key) {
                        entry.sources.first() -> PresentValue(entry)
                        deadEntry -> Empty()
                        else -> shouldNotBeInvoked()
                    }
                }
            }

            val testCommandHistory = object: CommandHistory by TestCommandHistory {
                override fun push(command: ReversibleCommand) { }
            }

            val eventsReceived = mutableListOf<Event>()
            val testEventBus = object: EventBus by TestEventBus {
                override fun post(event: Event) { eventsReceived.add(event) }
            }

            val defaultListHandler = DefaultListHandler(
                state = state,
                commandHistory = testCommandHistory,
                cache = testCache,
                eventBus = testEventBus,
            )

            // when
            defaultListHandler.addIgnoreListEntry(setOf(deadEntry, entry.sources.first()))

            // then
            sleep(1000)
            assertThat(savedEntries).containsExactlyInAnyOrder(IgnoreListEntry(entry))
            assertThat(eventsReceived).hasSize(2)
            assertThat(eventsReceived.first()).isEqualTo(AddIgnoreListStatusUpdateEvent(1, 2))
            assertThat(eventsReceived.last()).isEqualTo(AddIgnoreListStatusUpdateEvent(2, 2))
        }
    }
}