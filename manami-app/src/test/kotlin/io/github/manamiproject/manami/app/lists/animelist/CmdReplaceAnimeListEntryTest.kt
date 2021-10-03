package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.*
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

internal class CmdReplaceAnimeListEntryTest {

    @Test
    fun `don't replace entry if the currentEntry doesn't exist in anime list`() {
        // given
        val animeListEntry = AnimeListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            episodes = 26,
            type = TV,
            location = URI("some/relative/path/beck"),
        )

        val savedEntries = mutableListOf<AnimeListEntry>()
        val testState = object: State by TestState {
            override fun openedFile(): OpenedFile = NoFile
            override fun animeListEntrtyExists(anime: AnimeListEntry): Boolean = false
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                savedEntries.addAll(anime)
            }
        }

        val command = CmdReplaceAnimeListEntry(
            state = testState,
            currentEntry = animeListEntry,
            replacementEntry = animeListEntry.copy(title = "Replaced Beck")
        )

        // when
        val result = command.execute()

        //then
        assertThat(result).isFalse()
        assertThat(savedEntries).isEmpty()
    }

    @Test
    fun `replace anime - containing original path, because no file is opened`() {
        // given
        val savedEntries = mutableListOf<AnimeListEntry>()
        val removedEntries = mutableListOf<AnimeListEntry>()
        val testState = object: State by TestState {
            override fun openedFile(): OpenedFile = NoFile
            override fun animeListEntrtyExists(anime: AnimeListEntry): Boolean = true
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                savedEntries.addAll(anime)
            }
            override fun removeAnimeListEntry(entry: AnimeListEntry) {
                removedEntries.add(entry)
            }
        }

        val animeListEntry = AnimeListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            episodes = 26,
            type = TV,
            location = URI("some/relative/path/beck"),
        )
        val expectedEntry = animeListEntry.copy(title = "Replacement Beck")

        val command = CmdReplaceAnimeListEntry(
            state = testState,
            currentEntry = animeListEntry,
            replacementEntry = expectedEntry
        )

        // when
        val result = command.execute()

        //then
        assertThat(result).isTrue()
        assertThat(savedEntries).containsExactly(expectedEntry)
    }

    @Test
    fun `create relative path if file has been opened`() {
        // given
        tempDirectory {
            val openedFile = tempDir.resolve("test.xml").createFile()
            val testLocation = tempDir.resolve("anime").createDirectory().resolve("beck").createDirectory()

            val savedEntries = mutableListOf<AnimeListEntry>()
            val removedEntries = mutableListOf<AnimeListEntry>()
            val testState = object: State by TestState {
                override fun openedFile(): OpenedFile = CurrentFile(openedFile)
                override fun animeListEntrtyExists(anime: AnimeListEntry): Boolean = true
                override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                    savedEntries.addAll(anime)
                }
                override fun removeAnimeListEntry(entry: AnimeListEntry) {
                    removedEntries.add(entry)
                }
            }

            val animeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = URI(testLocation.toAbsolutePath().toString()),
            )

            val command = CmdReplaceAnimeListEntry(
                state = testState,
                currentEntry = animeListEntry,
                replacementEntry = animeListEntry.copy(title = "Replacement Beck"),
            )

            // when
            val result = command.execute()

            //then
            assertThat(result).isTrue()
            assertThat(savedEntries.first().location).isEqualTo(URI("anime/beck"))
        }
    }
}