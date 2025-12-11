package io.github.manamiproject.manami.app.inconsistencies.animelist.episodes

import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.InconsistenciesState
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.OpenedFile
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.anime.AnimeType.OVA
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.test.AfterTest
import kotlin.test.Test

internal class AnimeListEpisodesInconsistenciesHandlerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class ExecuteTests {

        @Test
        fun `don't update state if there is nothing to report`() {
            runBlocking {
                tempDirectory {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testOpenedFile = tempDir.resolve("test-file.json").createFile()

                    val testState = object : State by TestState {
                        override fun openedFile(): OpenedFile = CurrentFile(testOpenedFile)
                        override fun animeList(): List<AnimeListEntry> {
                            return emptyList()
                        }
                    }

                    val handler = AnimeListEpisodesInconsistenciesHandler(
                        state = testState,
                    )

                    // when
                    val result = handler.execute()

                    // then
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(1) // initial

                    assertThat(result).isEmpty()
                }
            }
        }

        @Test
        fun `exclude entries having the same amount of files as expected number of episodes`() {
            tempDirectory {
                // given
                val testOpenedFile = tempDir.resolve("test-file.json").createFile()
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
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
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
                assertThat(result).isEmpty()
            }
        }

        @Test
        fun `ignore files starting with a dot (hidden files)`() {
            tempDirectory {
                // given
                val testOpenedFile = tempDir.resolve("test-file.json").createFile()
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
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
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
                assertThat(result).isEmpty()
            }
        }

        @Test
        fun `ignore additional directories`() {
            tempDirectory {
                // given
                val testOpenedFile = tempDir.resolve("test-file.json").createFile()
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
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
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
                assertThat(result).isEmpty()
            }
        }

        @Test
        fun `include entries if number of files and number of episodes differ - too many files`() {
            tempDirectory {
                // given
                val testOpenedFile = tempDir.resolve("test-file.json").createFile()
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
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
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
                assertThat(result).hasSize(1)
            }
        }

        @Test
        fun `include entries if number of files and number of episodes differ - not enough files`() {
            tempDirectory {
                // given
                val testOpenedFile = tempDir.resolve("test-file.json").createFile()
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
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
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
                assertThat(result).hasSize(1)
            }
        }

        @Test
        fun `return 0 number of files if directory doesn't exist`() {
            tempDirectory {
                // given
                val testOpenedFile = tempDir.resolve("test-file.json").createFile()

                val testState = object : State by TestState {
                    override fun openedFile(): OpenedFile = CurrentFile(testOpenedFile)
                    override fun animeList(): List<AnimeListEntry> {
                        return listOf(
                            AnimeListEntry(
                                link = Link("https://myanimelist.net/anime/6864"),
                                title = "xxxHOLiC Rou",
                                type = OVA,
                                episodes = 2,
                                thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
                                location = Path("non-existent"),
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
                assertThat(result).hasSize(1)
            }
        }

        @Test
        fun `update state for any findings`() {
            runBlocking { 
                tempDirectory {
                    // given
                    val receivedEvents = mutableListOf<InconsistenciesState>()
                    val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                    delay(100)

                    val testOpenedFile = tempDir.resolve("test-file.json").createFile()
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
                                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
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
                    delay(100)
                    eventCollector.cancelAndJoin()
                    assertThat(receivedEvents).hasSize(2) // initial, update

                    assertThat(result).hasSize(1)
                }
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = AnimeListEpisodesInconsistenciesHandler.instance

            // when
            val result = AnimeListEpisodesInconsistenciesHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(AnimeListEpisodesInconsistenciesHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}