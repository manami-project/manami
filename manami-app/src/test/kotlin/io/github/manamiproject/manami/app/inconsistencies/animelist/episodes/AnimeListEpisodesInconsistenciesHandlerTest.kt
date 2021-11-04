package io.github.manamiproject.manami.app.inconsistencies.animelist.episodes

import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.OpenedFile
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.models.Anime.Type.OVA
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Files

internal class AnimeListEpisodesInconsistenciesHandlerTest {

    @Nested
    inner class IsExecutableTests {

        @Test
        fun `is executable if the config explicitly activates the option`() {
            // given
            val inconsistencyHandler = AnimeListEpisodesInconsistenciesHandler(
                state = TestState,
            )

            val isExecutableConfig = InconsistenciesSearchConfig(
                checkAnimeListEpisodes = true
            )

            // when
            val result = inconsistencyHandler.isExecutable(isExecutableConfig)

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `is not executable if the config doesn't explicitly activates the option`() {
            // given
            val inconsistencyHandler = AnimeListEpisodesInconsistenciesHandler(
                state = TestState,
            )

            val isNotExecutableConfig = InconsistenciesSearchConfig(
                checkAnimeListEpisodes = false
            )

            // when
            val result = inconsistencyHandler.isExecutable(isNotExecutableConfig)

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class CalculateWorkloadTests {

        @Test
        fun `workload is computed by the number of anime list entries having a link`() {
            // given
            val testState = object : State by TestState {
                override fun animeList(): List<AnimeListEntry> {
                    return listOf(
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/5114"),
                            title = "Fullmetal Alchemist: Brotherhood",
                            type = TV,
                            episodes = 64,
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
                            location = URI(".")
                        ),
                        AnimeListEntry(
                            link = NoLink,
                            title = "Ame-iro Cocoa: Rainy Color e Youkoso!",
                            type = TV,
                            episodes = 12,
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1065/111717t.jpg"),
                            location = URI("."),
                        ),
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/37747"),
                            title = "Ame-iro Cocoa: Side G",
                            type = TV,
                            episodes = 12,
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1394/111379t.jpg"),
                            location = URI("."),
                        )
                    )
                }
            }

            val inconsistencyHandler = AnimeListEpisodesInconsistenciesHandler(
                state = testState,
            )

            // when
            val result = inconsistencyHandler.calculateWorkload()

            // then
            assertThat(result).isEqualTo(2)
        }
    }

    @Nested
    inner class ExecuteTests {

        @Test
        fun `exclude entries without link`() {
            tempDirectory {
                // given
                //FIXME: use kotlin extensions
                val testOpenedFile = Files.createFile(tempDir.resolve("testfile.xml"))

                val testState = object : State by TestState {
                    override fun openedFile(): OpenedFile = CurrentFile(testOpenedFile)
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            AnimeListEntry(
                                link = NoLink,
                                title = "No Link Entry",
                                type = TV,
                                episodes = 12,
                                thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                                location = URI("/test")
                            ),
                        )
                    }
                }

                val handler = AnimeListEpisodesInconsistenciesHandler(
                    state = testState,
                )

                // when
                val result = handler.execute()

                // then
                assertThat(result.entries).isEmpty()
            }
        }

        @Test
        fun `exclude entries having the same amount of files as expected number of episodes`() {
            tempDirectory {
                // given
                //FIXME: use kotlin extensions
                val testOpenedFile = Files.createFile(tempDir.resolve("testfile.xml"))
                val dir = Files.createDirectory(tempDir.resolve("test"))
                Files.createFile(dir.resolve("episode1.txt"))
                Files.createFile(dir.resolve("episode2.txt"))

                val testState = object : State by TestState {
                    override fun openedFile(): OpenedFile = CurrentFile(testOpenedFile)
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            AnimeListEntry(
                                link = Link("https://myanimelist.net/anime/6864"),
                                title = "xxxHOLiC Rou",
                                type = OVA,
                                episodes = 2,
                                thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                                location = URI("test")
                            ),
                        )
                    }
                }

                val handler = AnimeListEpisodesInconsistenciesHandler(
                    state = testState,
                )

                // when
                val result = handler.execute()

                // then
                assertThat(result.entries).isEmpty()
            }
        }

        @Test
        fun `ignore files starting with a dot (hidden files)`() {
            tempDirectory {
                // given
                //FIXME: use kotlin extensions
                val testOpenedFile = Files.createFile(tempDir.resolve("testfile.xml"))
                val dir = Files.createDirectory(tempDir.resolve("test"))
                Files.createFile(dir.resolve("episode1.txt"))
                Files.createFile(dir.resolve("episode2.txt"))
                Files.createFile(dir.resolve(".hidden-file"))

                val testState = object : State by TestState {
                    override fun openedFile(): OpenedFile = CurrentFile(testOpenedFile)
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            AnimeListEntry(
                                link = Link("https://myanimelist.net/anime/6864"),
                                title = "xxxHOLiC Rou",
                                type = OVA,
                                episodes = 2,
                                thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                                location = URI("test")
                            ),
                        )
                    }
                }

                val handler = AnimeListEpisodesInconsistenciesHandler(
                    state = testState,
                )

                // when
                val result = handler.execute()

                // then
                assertThat(result.entries).isEmpty()
            }
        }

        @Test
        fun `ignore additional directories`() {
            tempDirectory {
                // given
                //FIXME: use kotlin extensions
                val testOpenedFile = Files.createFile(tempDir.resolve("testfile.xml"))
                val dir = Files.createDirectory(tempDir.resolve("test"))
                Files.createFile(dir.resolve("episode1.txt"))
                Files.createFile(dir.resolve("episode2.txt"))

                val subdir = Files.createDirectory(dir.resolve("subdir"))
                Files.createFile(subdir.resolve("third-file.txt"))

                val testState = object : State by TestState {
                    override fun openedFile(): OpenedFile = CurrentFile(testOpenedFile)
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            AnimeListEntry(
                                link = Link("https://myanimelist.net/anime/6864"),
                                title = "xxxHOLiC Rou",
                                type = OVA,
                                episodes = 2,
                                thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                                location = URI("test")
                            ),
                        )
                    }
                }

                val handler = AnimeListEpisodesInconsistenciesHandler(
                    state = testState,
                )

                // when
                val result = handler.execute()

                // then
                assertThat(result.entries).isEmpty()
            }
        }

        @Test
        fun `include entries if number of files and number of episodes differ - too many files`() {
            tempDirectory {
                // given
                //FIXME: use kotlin extensions
                val testOpenedFile = Files.createFile(tempDir.resolve("testfile.xml"))
                val dir = Files.createDirectory(tempDir.resolve("test"))
                Files.createFile(dir.resolve("episode1.txt"))
                Files.createFile(dir.resolve("episode2.txt"))
                Files.createFile(dir.resolve("episode3.txt"))

                val testState = object : State by TestState {
                    override fun openedFile(): OpenedFile = CurrentFile(testOpenedFile)
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            AnimeListEntry(
                                link = Link("https://myanimelist.net/anime/6864"),
                                title = "xxxHOLiC Rou",
                                type = OVA,
                                episodes = 2,
                                thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                                location = URI("test")
                            ),
                        )
                    }
                }

                val handler = AnimeListEpisodesInconsistenciesHandler(
                    state = testState,
                )

                // when
                val result = handler.execute()

                // then
                assertThat(result.entries).hasSize(1)
            }
        }

        @Test
        fun `include entries if number of files and number of episodes differ - not enough files`() {
            tempDirectory {
                // given
                //FIXME: use kotlin extensions
                val testOpenedFile = Files.createFile(tempDir.resolve("testfile.xml"))
                val dir = Files.createDirectory(tempDir.resolve("test"))
                Files.createFile(dir.resolve("episode1.txt"))

                val testState = object : State by TestState {
                    override fun openedFile(): OpenedFile = CurrentFile(testOpenedFile)
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            AnimeListEntry(
                                link = Link("https://myanimelist.net/anime/6864"),
                                title = "xxxHOLiC Rou",
                                type = OVA,
                                episodes = 2,
                                thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                                location = URI("test")
                            ),
                        )
                    }
                }

                val handler = AnimeListEpisodesInconsistenciesHandler(
                    state = testState,
                )

                // when
                val result = handler.execute()

                // then
                assertThat(result.entries).hasSize(1)
            }
        }
    }
}