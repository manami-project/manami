package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.commands.TestCommandHistory
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.models.Anime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.io.path.Path

internal class CmdOpenFileTest {

    @Test
    fun `clears state, clears command history, closes file, adds entries from parsed file to state and stes opened file`() {
        // given
        var hasCloseFileBeenCalled = false
        var hasClearStateBeenCalled = false
        var hasClearHistoryBeenCalled = false
        var openedFile = Path(".")
        val animeListEntries = mutableListOf<AnimeListEntry>()
        val watchListEntries = mutableListOf<WatchListEntry>()
        val ignoreListEntries = mutableListOf<IgnoreListEntry>()

        val testState = object: State by TestState {
            override fun closeFile() { hasCloseFileBeenCalled = true }
            override fun clear() { hasClearStateBeenCalled = true }
            override fun setOpenedFile(file: RegularFile) { openedFile = file }
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) { animeListEntries.addAll(anime) }
            override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) { watchListEntries.addAll(anime) }
            override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) { ignoreListEntries.addAll(anime) }
        }

        val testCommandHistory = object: CommandHistory by TestCommandHistory {
            override fun clear() { hasClearHistoryBeenCalled = true }
        }

        val notExistingTestFile = Path(".").resolve("test.xml")

        val parsedFile = ParsedManamiFile(
            animeListEntries = setOf(
                    AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/57"),
                            title = "Beck",
                            episodes = 26,
                            type = Anime.Type.TV,
                            location = URI("some/relative/path/beck"),
                    )
            ),
            watchListEntries = setOf(
                WatchListEntry(
                    link = Link("https://myanimelist.net/anime/40059"),
                    title = "Golden Kamuy 3rd Season",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
                )
            ),
            ignoreListEntries = setOf(
                IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/31139"),
                    title = "Ame-iro Cocoa: Rainy Color e Youkoso!",
                    thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/76340t.jpg")
                )
            )
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