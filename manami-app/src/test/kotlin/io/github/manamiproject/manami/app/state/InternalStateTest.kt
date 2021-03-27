package io.github.manamiproject.manami.app.state

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import io.github.manamiproject.modb.core.extensions.createFile
import io.github.manamiproject.modb.core.models.Anime.Type.Special
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

internal class InternalStateTest {

    @AfterEach
    fun after() {
        InternalState.closeFile()
        InternalState.clear()
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
            // given
            val entry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                episodes = 26,
                type = TV,
                location = URI("some/relative/path/beck"),
            )

            // when
            InternalState.addAllAnimeListEntries(
                setOf(
                    entry,
                    entry.copy()
                )
            )

            // then
            assertThat(InternalState.animeList()).containsExactly(entry)
        }

        @Test
        fun `adding an entry removed it from the watchlist`() {
            // given
            val entry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                episodes = 26,
                type = TV,
                location = URI("some/relative/path/beck"),
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
            assertThat(InternalState.animeList()).containsExactly(entry)
            assertThat(InternalState.watchList()).isEmpty()
        }

        @Test
        fun `adding an entry removed it from the ignorelist`() {
            // given
            val entry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                episodes = 26,
                type = TV,
                location = URI("some/relative/path/beck"),
            )
            val ignoreListEntry = IgnoreListEntry(
                link = entry.link.asLink(),
                title = entry.title,
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg")
            )

            InternalState.addAllIgnoreListEntries(setOf(ignoreListEntry))

            // when
            InternalState.addAllAnimeListEntries(setOf(entry))

            // then
            assertThat(InternalState.animeList()).containsExactly(entry)
            assertThat(InternalState.ignoreList()).isEmpty()
        }
    }

    @Nested
    inner class RemoveAnimeListEntryTests {

        @Test
        fun `removes a specific entry`() {
            // given
            val entry1 = AnimeListEntry(
                title = "H2O: Footprints in the Sand",
                episodes = 4,
                type = Special,
                location = URI("some/relative/path/h2o_-_footprints_in_the_sand_special"),
            )
            val entry2 = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = URI("some/relative/path/beck"),
            )
            val entry3 = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/12079"),
                title = "Black★Rock Shooter",
                episodes = 1,
                type = Special,
                location = URI("some/relative/path/black_rock_shooter"),
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
            assertThat(InternalState.animeList()).containsExactly(entry1, entry3)
        }
    }

    @Nested
    inner class AddAllWatchListEntriesTests {

        @Test
        fun `adds entries without duplicates`() {
            // given
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
            assertThat(InternalState.watchList()).containsExactly(entry)
        }

        @Test
        fun `adding an entry to the watchlist will remove it from the ignorelist`() {
            // given
            val entry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            InternalState.addAllIgnoreListEntries(setOf(IgnoreListEntry(entry)))

            // when
            InternalState.addAllWatchListEntries(setOf(entry))

            // then
            assertThat(InternalState.watchList()).containsExactly(entry)
            assertThat(InternalState.ignoreList()).isEmpty()
        }
    }

    @Nested
    inner class RemoveWatchListEntryTests {

        @Test
        fun `removes a specific entry`() {
            // given
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
            assertThat(InternalState.watchList()).containsExactly(entry1, entry3)
        }
    }

    @Nested
    inner class AddAllIgnoreListEntriesTests {

        @Test
        fun `adds entries without duplicates`() {
            // given
            val entry = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/28981"),
                title = "Ame-iro Cocoa",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
            )

            // when
            InternalState.addAllIgnoreListEntries(
                setOf(
                    entry,
                    entry.copy()
                )
            )

            // then
            assertThat(InternalState.ignoreList()).containsExactly(entry)
        }

        @Test
        fun `adding an entry to the ignorelist will remove it from the watchlist`() {
            // given
            val entry = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            InternalState.addAllWatchListEntries(setOf(WatchListEntry(entry)))

            // when
            InternalState.addAllIgnoreListEntries(setOf(entry))

            // then
            assertThat(InternalState.ignoreList()).containsExactly(entry)
            assertThat(InternalState.watchList()).isEmpty()
        }
    }

    @Nested
    inner class RemoveIgnoreListEntryTests {

        @Test
        fun `removes a specific entry`() {
            // given
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
            assertThat(InternalState.ignoreList()).containsExactly(entry1, entry3)
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
                location = URI("some/relative/path/beck"),
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
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
            )
            InternalState.addAllIgnoreListEntries(setOf(ignoreListEntry))

            // when
            val result = InternalState.createSnapshot()

            // then
            InternalState.clear()
            assertThat(result.animeList()).containsExactly(animeListEntry)
            assertThat(result.watchList()).containsExactly(watchListEntry)
            assertThat(result.ignoreList()).containsExactly(ignoreListEntry)
        }
    }

    @Nested
    inner class RestoreSnapshotTests {

        @Test
        fun `remove whatever is current in state and restore the entries from the given snapshot`() {
            // given
            InternalState.addAllAnimeListEntries(
                setOf(
                    AnimeListEntry(
                        title = "H2O: Footprints in the Sand",
                        episodes = 4,
                        type = Special,
                        location = URI("some/relative/path/h2o_-_footprints_in_the_sand_special"),
                    )
                )
            )
            InternalState.addAllWatchListEntries(
                setOf(
                    WatchListEntry(
                        link = Link("https://myanimelist.net/anime/40059"),
                        title = "Golden Kamuy 3rd Season",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
                    )
                )
            )
            InternalState.addAllIgnoreListEntries(
                setOf(
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/31139"),
                        title = "Ame-iro Cocoa: Rainy Color e Youkoso!",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/76340t.jpg")
                    )
                )
            )

            val animeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                episodes = 26,
                type = TV,
                location = URI("some/relative/path/beck"),
            )

            val watchListEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            val ignoreListEntry = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/28981"),
                title = "Ame-iro Cocoa",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
            )

            val snapshot = StateSnapshot(
                animeList = listOf(animeListEntry),
                watchList = setOf(watchListEntry),
                ignoreList = setOf(ignoreListEntry),
            )

            // when
            InternalState.restore(snapshot)

            // then
            assertThat(InternalState.animeList()).containsExactly(animeListEntry)
            assertThat(InternalState.watchList()).containsExactly(watchListEntry)
            assertThat(InternalState.ignoreList()).containsExactly(ignoreListEntry)
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
                        location = URI("some/relative/path/beck"),
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
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
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