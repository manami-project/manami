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
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile

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
                            location = Path("."),
                        ),
                        AnimeListEntry(
                            link = NoLink,
                            title = "Ame-iro Cocoa: Rainy Color e Youkoso!",
                            type = TV,
                            episodes = 12,
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1065/111717t.jpg"),
                            location = Path("."),
                        ),
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/37747"),
                            title = "Ame-iro Cocoa: Side G",
                            type = TV,
                            episodes = 12,
                            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1394/111379t.jpg"),
                            location = Path("."),
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
                val testOpenedFile = tempDir.resolve("testfile.xml").createFile()

                val testLocation = tempDir.resolve("test").createDirectory().toAbsolutePath()
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
                                location = testLocation,
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
                val testOpenedFile = tempDir.resolve("testfile.xml").createFile()
                val dir = tempDir.resolve("test").createDirectory()
                dir.resolve("episode1.txt").createFile()
                dir.resolve("episode2.txt").createFile()

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
                                location = Path("test"),
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
                val testOpenedFile = tempDir.resolve("testfile.xml").createFile()
                val dir = tempDir.resolve("test").createDirectory()
                dir.resolve("episode1.txt").createFile()
                dir.resolve("episode2.txt").createFile()
                dir.resolve(".hidden-file").createFile()

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
                                location = Path("test"),
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
                val testOpenedFile = tempDir.resolve("testfile.xml").createFile()
                val dir = tempDir.resolve("test").createDirectory()
                dir.resolve("episode1.txt").createFile()
                dir.resolve("episode2.txt").createFile()

                val subdir = dir.resolve("subdir").createDirectory()
                subdir.resolve("third-file.txt").createFile()

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
                                location = Path("test"),
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
                val testOpenedFile = tempDir.resolve("testfile.xml").createFile()
                val dir = tempDir.resolve("test").createDirectory()
                dir.resolve("episode1.txt").createFile()
                dir.resolve("episode2.txt").createFile()
                dir.resolve("episode3.txt").createFile()

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
                                location = Path("test"),
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
                val testOpenedFile = tempDir.resolve("testfile.xml").createFile()
                val dir = tempDir.resolve("test").createDirectory()
                dir.resolve("episode1.txt").createFile()

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
                                location = Path("test"),
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