package io.github.manamiproject.manami.app.state.commands

import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.Link
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestSnapshot
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.state.commands.history.CommandHistory
import io.github.manamiproject.manami.app.state.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.models.Anime.Type.Movie
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.net.URL

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
            override fun execute() { }
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
        var isCommandExecuted = false
        val testCommand = object: Command {
            override fun execute() { isCommandExecuted = true }
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
        reversibleCommand.execute()

        // then
        assertThat(isCommandExecuted).isTrue()
        assertThat(isSnapshotCreated).isTrue()
        assertThat(isAddedToHistory).isTrue()
    }

    @Test
    fun `restores the snapshot if undo is called`() {
        // given
        val initialAnimeList = setOf(
                AnimeListEntry(
                    link = Link(URI("https://myanimelist.net/anime/1535")),
                    title = "Death Note",
                    episodes = 37,
                    type = TV,
                    location = URI("."),
                )
        )
        val initialWatchList = setOf(URL("https://myanimelist.net/anime/5114"))
        val initialIgnoreList = setOf(URL("https://myanimelist.net/anime/28977"))

        InternalState.addAllAnimeListEntries(initialAnimeList)
        InternalState.addAllWatchListEntries(initialWatchList)
        InternalState.addAllIgnoreListEntries(initialIgnoreList)

        val testCommand = object: Command {
            override fun execute() {
                InternalState.addAllAnimeListEntries(
                        setOf(
                                AnimeListEntry(
                                        link = Link(URI("https://myanimelist.net/anime/28851")),
                                        title = "Koe no Katachi",
                                        episodes = 1,
                                        type = Movie,
                                        location = URI("."),
                                )
                        )
                )
                InternalState.addAllWatchListEntries(setOf(URL("https://myanimelist.net/anime/35180")))
                InternalState.addAllIgnoreListEntries(setOf(URL("https://myanimelist.net/anime/11061")))
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