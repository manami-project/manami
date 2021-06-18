package io.github.manamiproject.manami.app.state.commands

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestSnapshot
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.state.commands.history.CommandHistory
import io.github.manamiproject.manami.app.state.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.models.Anime.Type.SPECIAL
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

internal class GenericReversibleCommandTest {

    @BeforeEach
    fun beforeEach() {
        InternalState.clear()
        DefaultCommandHistory.clear()
    }

    @Test
    fun `throws exception if undo is being called before command has been executed`() {
        // given
        val testCommand = object: Command {
            override fun execute() = true
        }
        val reversibleCommand = GenericReversibleCommand(command = testCommand)

        // when
        val result = assertThrows<IllegalStateException> {
            reversibleCommand.undo()
        }

        // then
        assertThat(result).hasMessage("Cannot undo command which hasn't been executed")
    }

    @Test
    fun `creates a snapshot and adds the command to the history if the ReversibleCommand is being executed`() {
        // given
        val testCommand = object: Command {
            override fun execute() = true
        }

        var isSnapshotCreated = false
        val testState = object: State by TestState {
            override fun createSnapshot(): Snapshot {
                isSnapshotCreated = true
                return TestSnapshot
            }
        }

        var isAddedToHistory = false
        val testCommandHistory = object: CommandHistory by TestCommandHistory {
            override fun push(command: ReversibleCommand) { isAddedToHistory = true }
        }

        val reversibleCommand = GenericReversibleCommand(
                state = testState,
                commandHistory = testCommandHistory,
                command = testCommand,
        )

        // when
        val result = reversibleCommand.execute()

        // then
        assertThat(result).isTrue()
        assertThat(isSnapshotCreated).isTrue()
        assertThat(isAddedToHistory).isTrue()
    }

    @Test
    fun `restores the snapshot if undo is called`() {
        // given
        val initialAnimeList = setOf(
                AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/1535"),
                    title = "Death Note",
                    episodes = 37,
                    type = TV,
                    location = URI("."),
                )
        )
        InternalState.addAllAnimeListEntries(initialAnimeList)

        val initialWatchList = setOf(
            WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
        )
        InternalState.addAllWatchListEntries(initialWatchList)

        val initialIgnoreList = setOf(
            IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/28977"),
                title = "Gintama°",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/3/72078t.jpg"),
            )
        )
        InternalState.addAllIgnoreListEntries(initialIgnoreList)


        val testCommand = object: Command {
            override fun execute(): Boolean {
                InternalState.addAllAnimeListEntries(
                        setOf(
                                AnimeListEntry(
                                        link = Link("https://myanimelist.net/anime/28851"),
                                        title = "Koe no Katachi",
                                        episodes = 1,
                                        type = SPECIAL,
                                        location = URI("."),
                                )
                        )
                )
                InternalState.addAllWatchListEntries(
                    setOf(
                        WatchListEntry(
                            link = Link("https://myanimelist.net/anime/35180"),
                            title = "3-gatsu no Lion 2nd Season",
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/3/88469t.jpg"),
                        )
                    )
                )
                InternalState.addAllIgnoreListEntries(
                    setOf(
                        IgnoreListEntry(
                            link = Link("https://myanimelist.net/anime/11061"),
                            title = "Hunter x Hunter (2011)",
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/33657t.jpg")
                        )
                    )
                )
                return true
            }
        }
        val reversibleCommand = GenericReversibleCommand(command = testCommand)
        reversibleCommand.execute()

        // when
        reversibleCommand.undo()

        // then
        assertThat(InternalState.animeList()).containsExactlyInAnyOrder(*initialAnimeList.toTypedArray())
        assertThat(InternalState.watchList()).containsExactlyInAnyOrder(*initialWatchList.toTypedArray())
        assertThat(InternalState.ignoreList()).containsExactlyInAnyOrder(*initialIgnoreList.toTypedArray())
    }
}