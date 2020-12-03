package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.state.commands.TestCommandHistory
import io.github.manamiproject.manami.app.state.commands.history.CommandHistory
import io.github.manamiproject.manami.app.import.parser.ParsedFile
import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.Link
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.models.Anime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.URL
import java.nio.file.Paths

internal class CmdOpenFileTest {

    @Test
    fun `clears state, clears command history, closes file, adds entries from parsed file to state and stes opened file`() {
        // given
        var hasCloseFileBeenCalled = false
        var hasClearStateBeenCalled = false
        var hasClearHistoryBeenCalled = false
        var openedFile = Paths.get(".")
        val animeListEntries = mutableListOf<AnimeListEntry>()
        val watchListEntries = mutableListOf<URL>()
        val ignoreListEntries = mutableListOf<URL>()

        val testState = object: State by TestState {
            override fun closeFile() { hasCloseFileBeenCalled = true }
            override fun clear() { hasClearStateBeenCalled = true }
            override fun openedFile(file: RegularFile) { openedFile = file }
            override fun addAllAnimeListEntries(anime: Set<AnimeListEntry>) { animeListEntries.addAll(anime) }
            override fun addAllWatchListEntries(anime: Set<URL>) { watchListEntries.addAll(anime) }
            override fun addAllIgnoreListEntries(anime: Set<URL>) { ignoreListEntries.addAll(anime) }
        }

        val testCommandHistory = object: CommandHistory by TestCommandHistory {
            override fun clear() { hasClearHistoryBeenCalled = true }
        }

        val notExistingTestFile = Paths.get(".").resolve("test.xml")

        val parsedFile = ParsedFile(
            animeListEntries = setOf(
                    AnimeListEntry(
                            link = Link(URI("https://myanimelist.net/anime/57")),
                            title = "Beck",
                            episodes = 26,
                            type = Anime.Type.TV,
                            location = "some/relative/path/beck",
                    )
            ),
            watchListEntries = setOf(URL("https://myanimelist.net/anime/40059")),
            ignoreListEntries = setOf(URL("https://myanimelist.net/anime/31139")),
        )

        val command = CmdOpenFile(
                state = testState,
                commandHistory = testCommandHistory,
                file = notExistingTestFile,
                parsedFile = parsedFile,
        )

        // when
        command.execute()

        // then
        assertThat(hasCloseFileBeenCalled).isTrue()
        assertThat(hasClearStateBeenCalled).isTrue()
        assertThat(hasClearHistoryBeenCalled).isTrue()
        assertThat(openedFile).isEqualTo(notExistingTestFile)
        assertThat(animeListEntries).containsExactlyInAnyOrder(*parsedFile.animeListEntries.toTypedArray())
        assertThat(watchListEntries).containsExactlyInAnyOrder(*parsedFile.watchListEntries.toTypedArray())
        assertThat(ignoreListEntries).containsExactlyInAnyOrder(*parsedFile.ignoreListEntries.toTypedArray())
    }
}