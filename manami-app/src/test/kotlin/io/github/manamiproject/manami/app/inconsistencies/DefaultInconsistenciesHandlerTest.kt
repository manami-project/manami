package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.commands.ReversibleCommand
import io.github.manamiproject.manami.app.commands.TestCommandHistory
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.InconsistenciesState
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesHandler
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.NoFile
import io.github.manamiproject.manami.app.state.OpenedFile
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.manami.app.state.snapshot.StateSnapshot
import io.github.manamiproject.modb.core.anime.AnimeType
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import java.net.URI
import kotlin.io.path.Path
import kotlin.test.AfterTest
import kotlin.test.Test

internal class DefaultInconsistenciesHandlerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class FixAnimeListEntryMetaDataInconsistenciesTests {

        @Test
        fun `do nothing if both entries are identical`() {
            runBlocking {
                // given
                val currentEntry = AnimeListEntry(
                    title = "test",
                    link = Link(URI("https://example.org/anime/1")),
                    episodes = 1,
                    type = AnimeType.UNKNOWN,
                    location = Path(""),
                )
                val replacementEntry = currentEntry.copy()

                val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                    state = TestState,
                    commandHistory = TestCommandHistory,
                    inconsistencyHandlers = listOf(AnimeListMetaDataInconsistenciesHandler()),
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultInconsistenciesHandler.fixAnimeListEntryMetaDataInconsistencies(currentEntry, replacementEntry)
            }
        }

        @Test
        fun `replace current anime list entry with the new instance`() {
            runBlocking {
                // given
                val currentEntry = AnimeListEntry(
                    title = "test",
                    link = Link(URI("https://example.org/anime/1")),
                    episodes = 1,
                    type = AnimeType.UNKNOWN,
                    location = Path(""),
                )
                val replacementEntry = currentEntry.copy(
                    location = Path("./test"),
                )

                val removedEntries = mutableListOf<AnimeListEntry>()
                val addedEntries = mutableListOf<AnimeListEntry>()
                val testState = object: State by TestState {
                    override fun openedFile(): OpenedFile = NoFile
                    override fun createSnapshot(): Snapshot = StateSnapshot()
                    override fun animeListEntryExists(anime: AnimeListEntry): Boolean = true
                    override fun removeAnimeListEntry(entry: AnimeListEntry) {
                        removedEntries.add(entry)
                    }
                    override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                        addedEntries.addAll(anime)
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) {}
                }

                val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                    state = testState,
                    commandHistory = testCommandHistory,
                    inconsistencyHandlers = listOf(AnimeListMetaDataInconsistenciesHandler()),
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultInconsistenciesHandler.fixAnimeListEntryMetaDataInconsistencies(currentEntry, replacementEntry)

                // then
                assertThat(removedEntries).hasSize(1)
                assertThat(addedEntries).hasSize(1)
                assertThat(removedEntries.first()).isEqualTo(currentEntry)
                assertThat(addedEntries.first()).isEqualTo(replacementEntry)
            }
        }
    }

    @Nested
    inner class FindInconsistenciesTests {

        @Test
        fun `simply executes the different handlers`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<InconsistenciesState>()
                val eventCollector = launch { CoroutinesFlowEventBus.inconsistenciesState.collect { event -> receivedEvents.add(event) } }
                delay(100)

                var hasBeenInvoked = false
                val testInconsistencyHandler = object: InconsistencyHandler<Unit> by TestInconsistencyHandler {
                    override suspend fun execute() {
                        hasBeenInvoked = true
                    }
                }

                val defaultInconsistenciesHandler = DefaultInconsistenciesHandler(
                    state = TestState,
                    commandHistory = TestCommandHistory,
                    inconsistencyHandlers = listOf(testInconsistencyHandler),
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultInconsistenciesHandler.findInconsistencies()

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, stop
                assertThat(hasBeenInvoked).isTrue()
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultInconsistenciesHandler.instance

            // when
            val result = DefaultInconsistenciesHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultInconsistenciesHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}