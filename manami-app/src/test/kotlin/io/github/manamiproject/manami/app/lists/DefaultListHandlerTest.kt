package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.AddIgnoreListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.AddWatchListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.*
import io.github.manamiproject.manami.app.state.TestSnapshot
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.commands.ReversibleCommand
import io.github.manamiproject.manami.app.commands.TestCommandHistory
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.TestEventBus
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import io.github.manamiproject.modb.core.collections.SortedList
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.ONGOING
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Type.SPECIAL
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.net.URI
import kotlin.io.path.Path

internal class DefaultListHandlerTest {

    @Test
    fun `return the list of anime list entries from state`() {
        // given
        val entry1 = AnimeListEntry(
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
        val result = defaultListHandler.animeList()

        // then
        assertThat(result).containsExactlyInAnyOrder(entry1, entry2)
    }

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
        val testCache = object: AnimeCache by TestAnimeCache { }
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
    inner class AddAnimeListEntryTests {

        @Test
        fun `add anime list entries and fire command containing the progress`() {
            // given
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
                override fun animeListEntrtyExists(anime: AnimeListEntry): Boolean = false
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
                eventBus = TestEventBus,
            )

            // when
            defaultListHandler.addAnimeListEntry(entry)

            // then
            assertThat(savedEntries).containsExactly(entry)
        }
    }

    @Nested
    inner class AddWatchListEntryTests {

        @Test
        fun `add watch list entries and fire command containing the progress`() {
            // given
            val entry1 = Anime(
                sources = SortedList(URI("https://myanimelist.net/anime/37989")),
                _title = "Golden Kamuy 2nd Season",
                type = TV,
                episodes = 12,
                status = FINISHED,
                animeSeason = AnimeSeason(),
                picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                duration = Duration(23, MINUTES),
            )
            val entry2 = Anime(
                sources = SortedList(URI("https://myanimelist.net/anime/40059")),
                _title = "Golden Kamuy 3rd Season",
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
                sources = SortedList(URI("https://myanimelist.net/anime/37989")),
                _title = "Golden Kamuy 2nd Season",
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
                override fun fetch(key: URI): CacheEntry<Anime> {
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
                sources = SortedList(URI("https://myanimelist.net/anime/37989")),
                _title = "Golden Kamuy 2nd Season",
                type = TV,
                episodes = 12,
                status = FINISHED,
                animeSeason = AnimeSeason(),
                picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                duration = Duration(23, MINUTES),
            )
            val entry2 = Anime(
                sources = SortedList(URI("https://myanimelist.net/anime/40059")),
                _title = "Golden Kamuy 3rd Season",
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
                sources = SortedList(URI("https://myanimelist.net/anime/37989")),
                _title = "Golden Kamuy 2nd Season",
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
                override fun fetch(key: URI): CacheEntry<Anime> {
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

    @Test
    fun `remove anime list entry`() {
        // given
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
            override fun animeListEntrtyExists(anime: AnimeListEntry): Boolean = true
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
            eventBus = TestEventBus,
        )

        // when
        defaultListHandler.removeAnimeListEntry(expectedEntry)

        // then
        assertThat(resultingEntry).isEqualTo(expectedEntry)
    }

    @Test
    fun `remove watch list entry`() {
        // given
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
            eventBus = TestEventBus,
        )

        // when
        defaultListHandler.removeWatchListEntry(expectedEntry)

        // then
        assertThat(resultingEntry).isEqualTo(expectedEntry)
    }

    @Test
    fun `remove ignore list entry`() {
        // given
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
            eventBus = TestEventBus,
        )

        // when
        defaultListHandler.removeIgnoreListEntry(expectedEntry)

        // then
        assertThat(resultingEntry).isEqualTo(expectedEntry)
    }
}