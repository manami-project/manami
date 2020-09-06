package io.github.manamiproject.manami.app.commands.history

import io.github.manamiproject.manami.app.commands.ReversibleCommand
import io.github.manamiproject.manami.app.commands.TestReversibleCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class CommandHistoryTest {

    @BeforeEach
    fun beforeEach() {
        CommandHistory.clear()
    }

    @Nested
    inner class PushTests {

        @Test
        fun `successfully add a new command`() {
            // when
            CommandHistory.push(TestReversibleCommand)

            // then
            assertThat(CommandHistory.isUndoPossible()).isTrue()
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `successfully add a new command`() {
            // given
            CommandHistory.push(TestReversibleCommand)
            CommandHistory.push(TestReversibleCommand)
            CommandHistory.push(TestReversibleCommand)

            // when
            CommandHistory.clear()

            // then
            assertThat(CommandHistory.isUndoPossible()).isFalse()
            assertThat(CommandHistory.isRedoPossible()).isFalse()
        }
    }

    @Nested
    inner class IsUndoPossibleTests {

        @Test
        fun `returns false for empty history`() {
            // then
            assertThat(CommandHistory.isUndoPossible()).isFalse()
        }

        @Test
        fun `returns true if it's possible to undo previously executed command`() {
            // given
            CommandHistory.push(TestReversibleCommand)

            // when
            val result = CommandHistory.isUndoPossible()

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class IsRedoPossibleTests {

        @Test
        fun `returns false for empty history`() {
            // then
            assertThat(CommandHistory.isRedoPossible()).isFalse()
        }

        @Test
        fun `returns true if it's possible to redo previously undone command`() {
            // given
            val testReversibleCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() { }
            }

            CommandHistory.push(testReversibleCommand)
            CommandHistory.undo()

            // when
            val result = CommandHistory.isRedoPossible()

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class UndoTests {

        @Test
        fun `successfully undo command`() {
            // given
            var commandUndone = false
            val testReversibleCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() { commandUndone = true }
            }

            CommandHistory.push(testReversibleCommand)

            // when
            CommandHistory.undo()

            // then
            assertThat(commandUndone).isTrue()
        }
    }

    @Nested
    inner class RedoTests {

        @Test
        fun `successfully redo command`() {
            // given
            CommandHistory.push(TestReversibleCommand)

            var commandRedone = 0

            val testReversibleCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() { }
                override fun execute() { commandRedone++ }
            }

            CommandHistory.push(testReversibleCommand)
            CommandHistory.undo()

            // when
            CommandHistory.redo()

            // then
            assertThat(commandRedone).isOne()
        }
    }
}