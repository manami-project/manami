package io.github.manamiproject.manami.app.fileimport

import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.fileimport.parser.ParsedFile
import io.github.manamiproject.manami.app.fileimport.parser.Parser
import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.Link
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.createFile
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URL
import java.nio.file.Paths

internal class DefaultImportHandlerTest {

    @AfterEach
    fun afterEach() {
        InternalState.clear()
        DefaultCommandHistory.clear()
    }

    @Nested
    inner class ConstructorTests {

        @Test
        fun `throws exception if the list of parsers is empty`() {
            // when
            val result = assertThrows<IllegalArgumentException> {
                DefaultImportHandler(emptyList())
            }

            // then
            assertThat(result).hasMessage("List of parser must not be empty")
        }

        @Test
        fun `throws exception if there is more than one parser handling a file suffix`() {
            // given
            val json1 = object: Parser by TestParser {
                override fun handlesSuffix(): FileSuffix = "json"
            }
            val json2 = object: Parser by TestParser {
                override fun handlesSuffix(): FileSuffix = "json"
            }

            // when
            val result = assertThrows<IllegalArgumentException> {
                DefaultImportHandler(listOf(json1, json2))
            }

            // then
            assertThat(result).hasMessage("Only one parser per file suffix")
        }
    }

    @Nested
    inner class ImportTests {

        @Test
        fun `throws exception if the given file doesn't exist`() {
            // given
            val testParser = object: Parser by TestParser {
                override fun handlesSuffix(): FileSuffix = "json"
            }

            val nonExistentFile = Paths.get("non-existent-file.json")
            val importHandler = DefaultImportHandler(listOf(testParser))

            // when
            val result = assertThrows<IllegalArgumentException> {
                importHandler.import(nonExistentFile)
            }

            // then
            assertThat(result).hasMessage("Given path doesn't exist or is not a file [${nonExistentFile.toAbsolutePath()}]")
        }

        @Test
        fun `throws exception if the given path is not a file`() {
            tempDirectory {
                // given
                val testParser = object: Parser by TestParser {
                    override fun handlesSuffix(): FileSuffix = "json"
                }

                val importHandler = DefaultImportHandler(listOf(testParser))

                // when
                val result = assertThrows<IllegalArgumentException> {
                    importHandler.import(tempDir)
                }

                // then
                assertThat(result).hasMessage("Given path doesn't exist or is not a file [${tempDir.toAbsolutePath()}]")
            }
        }

        @Test
        fun `throws exception if the file's suffix is not supported`() {
            tempDirectory {
                // given
                val testParser = object: Parser by TestParser {
                    override fun handlesSuffix(): FileSuffix = "json"
                }

                val fileWithUnsupportedFileSuffix = tempDir.resolve("test.abc").createFile()
                val importHandler = DefaultImportHandler(listOf(testParser))

                // when
                val result = assertThrows<IllegalArgumentException> {
                    importHandler.import(fileWithUnsupportedFileSuffix)
                }

                // then
                assertThat(result).hasMessage("No suitable parser for file type [abc]")
            }
        }

        @Test
        fun `successfully import file, set state using a reversible command`() {
            tempDirectory {
                // given
                val animeListEntry1 = AnimeListEntry(
                        title = "H2O: Footprints in the Sand",
                        episodes = 4,
                        type = Anime.Type.Special,
                        location = "some/relative/path/h2o_-_footprints_in_the_sand_special",
                )
                val animeListEntry2 = AnimeListEntry(
                        link = Link(URL("https://myanimelist.net/anime/57")),
                        title = "Beck",
                        episodes = 26,
                        type = Anime.Type.TV,
                        location = "some/relative/path/beck",
                )

                val watchListEntry1 = URL("https://myanimelist.net/anime/37989")
                val watchListEntry2 = URL("https://myanimelist.net/anime/40059")

                val ignoreListEntry1 = URL("https://myanimelist.net/anime/28981")
                val ignoreListEntry2 = URL("https://myanimelist.net/anime/33245")
                val ignoreListEntry3 = URL("https://myanimelist.net/anime/35923")
                val ignoreListEntry4 = URL("https://myanimelist.net/anime/31139")
                val ignoreListEntry5 = URL("https://myanimelist.net/anime/37747")

                val testParser = object : Parser {
                    override fun handlesSuffix(): FileSuffix = "xml"
                    override fun parse(file: RegularFile): ParsedFile {
                        return ParsedFile(
                                animeListEntries = setOf(
                                        animeListEntry1,
                                        animeListEntry2,
                                ),
                                watchListEntries = setOf(
                                        watchListEntry1,
                                        watchListEntry2,
                                ),
                                ignoreListEntries = setOf(
                                        ignoreListEntry1,
                                        ignoreListEntry2,
                                        ignoreListEntry3,
                                        ignoreListEntry4,
                                        ignoreListEntry5,
                                ),
                        )
                    }
                }

                val defaultImportHandler = DefaultImportHandler(
                    parserList = listOf(testParser)
                )

                val dummyTestFile = tempDir.resolve("test.xml").createFile()

                // when
                defaultImportHandler.import(dummyTestFile)

                // then
                assertThat(InternalState.animeList()).containsExactlyInAnyOrder(
                        animeListEntry1,
                        animeListEntry2,
                )
                assertThat(InternalState.watchList()).containsExactlyInAnyOrder(
                        watchListEntry1,
                        watchListEntry2,
                )
                assertThat(InternalState.ignoreList()).containsExactlyInAnyOrder(
                        ignoreListEntry1,
                        ignoreListEntry2,
                        ignoreListEntry3,
                        ignoreListEntry4,
                        ignoreListEntry5,
                )
                assertThat(DefaultCommandHistory.isUndoPossible()).isTrue()
            }
        }
    }
}