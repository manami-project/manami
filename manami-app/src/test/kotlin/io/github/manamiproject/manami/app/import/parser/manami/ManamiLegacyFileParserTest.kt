package io.github.manamiproject.manami.app.import.parser.manami

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.modb.core.models.Anime.Type.SPECIAL
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.test.tempDirectory
import io.github.manamiproject.modb.test.testResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import kotlin.io.path.createFile

internal class ManamiLegacyFileParserTest {

    @Test
    fun `handles XML files`() {
        // given
        val parser = ManamiLegacyFileParser()

        // when
        val result = parser.handlesSuffix()

        // then
        assertThat(result).isEqualTo("xml")
    }

    @Test
    fun `throws exception if the given path is a directory`() {
        tempDirectory {
            // given
            val parser = ManamiLegacyFileParser()

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
            val parser = ManamiLegacyFileParser()
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
            val parser = ManamiLegacyFileParser()
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
    fun `throws exception if the version is too new`() {
        // given
        val parser = ManamiLegacyFileParser()
        val file = testResource("fileimport/parser/manami/LegacyManamiParser/version_too_new.xml")

        // when
        val result = assertThrows<IllegalArgumentException> {
            parser.parse(file)
        }

        // then
        assertThat(result).hasMessage("Unable to parse manami file newer than 3.0.0")
    }

    @Test
    fun `correctly parse file`() {
        // given
        val parser = ManamiLegacyFileParser()
        val file = testResource("fileimport/parser/manami/LegacyManamiParser/correctly_parse_entries.xml")

        // when
        val result = parser.parse(file)

        // then
        assertThat(result.animeListEntries).containsExactlyInAnyOrder(
                AnimeListEntry(
                    title = "H2O: Footprints in the Sand",
                    episodes = 4,
                    type = SPECIAL,
                    location = URI("some/relative/path/h2o_-_footprints_in_the_sand_special"),
                ),
                AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/57"),
                    title = "Beck",
                    episodes = 26,
                    type = TV,
                    location = URI("some/relative/path/beck"),
                )
        )
        assertThat(result.watchListEntries).containsExactlyInAnyOrder(
            URI("https://myanimelist.net/anime/37989"),
            URI("https://myanimelist.net/anime/40059"),
        )
        assertThat(result.ignoreListEntries).containsExactlyInAnyOrder(
            URI("https://myanimelist.net/anime/28981"),
            URI("https://myanimelist.net/anime/33245"),
            URI("https://myanimelist.net/anime/35923"),
            URI("https://myanimelist.net/anime/31139"),
            URI("https://myanimelist.net/anime/37747"),
        )
    }

    @Test
    fun `type 'music' gets converted to 'special'`() {
        // given
        val parser = ManamiLegacyFileParser()
        val file = testResource("fileimport/parser/manami/LegacyManamiParser/convert_type_music_to_special.xml")

        // when
        val result = parser.parse(file)

        // then
        assertThat(result.animeListEntries).containsExactlyInAnyOrder(
                AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/12079"),
                        title = "Black★Rock Shooter",
                        episodes = 1,
                        type = SPECIAL,
                        location = URI("some/relative/path/black_rock_shooter"),
                ),
        )
    }
}