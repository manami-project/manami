package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.commands.TestCommandHistory
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.*
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.test.tempDirectory
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.io.path.Path
import kotlin.io.path.createFile
import kotlin.test.AfterTest

internal class DefaultFileHandlerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class NewFileTests {

        @Test
        fun `throws exception if ignoreUnsavedChanges is false and state is unsaved`() {
            // given
            val testCommandHistory = object: CommandHistory by TestCommandHistory {
                override fun isSaved(): Boolean = false
            }

            val defaultFileHandler = DefaultFileHandler(
                state = TestState,
                commandHistory = testCommandHistory,
                parser = TestManamiFileParser,
                fileWriter = TestFileWriter,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = assertThrows<IllegalStateException> {
                defaultFileHandler.newFile()
            }

            // then
            assertThat(result).hasMessage("Cannot create a new list, because there are unsaved changes.")
        }

        @Test
        fun `creates a new list if ignoreUnsavedChanges is true and state is unsaved`() {
            // given
            var isCommandExecuted = false

            val testCommandHistory = object: CommandHistory by TestCommandHistory {
                override fun isSaved(): Boolean = false
                override fun clear() { isCommandExecuted = true }
            }

            val testState = object: State by TestState {
                override fun closeFile() { }
                override fun clear() {}
            }

            val defaultFileHandler = DefaultFileHandler(
                state = testState,
                commandHistory = testCommandHistory,
                parser = TestManamiFileParser,
                fileWriter = TestFileWriter,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            defaultFileHandler.newFile(ignoreUnsavedChanged = true)

            // then
            assertThat(isCommandExecuted).isTrue()
        }
    }

    @Nested
    inner class OpenTests {

        @Test
        fun `throws exception if ignoreUnsavedChanges is false and state is unsaved`() {
            // given
            val testCommandHistory = object: CommandHistory by TestCommandHistory {
                override fun isSaved(): Boolean = false
            }

            val defaultFileHandler = DefaultFileHandler(
                state = TestState,
                commandHistory = testCommandHistory,
                parser = TestManamiFileParser,
                fileWriter = TestFileWriter,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = assertThrows<IllegalStateException> {
                defaultFileHandler.open(Path(".").resolve("test.xml"))
            }

            // then
            assertThat(result).hasMessage("Cannot open file, because there are unsaved changes.")
        }

        @Test
        fun `creates a new list if ignoreUnsavedChanges is true and state is unsaved`() {
            // given
            var isCommandExecuted = false

            val testCommandHistory = object: CommandHistory by TestCommandHistory {
                override fun isSaved(): Boolean = false
                override fun clear() { isCommandExecuted = true }
            }

            val testParser = object: Parser<ParsedManamiFile> by TestManamiFileParser {
                override fun parse(file: RegularFile): ParsedManamiFile = ParsedManamiFile()
            }

            val testState = object: State by TestState{
                override fun clear() { }
                override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) { }
                override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) { }
                override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) { }
                override fun setOpenedFile(file: RegularFile) { isCommandExecuted = true }
                override fun closeFile() { }
            }

            val defaultFileHandler = DefaultFileHandler(
                state = testState,
                commandHistory = testCommandHistory,
                parser = testParser,
                fileWriter = TestFileWriter,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            defaultFileHandler.open(Path(".").resolve("test.xml"), ignoreUnsavedChanged = true)

            // then
            assertThat(isCommandExecuted).isTrue()
            assertThat(CoroutinesFlowEventBus.generalAppState.value.openedFile).isEqualTo("test.xml")
        }
    }

    @Test
    fun `simply delegate isSaved to command history`() {
        // given
        val testCommandHistory = object: CommandHistory by TestCommandHistory {
            override fun isSaved(): Boolean = false
        }

        val defaultFileHandler = DefaultFileHandler(
            state = TestState,
            commandHistory = testCommandHistory,
            parser = TestManamiFileParser,
            fileWriter = TestFileWriter,
            eventBus = CoroutinesFlowEventBus,
        )

        // when
        val result = defaultFileHandler.isSaved()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `simply delegate isUnsaved to command history`() {
        // given
        val testCommandHistory = object: CommandHistory by TestCommandHistory {
            override fun isUnsaved(): Boolean = true
        }

        val defaultFileHandler = DefaultFileHandler(
            state = TestState,
            commandHistory = testCommandHistory,
            parser = TestManamiFileParser,
            fileWriter = TestFileWriter,
            eventBus = CoroutinesFlowEventBus,
        )

        // when
        val result = defaultFileHandler.isUnsaved()

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `simply delegate isUndoPossible to command history`() {
        // given
        val testCommandHistory = object: CommandHistory by TestCommandHistory {
            override fun isUndoPossible(): Boolean = true
        }

        val defaultFileHandler = DefaultFileHandler(
            state = TestState,
            commandHistory = testCommandHistory,
            parser = TestManamiFileParser,
            fileWriter = TestFileWriter,
            eventBus = CoroutinesFlowEventBus,
        )

        // when
        val result = defaultFileHandler.isUndoPossible()

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `simply delegate isRedoPossible to command history`() {
        // given
        val testCommandHistory = object: CommandHistory by TestCommandHistory {
            override fun isRedoPossible(): Boolean = false
        }

        val defaultFileHandler = DefaultFileHandler(
            state = TestState,
            commandHistory = testCommandHistory,
            parser = TestManamiFileParser,
            fileWriter = TestFileWriter,
            eventBus = CoroutinesFlowEventBus,
        )

        // when
        val result = defaultFileHandler.isRedoPossible()

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `simply delegate undo to command history`() {
        // given
        var hasBeenInvoked = false

        val testCommandHistory = object: CommandHistory by TestCommandHistory {
            override fun undo() { hasBeenInvoked = true }
        }

        val defaultFileHandler = DefaultFileHandler(
            state = TestState,
            commandHistory = testCommandHistory,
            parser = TestManamiFileParser,
            fileWriter = TestFileWriter,
            eventBus = CoroutinesFlowEventBus,
        )

        // when
        defaultFileHandler.undo()

        // then
        assertThat(hasBeenInvoked).isTrue()
    }

    @Test
    fun `simply delegate redo to command history`() {
        // given
        var hasBeenInvoked = false

        val testCommandHistory = object: CommandHistory by TestCommandHistory {
            override fun redo() { hasBeenInvoked = true }
        }

        val defaultFileHandler = DefaultFileHandler(
            state = TestState,
            commandHistory = testCommandHistory,
            parser = TestManamiFileParser,
            fileWriter = TestFileWriter,
            eventBus = CoroutinesFlowEventBus,
        )

        // when
        defaultFileHandler.redo()

        // then
        assertThat(hasBeenInvoked).isTrue()
    }

    @Nested
    inner class IsOpenFileSetTests {

        @Test
        fun `return true if the opened file is of type CurrentFile which indicates that an file has been set`() {
            // given
            val testState = object: State by TestState {
                override fun openedFile(): OpenedFile = CurrentFile(Path("."))
            }

            val defaultFileHandler = DefaultFileHandler(
                state = testState,
                commandHistory = TestCommandHistory,
                parser = TestManamiFileParser,
                fileWriter = TestFileWriter,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = defaultFileHandler.isOpenFileSet()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `return false if the opened file is not of type CurrentFile`() {
            // given
            val testState = object: State by TestState {
                override fun openedFile(): OpenedFile = NoFile
            }

            val defaultFileHandler = DefaultFileHandler(
                state = testState,
                commandHistory = TestCommandHistory,
                parser = TestManamiFileParser,
                fileWriter = TestFileWriter,
                eventBus = CoroutinesFlowEventBus,
            )

            // when
            val result = defaultFileHandler.isOpenFileSet()

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class SaveTests {

        @Test
        fun `don't do anything if state is already saved`() {
            runBlocking {
                // given
                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun isSaved(): Boolean = true
                }

                val defaultFileHandler = DefaultFileHandler(
                    state = TestState,
                    commandHistory = testCommandHistory,
                    parser = TestManamiFileParser,
                    fileWriter = TestFileWriter,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultFileHandler.save()
            }
        }

        @Test
        fun `throws exception if openedFile is not set`() {
            tempDirectory {
                // given
                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun isSaved(): Boolean = false
                }

                val testState = object: State by TestState {
                    override fun openedFile(): OpenedFile = NoFile
                }

                val defaultFileHandler = DefaultFileHandler(
                    state = testState,
                    commandHistory = testCommandHistory,
                    parser = TestManamiFileParser,
                    fileWriter = TestFileWriter,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                val result = assertThrows<IllegalStateException> {
                    defaultFileHandler.save()
                }

                // then
                assertThat(result).hasMessage("No file set")
            }
        }

        @Test
        fun `successfully write file`() {
            tempDirectory {
                // given
                var  cmdHasBeenSaved = false
                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun isSaved(): Boolean = false
                    override fun save() {
                        cmdHasBeenSaved = true
                    }
                }

                val testState = object: State by TestState {
                    override fun openedFile(): OpenedFile = CurrentFile(tempDir.resolve("test.xml").createFile())
                }

                var fileHasBeenWritten = false
                val testFileWriter = object: FileWriter by TestFileWriter {
                    override suspend fun writeTo(file: RegularFile) {
                        fileHasBeenWritten = true
                    }
                }

                val defaultFileHandler = DefaultFileHandler(
                    state = testState,
                    commandHistory = testCommandHistory,
                    parser = TestManamiFileParser,
                    fileWriter = testFileWriter,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultFileHandler.save()

                // then
                assertThat(fileHasBeenWritten).isTrue()
                assertThat(cmdHasBeenSaved).isTrue()
            }
        }
    }

    @Nested
    inner class SaveAsTests {

        @Test
        fun `create file if it doesn't exit`() {
            tempDirectory {
                // given
                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun isSaved(): Boolean = false
                    override fun save() { }
                }

                var savedFile: RegularFile? = null
                val testState = object: State by TestState {
                    override fun openedFile(): OpenedFile = CurrentFile(savedFile!!)
                    override fun setOpenedFile(file: RegularFile) {
                        savedFile = file
                    }
                }

                val testFileWriter = object: FileWriter by TestFileWriter {
                    override suspend fun writeTo(file: RegularFile) { }
                }

                val defaultFileHandler = DefaultFileHandler(
                    state = testState,
                    commandHistory = testCommandHistory,
                    parser = TestManamiFileParser,
                    fileWriter = testFileWriter,
                    eventBus = CoroutinesFlowEventBus,
                )

                val fileToSave = tempDir.resolve("junit-test.xml")

                // when
                defaultFileHandler.saveAs(fileToSave)

                // then
                assertThat(savedFile).isEqualTo(fileToSave)
                assertThat(CoroutinesFlowEventBus.generalAppState.value.openedFile).isEqualTo("junit-test.xml")
            }
        }

        @Test
        fun `successfully write file`() {
            tempDirectory {
                // given
                var  cmdHasBeenSaved = false
                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun isSaved(): Boolean = false
                    override fun save() {
                        cmdHasBeenSaved = true
                    }
                }

                val file = tempDir.resolve("test.xml").createFile()
                val testState = object: State by TestState {
                    override fun openedFile(): OpenedFile = CurrentFile(file)
                    override fun setOpenedFile(file: RegularFile) { }
                }

                var fileHasBeenWritten = false
                val testFileWriter = object: FileWriter by TestFileWriter {
                    override suspend fun writeTo(file: RegularFile) {
                        fileHasBeenWritten = true
                    }
                }

                val defaultFileHandler = DefaultFileHandler(
                    state = testState,
                    commandHistory = testCommandHistory,
                    parser = TestManamiFileParser,
                    fileWriter = testFileWriter,
                    eventBus = CoroutinesFlowEventBus,
                )

                // when
                defaultFileHandler.saveAs(file)

                // then
                assertThat(fileHasBeenWritten).isTrue()
                assertThat(cmdHasBeenSaved).isTrue()
                assertThat(CoroutinesFlowEventBus.generalAppState.value.openedFile).isEqualTo("test.xml")
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultFileHandler.instance

            // when
            val result = DefaultFileHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultFileHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}