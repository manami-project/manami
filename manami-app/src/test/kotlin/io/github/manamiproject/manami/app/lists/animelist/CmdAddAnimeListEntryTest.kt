package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.*
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

internal class CmdAddAnimeListEntryTest {

    @Test
    fun `add the anime list entry to the state - containing original path, because no file is opened`() {
        // given
        val savedEntries = mutableListOf<AnimeListEntry>()
        val testState = object: State by TestState {
            override fun openedFile(): OpenedFile = NoFile
            override fun animeList(): List<AnimeListEntry> = emptyList()
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                savedEntries.addAll(anime)
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

        val command = CmdAddAnimeListEntry(
            state = testState,
            animeListEntry = animeListEntry,
        )

        // when
        val result = command.execute()

        //then
        assertThat(result).isTrue()
        assertThat(savedEntries).containsExactly(animeListEntry)
    }

    @Test
    fun `don't add entry if it already exists in list`() {
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
            override fun animeList(): List<AnimeListEntry> = listOf(animeListEntry)
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                savedEntries.addAll(anime)
            }
        }

        val command = CmdAddAnimeListEntry(
            state = testState,
            animeListEntry = animeListEntry,
        )

        // when
        val result = command.execute()

        //then
        assertThat(result).isFalse()
        assertThat(savedEntries).isEmpty()
    }

    @Test
    fun `create relative path if file has been opened`() {
        // given
        tempDirectory {
            val openedFile = tempDir.resolve("test.xml").createFile()
            val testLocation = tempDir.resolve("anime").createDirectory().resolve("beck").createDirectory()

            val savedEntries = mutableListOf<AnimeListEntry>()
            val testState = object: State by TestState {
                override fun openedFile(): OpenedFile = CurrentFile(openedFile)
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                    savedEntries.addAll(anime)
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

            val command = CmdAddAnimeListEntry(
                state = testState,
                animeListEntry = animeListEntry,
            )

            // when
            val result = command.execute()

            //then
            assertThat(result).isTrue()
            assertThat(savedEntries.first().location).isEqualTo(URI("anime/beck"))
        }
    }
}