package io.github.manamiproject.manami.app.import

import io.github.manamiproject.manami.app.import.parser.ParsedFile
import io.github.manamiproject.manami.app.import.parser.Parser
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.models.Link
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.commands.history.DefaultCommandHistory
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
import java.net.URI
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
            val json1 = object: Parser<ParsedFile> by TestImportParser {
                override fun handlesSuffix(): FileSuffix = "json"
            }
            val json2 = object: Parser<ParsedFile> by TestImportParser {
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
            val testParser = object: Parser<ParsedFile> by TestImportParser {
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
                val testParser = object: Parser<ParsedFile> by TestImportParser {
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
                val testParser = object: Parser<ParsedFile> by TestImportParser {
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
                        location = URI("some/relative/path/h2o_-_footprints_in_the_sand_special"),
                )
                val animeListEntry2 = AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/57"),
                        title = "Beck",
                        episodes = 26,
                        type = Anime.Type.TV,
                        location = URI("some/relative/path/beck"),
                )

                val watchListEntry1 = URI("https://myanimelist.net/anime/37989")
                val watchListEntry2 = URI("https://myanimelist.net/anime/40059")

                val ignoreListEntry1 = URI("https://myanimelist.net/anime/28981")
                val ignoreListEntry2 = URI("https://myanimelist.net/anime/33245")
                val ignoreListEntry3 = URI("https://myanimelist.net/anime/35923")
                val ignoreListEntry4 = URI("https://myanimelist.net/anime/31139")
                val ignoreListEntry5 = URI("https://myanimelist.net/anime/37747")

                val testParser = object : Parser<ParsedFile> {
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
                assertThat(InternalState.ignoreList()).containsExactlyInAnyOrder(
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/28981"),
                        title = "Ame-iro Cocoa",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/72517t.jpg")
                    ),
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/33245"),
                        title = "Ame-iro Cocoa in Hawaii",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/3/82186t.jpg")
                    ),
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/35923"),
                        title = "Ame-iro Cocoa Series: Ame-con!!",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/3/88225t.jpg")
                    ),
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/31139"),
                        title = "Ame-iro Cocoa: Rainy Color e Youkoso!",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/76340.jpg")
                    ),
                    IgnoreListEntry(
                        link = Link("https://myanimelist.net/anime/37747"),
                        title = "Ame-iro Cocoa: Side G",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/1463/97361.jpg")
                    ),
                )
                assertThat(DefaultCommandHistory.isUndoPossible()).isTrue()
            }
        }
    }
}