package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.modb.core.models.Anime.Type.SPECIAL
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.io.path.createFile

internal class FileParserTest {

    @Test
    fun `handles XML files`() {
        // given
        val parser = FileParser()

        // when
        val result = parser.handlesSuffix()

        // then
        assertThat(result).isEqualTo("xml")
    }

    @Test
    fun `throws exception if the given path is a directory`() {
        tempDirectory {
            // given
            val parser = FileParser()

            // when
            val result = assertThrows<IllegalArgumentException> {
                parser.parse(tempDir)
            }

            // then
            assertThat(result).hasMessage("Given path [${tempDir.toAbsolutePath()}] is either not a file or doesn't exist.")
        }
    }

    @Test
    fun `throws exception if the given path does not exist`() {
        tempDirectory {
            // given
            val parser = FileParser()
            val file = tempDir.resolve("test.xml")

            // when
            val result = assertThrows<IllegalArgumentException> {
                parser.parse(file)
            }

            // then
            assertThat(result).hasMessage("Given path [$file] is either not a file or doesn't exist.")
        }
    }

    @Test
    fun `given suffix is not supported`() {
        tempDirectory {
            // given
            val parser = FileParser()
            val file = tempDir.resolve("test.json").createFile()

            // when
            val result = assertThrows<IllegalArgumentException> {
                parser.parse(file)
            }

            // then
            assertThat(result).hasMessage("Parser doesn't support given file suffix.")
        }
    }

    @Test
    fun `throws exception if the version is too old`() {
        // given
        val parser = FileParser()
        val file = testResource("file/FileParser/version_too_old.xml")

        // when
        val result = assertThrows<IllegalArgumentException> {
            parser.parse(file)
        }

        // then
        assertThat(result).hasMessage("Unable to parse manami file older than 3.0.0")
    }

    @Test
    fun `correctly parse file`() {
        // given
        val parser = FileParser()
        val file = testResource("file/FileParser/correctly_parse_entries.xml")

        // when
        val result = parser.parse(file)

        // then
        assertThat(result.animeListEntries).containsExactlyInAnyOrder(
                AnimeListEntry(
                        title = "H2O: Footprints in the Sand",
                        thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                        episodes = 4,
                        type = SPECIAL,
                        location = URI("some/relative/path/h2o_-_footprints_in_the_sand_special"),
                ),
                AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/57"),
                        title = "Beck",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                        episodes = 26,
                        type = TV,
                        location = URI("some/relative/path/beck"),
                ),
        )
        assertThat(result.watchListEntries).containsExactlyInAnyOrder(
            WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            ),
            WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            ),
        )
        assertThat(result.ignoreListEntries).containsExactlyInAnyOrder(
            IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/28981"),
                title = "Ame-iro Cocoa",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg")
            ),
            IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/33245"),
                title = "Ame-iro Cocoa in Hawaii",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1727/111715t.jpg")
            ),
            IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/35923"),
                title = "Ame-iro Cocoa Series: Ame-con!!",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1165/111716t.jpg")
            ),
            IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/31139"),
                title = "Ame-iro Cocoa: Rainy Color e Youkoso!",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1065/111717t.jpg")
            ),
            IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/37747"),
                title = "Ame-iro Cocoa: Side G",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1394/111379t.jpg")
            ),
        )
    }
}