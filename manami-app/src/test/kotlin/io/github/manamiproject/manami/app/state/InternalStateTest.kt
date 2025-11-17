package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.events.AnimeListState
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.IgnoreListState
import io.github.manamiproject.manami.app.events.WatchListState
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import io.github.manamiproject.modb.core.anime.AnimeType.SPECIAL
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.test.BeforeTest

internal class InternalStateTest {

    @BeforeTest
    @AfterEach
    fun after() {
        InternalState.closeFile()
        InternalState.clear()
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class SetOpenedFileTests {

        @Test
        fun `throws exception if path is not a regular file`() {
            tempDirectory {
                // when
                val result = assertThrows<IllegalStateException> {
                    InternalState.setOpenedFile(tempDir)
                }

                // then
                assertThat(result).hasMessage("Path is not a regular file")
            }
        }

        @Test
        fun `successfully set opened file`() {
            tempDirectory {
                val testFile = tempDir.resolve("test.xml").createFile()

                // when
                InternalState.setOpenedFile(testFile)

                // then
                assertThat(InternalState.openedFile()).isEqualTo(CurrentFile(testFile))
            }
        }
    }

    @Nested
    inner class OpenedFileTests {

        @Test
        fun `returns the opened file`() {
            tempDirectory {
                val testFile = tempDir.resolve("test.xml").createFile()
                InternalState.setOpenedFile(testFile)

                // when
                val result = InternalState.openedFile()

                // then
                assertThat(result).isEqualTo(CurrentFile(testFile))
            }
        }
    }

    @Nested
    inner class CloseFileTests {

        @Test
        fun `successfully set close file`() {
            tempDirectory {
                val testFile = tempDir.resolve("test.xml").createFile()
                InternalState.setOpenedFile(testFile)

                // when
                InternalState.closeFile()

                // then
                assertThat(InternalState.openedFile()).isEqualTo(NoFile)
            }
        }
    }

    @Nested
    inner class AddAllAnimeListEntriesTests {

        @Test
        fun `adds entries without duplicates`() {
            runBlocking {
                // given
                val receivedAnimeListStateEvents = mutableListOf<AnimeListState>()
                val animeListStateEventCollector = launch { CoroutinesFlowEventBus.animeListState.collect { event -> receivedAnimeListStateEvents.add(event) } }

                val receivedWatchListStateEvents = mutableListOf<WatchListState>()
                val watchListStateEventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedWatchListStateEvents.add(event) } }

                val receivedIgnoreListStateEvents = mutableListOf<IgnoreListState>()
                val ignoreListStateEventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedIgnoreListStateEvents.add(event) } }
                delay(100)

                val entry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/57"),
                    title = "Beck",
                    episodes = 26,
                    type = TV,
                    location = Path("some/relative/path/beck"),
                )

                // when
                InternalState.addAllAnimeListEntries(
                    setOf(
                        entry,
                        entry.copy()
                    )
                )

                // then
                delay(100)
                animeListStateEventCollector.cancelAndJoin()
                watchListStateEventCollector.cancelAndJoin()
                ignoreListStateEventCollector.cancelAndJoin()
                assertThat(receivedAnimeListStateEvents).hasSize(2) // initial, update
                assertThat(receivedWatchListStateEvents).hasSize(2) // initial, update
                assertThat(receivedIgnoreListStateEvents).hasSize(2) // initial, update
                assertThat(InternalState.animeList()).containsExactly(entry)
            }
        }

        @Test
        fun `adding an entry removed it from the watchlist`() {
            runBlocking {
                // given
                val receivedAnimeListStateEvents = mutableListOf<AnimeListState>()
                val animeListStateEventCollector = launch { CoroutinesFlowEventBus.animeListState.collect { event -> receivedAnimeListStateEvents.add(event) } }

                val receivedWatchListStateEvents = mutableListOf<WatchListState>()
                val watchListStateEventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedWatchListStateEvents.add(event) } }

                val receivedIgnoreListStateEvents = mutableListOf<IgnoreListState>()
                val ignoreListStateEventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedIgnoreListStateEvents.add(event) } }
                delay(100)

                val entry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/57"),
                    title = "Beck",
                    episodes = 26,
                    type = TV,
                    location = Path("some/relative/path/beck"),
                )
                val watchListEntry = WatchListEntry(
                    link = entry.link.asLink(),
                    title = entry.title,
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg")
                )

                InternalState.addAllWatchListEntries(setOf(watchListEntry))

                // when
                InternalState.addAllAnimeListEntries(setOf(entry))

                // then
                delay(100)
                animeListStateEventCollector.cancelAndJoin()
                watchListStateEventCollector.cancelAndJoin()
                ignoreListStateEventCollector.cancelAndJoin()
                assertThat(receivedAnimeListStateEvents).hasSize(2) // initial, update
                assertThat(receivedWatchListStateEvents).hasSize(2) // initial, update
                assertThat(receivedIgnoreListStateEvents).hasSize(2) // initial, update
                assertThat(InternalState.animeList()).containsExactly(entry)
                assertThat(InternalState.watchList()).isEmpty()
            }
        }

        @Test
        fun `adding an entry removed it from the ignorelist`() {
            runBlocking {
                // given
                val receivedAnimeListStateEvents = mutableListOf<AnimeListState>()
                val animeListStateEventCollector = launch { CoroutinesFlowEventBus.animeListState.collect { event -> receivedAnimeListStateEvents.add(event) } }

                val receivedWatchListStateEvents = mutableListOf<WatchListState>()
                val watchListStateEventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedWatchListStateEvents.add(event) } }

                val receivedIgnoreListStateEvents = mutableListOf<IgnoreListState>()
                val ignoreListStateEventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedIgnoreListStateEvents.add(event) } }
                delay(100)

                val entry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/57"),
                    title = "Beck",
                    episodes = 26,
                    type = TV,
                    location = Path("some/relative/path/beck"),
                )
                val ignoreListEntry = IgnoreListEntry(
                    link = entry.link.asLink(),
                    title = entry.title,
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                )

                InternalState.addAllIgnoreListEntries(setOf(ignoreListEntry))

                // when
                InternalState.addAllAnimeListEntries(setOf(entry))

                // then
                delay(100)
                animeListStateEventCollector.cancelAndJoin()
                watchListStateEventCollector.cancelAndJoin()
                ignoreListStateEventCollector.cancelAndJoin()
                assertThat(receivedAnimeListStateEvents).hasSize(2) // initial, update
                assertThat(receivedWatchListStateEvents).hasSize(2) // initial, update
                assertThat(receivedIgnoreListStateEvents).hasSize(2) // initial, update
                assertThat(InternalState.animeList()).containsExactly(entry)
                assertThat(InternalState.ignoreList()).isEmpty()
            }
        }
    }

    @Nested
    inner class RemoveAnimeListEntryTests {

        @Test
        fun `removes a specific entry`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<AnimeListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.animeListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

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
                val entry3 = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/12079"),
                    title = "Black★Rock Shooter",
                    episodes = 1,
                    type = SPECIAL,
                    location = Path("some/relative/path/black_rock_shooter"),
                )

                InternalState.addAllAnimeListEntries(
                    setOf(
                        entry1,
                        entry2,
                        entry3,
                    )
                )

                // when
                InternalState.removeAnimeListEntry(entry2)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(2) // initial, update
                assertThat(InternalState.animeList()).containsExactly(entry1, entry3)
            }
        }
    }

    @Nested
    inner class AddAllWatchListEntriesTests {

        @Test
        fun `adds entries without duplicates`() {
            runBlocking {
                // given
                val receivedWatchListStateEvents = mutableListOf<WatchListState>()
                val watchListStateEventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedWatchListStateEvents.add(event) } }

                val receivedIgnoreListStateEvents = mutableListOf<IgnoreListState>()
                val ignoreListStateEventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedIgnoreListStateEvents.add(event) } }
                delay(100)

                val entry = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                )

                // when
                InternalState.addAllWatchListEntries(
                    setOf(
                        entry,
                        entry.copy()
                    )
                )

                // then
                delay(100)
                watchListStateEventCollector.cancelAndJoin()
                ignoreListStateEventCollector.cancelAndJoin()
                assertThat(receivedWatchListStateEvents).hasSize(2) // initial, update
                assertThat(receivedIgnoreListStateEvents).hasSize(1) // initial, update
                assertThat(InternalState.watchList()).containsExactly(entry)
            }
        }

        @Test
        fun `adding an entry to the watchlist will remove it from the ignorelist`() {
            runBlocking {
                // given
                val receivedWatchListStateEvents = mutableListOf<WatchListState>()
                val watchListStateEventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedWatchListStateEvents.add(event) } }

                val receivedIgnoreListStateEvents = mutableListOf<IgnoreListState>()
                val ignoreListStateEventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedIgnoreListStateEvents.add(event) } }
                delay(100)

                val entry = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                )
                InternalState.addAllIgnoreListEntries(setOf(IgnoreListEntry(entry)))

                // when
                InternalState.addAllWatchListEntries(setOf(entry))

                // then
                delay(100)
                watchListStateEventCollector.cancelAndJoin()
                ignoreListStateEventCollector.cancelAndJoin()
                assertThat(receivedWatchListStateEvents).hasSize(2) // initial, update
                assertThat(receivedIgnoreListStateEvents).hasSize(2) // initial, update
                assertThat(InternalState.watchList()).containsExactly(entry)
                assertThat(InternalState.ignoreList()).isEmpty()
            }
        }
    }

    @Nested
    inner class RemoveWatchListEntryTests {

        @Test
        fun `removes a specific entry`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<WatchListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val entry1 = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                )
                val entry2 = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/37989"),
                    title = "Golden Kamuy 2nd Season",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                )
                val entry3 = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/40059"),
                    title = "Golden Kamuy 3rd Season",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
                )

                InternalState.addAllWatchListEntries(
                    setOf(
                        entry1,
                        entry2,
                        entry3,
                    )
                )

                // when
                InternalState.removeWatchListEntry(entry2)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(2) // initial, update
                assertThat(InternalState.watchList()).containsExactly(entry1, entry3)
            }
        }
    }

    @Nested
    inner class AddAllIgnoreListEntriesTests {

        @Test
        fun `adds entries without duplicates`() {
            runBlocking {
                // given
                val receivedWatchListStateEvents = mutableListOf<WatchListState>()
                val watchListStateEventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedWatchListStateEvents.add(event) } }

                val receivedIgnoreListStateEvents = mutableListOf<IgnoreListState>()
                val ignoreListStateEventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedIgnoreListStateEvents.add(event) } }
                delay(100)

                val entry = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/28981"),
                    title = "Ame-iro Cocoa",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                )

                // when
                InternalState.addAllIgnoreListEntries(
                    setOf(
                        entry,
                        entry.copy()
                    )
                )

                // then
                delay(100)
                watchListStateEventCollector.cancelAndJoin()
                ignoreListStateEventCollector.cancelAndJoin()
                assertThat(receivedWatchListStateEvents).hasSize(1) // initial, update
                assertThat(receivedIgnoreListStateEvents).hasSize(2) // initial, update
                assertThat(InternalState.ignoreList()).containsExactly(entry)
            }
        }

        @Test
        fun `adding an entry to the ignorelist will remove it from the watchlist`() {
            runBlocking {
                // given
                val receivedWatchListStateEvents = mutableListOf<WatchListState>()
                val watchListStateEventCollector = launch { CoroutinesFlowEventBus.watchListState.collect { event -> receivedWatchListStateEvents.add(event) } }

                val receivedIgnoreListStateEvents = mutableListOf<IgnoreListState>()
                val ignoreListStateEventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedIgnoreListStateEvents.add(event) } }
                delay(100)

                val entry = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                )
                InternalState.addAllWatchListEntries(setOf(WatchListEntry(entry)))

                // when
                InternalState.addAllIgnoreListEntries(setOf(entry))

                // then
                delay(100)
                watchListStateEventCollector.cancelAndJoin()
                ignoreListStateEventCollector.cancelAndJoin()
                assertThat(receivedWatchListStateEvents).hasSize(2) // initial, update
                assertThat(receivedIgnoreListStateEvents).hasSize(2) // initial, update
                assertThat(InternalState.ignoreList()).containsExactly(entry)
                assertThat(InternalState.watchList()).isEmpty()
            }
        }
    }

    @Nested
    inner class RemoveIgnoreListEntryTests {

        @Test
        fun `removes a specific entry`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf< IgnoreListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.ignoreListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                val entry1 = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                )
                val entry2 = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/37989"),
                    title = "Golden Kamuy 2nd Season",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
                )
                val entry3 = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/40059"),
                    title = "Golden Kamuy 3rd Season",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
                )

                InternalState.addAllIgnoreListEntries(
                    setOf(
                        entry1,
                        entry2,
                        entry3,
                    )
                )

                // when
                InternalState.removeIgnoreListEntry(entry2)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(2) // initial, update
                assertThat(InternalState.ignoreList()).containsExactly(entry1, entry3)
            }
        }
    }

    @Nested
    inner class CreateSnapshotTests {

        @Test
        fun `create a snapshot containing all three lists`() {
            // given
            val animeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                episodes = 26,
                type = TV,
                location = Path("some/relative/path/beck"),
            )
            InternalState.addAllAnimeListEntries(setOf(animeListEntry))

            val watchListEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            InternalState.addAllWatchListEntries(setOf(watchListEntry))

            val ignoreListEntry = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/28981"),
                title = "Ame-iro Cocoa",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
            )
            InternalState.addAllIgnoreListEntries(setOf(ignoreListEntry))

            // when
            val result = InternalState.createSnapshot()

            // then
            assertThat(result.animeList()).containsExactly(animeListEntry)
            assertThat(result.watchList()).containsExactly(watchListEntry)
            assertThat(result.ignoreList()).containsExactly(ignoreListEntry)
        }
    }

    @Nested
    inner class RestoreSnapshotTests {

        @Test
        fun `remove whatever is current in state and restore the entries from the given snapshot`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<AnimeListState>()
                val eventCollector = launch { CoroutinesFlowEventBus.animeListState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                InternalState.addAllAnimeListEntries(
                    setOf(
                        AnimeListEntry(
                            title = "H2O: Footprints in the Sand",
                            episodes = 4,
                            type = SPECIAL,
                            location = Path("some/relative/path/h2o_-_footprints_in_the_sand_special"),
                        )
                    )
                )
                InternalState.addAllWatchListEntries(
                    setOf(
                        WatchListEntry(
                            link = Link("https://myanimelist.net/anime/40059"),
                            title = "Golden Kamuy 3rd Season",
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
                        )
                    )
                )
                InternalState.addAllIgnoreListEntries(
                    setOf(
                        IgnoreListEntry(
                            link = Link("https://myanimelist.net/anime/31139"),
                            title = "Ame-iro Cocoa: Rainy Color e Youkoso!",
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/76340t.jpg"),
                        )
                    )
                )

                val animeListEntry = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/57"),
                    title = "Beck",
                    episodes = 26,
                    type = TV,
                    location = Path("some/relative/path/beck"),
                )

                val watchListEntry = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/5114"),
                    title = "Fullmetal Alchemist: Brotherhood",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                )

                val ignoreListEntry = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/28981"),
                    title = "Ame-iro Cocoa",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                )

                val snapshot = StateSnapshot(
                    animeList = listOf(animeListEntry),
                    watchList = setOf(watchListEntry),
                    ignoreList = setOf(ignoreListEntry),
                )

                // when
                InternalState.restore(snapshot)

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(2) // initial, update
                assertThat(InternalState.animeList()).containsExactly(animeListEntry)
                assertThat(InternalState.watchList()).containsExactly(watchListEntry)
                assertThat(InternalState.ignoreList()).containsExactly(ignoreListEntry)
            }
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `animeList is cleared`() {
            // given
            InternalState.addAllAnimeListEntries(
                setOf(
                    AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/57"),
                        title = "Beck",
                        episodes = 26,
                        type = TV,
                        location = Path("some/relative/path/beck"),
                    )
                )
            )

            // when
            InternalState.clear()

            // then
            assertThat(InternalState.animeList()).isEmpty()
        }

        @Test
        fun `watchList is cleared`() {
            // given
            InternalState.addAllWatchListEntries(
                setOf(
                    WatchListEntry(
                        link = Link("https://myanimelist.net/anime/5114"),
                        title = "Fullmetal Alchemist: Brotherhood",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                    )
                )
            )

            // when
            InternalState.clear()

            // then
            assertThat(InternalState.animeList()).isEmpty()
        }

        @Test
        fun `ignoreList is cleared`() {
            // given
            InternalState.addAllIgnoreListEntries(
                setOf(
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/28981"),
                        title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
                    )
                )
            )

            // when
            InternalState.clear()

            // then
            assertThat(InternalState.animeList()).isEmpty()
        }
    }
}