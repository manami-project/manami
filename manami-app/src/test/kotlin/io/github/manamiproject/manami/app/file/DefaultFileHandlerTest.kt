package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.import.parser.Parser
import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.state.commands.TestCommandHistory
import io.github.manamiproject.manami.app.state.commands.history.CommandHistory
import io.github.manamiproject.modb.core.extensions.RegularFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.file.Paths

internal class DefaultFileHandlerTest {

    @Nested
    inner class NewFileTests {

        @Test
        fun `throws exception if ignoreUnsavedChanges is false and state is unsaved`() {
            // given
            val testCommandHistory = object: CommandHistory by TestCommandHistory {
                override fun isSaved(): Boolean = false
            }

            val defaultFileHandler = DefaultFileHandler(commandHistory = testCommandHistory)

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

            val defaultFileHandler = DefaultFileHandler(commandHistory = testCommandHistory)

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

            val defaultFileHandler = DefaultFileHandler(commandHistory = testCommandHistory)

            // when
            val result = assertThrows<IllegalStateException> {
                defaultFileHandler.open(Paths.get(".").resolve("test.xml"))
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
                override fun addAllAnimeListEntries(anime: Set<AnimeListEntry>) { }
                override fun addAllWatchListEntries(anime: Set<WatchListEntry>) { }
                override fun addAllIgnoreListEntries(anime: Set<IgnoreListEntry>) { }
                override fun openedFile(file: RegularFile) { isCommandExecuted = true }
                override fun closeFile() { }
            }

            val defaultFileHandler = DefaultFileHandler(
                    state = testState,
                    commandHistory = testCommandHistory,
                    parser = testParser,
            )

            // when
            defaultFileHandler.open(Paths.get(".").resolve("test.xml"), ignoreUnsavedChanged = true)

            // then
            assertThat(isCommandExecuted).isTrue()
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
        )

        // when
        defaultFileHandler.redo()

        // then
        assertThat(hasBeenInvoked).isTrue()
    }
}