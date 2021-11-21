package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.NoFile
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

internal class AnimeListEntryTest {

    @Test
    fun `don't change location, because no file has been opened`() {
        // given
        tempDirectory {
            val testLocation = tempDir.resolve("anime").createDirectory().resolve("beck").createDirectory()

            val animeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = URI(testLocation.toAbsolutePath().toString()),
            )

            // when
            val result = animeListEntry.locationToRelativePathConverter(NoFile)

            // then
            assertThat(result.location).isEqualTo(animeListEntry.location)
        }
    }

    @Test
    fun `change location to be relative to opened file`() {
        // given
        tempDirectory {
            val openedFile = tempDir.resolve("test.xml").createFile()
            val testLocation = tempDir.resolve("anime").createDirectory().resolve("beck").createDirectory()

            val animeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = URI(testLocation.toAbsolutePath().toString()),
            )

            // when
            val result = animeListEntry.locationToRelativePathConverter(CurrentFile(openedFile))

            // then
            assertThat(result.location).isEqualTo(URI("anime/beck"))
        }
    }

    @Test
    fun `set location to a 'dot' if location and directory of the opened file are equal`() {
        // given
        tempDirectory {
            val openedFile = tempDir.resolve("test.xml").createFile()

            val animeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = URI(openedFile.parent.toAbsolutePath().toString()),
            )

            // when
            val result = animeListEntry.locationToRelativePathConverter(CurrentFile(openedFile))

            // then
            assertThat(result.location).isEqualTo(URI("."))
        }
    }
}