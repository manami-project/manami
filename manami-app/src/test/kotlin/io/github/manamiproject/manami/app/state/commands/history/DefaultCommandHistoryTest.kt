package io.github.manamiproject.manami.app.state.commands.history

import io.github.manamiproject.manami.app.state.commands.ReversibleCommand
import io.github.manamiproject.manami.app.state.commands.TestReversibleCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DefaultCommandHistoryTest {

    @AfterEach
    fun afterEach() {
        DefaultCommandHistory.clear()
    }

    @Nested
    inner class PushTests {

        @Test
        fun `successfully add a new command`() {
            // when
            DefaultCommandHistory.push(TestReversibleCommand)

            // then
            assertThat(DefaultCommandHistory.isUndoPossible()).isTrue()
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `successfully add a new command`() {
            // given
            DefaultCommandHistory.push(TestReversibleCommand)
            DefaultCommandHistory.push(TestReversibleCommand)
            DefaultCommandHistory.push(TestReversibleCommand)

            // when
            DefaultCommandHistory.clear()

            // then
            assertThat(DefaultCommandHistory.isUndoPossible()).isFalse()
            assertThat(DefaultCommandHistory.isRedoPossible()).isFalse()
        }
    }

    @Nested
    inner class IsUndoPossibleTests {

        @Test
        fun `returns false for empty command history`() {
            // then
            assertThat(DefaultCommandHistory.isUndoPossible()).isFalse()
        }

        @Test
        fun `returns true if it's possible to undo previously executed command`() {
            // given
            DefaultCommandHistory.push(TestReversibleCommand)

            // when
            val result = DefaultCommandHistory.isUndoPossible()

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class IsRedoPossibleTests {

        @Test
        fun `returns false for empty command history`() {
            // then
            assertThat(DefaultCommandHistory.isRedoPossible()).isFalse()
        }

        @Test
        fun `returns true if it's possible to redo previously undone command`() {
            // given
            val testReversibleCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() { }
            }

            DefaultCommandHistory.push(testReversibleCommand)
            DefaultCommandHistory.undo()

            // when
            val result = DefaultCommandHistory.isRedoPossible()

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

            DefaultCommandHistory.push(testReversibleCommand)

            // when
            DefaultCommandHistory.undo()

            // then
            assertThat(commandUndone).isTrue()
        }
    }

    @Nested
    inner class RedoTests {

        @Test
        fun `successfully redo command`() {
            // given
            DefaultCommandHistory.push(TestReversibleCommand)

            var commandRedone = 0

            val testReversibleCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() { }
                override fun execute() { commandRedone++ }
            }

            DefaultCommandHistory.push(testReversibleCommand)
            DefaultCommandHistory.undo()

            // when
            DefaultCommandHistory.redo()

            // then
            assertThat(commandRedone).isOne()
        }
    }

    @Nested
    inner class IsUnsavedTests {

        @Test
        fun `returns false if the command history is empty`() {
            // when
            val result = DefaultCommandHistory.isUnsaved()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns true if the command history contains an element and is not saved`() {
            // given
            val testCommand = object: ReversibleCommand by TestReversibleCommand { }
            DefaultCommandHistory.push(testCommand)

            // when
            val result = DefaultCommandHistory.isUnsaved()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns false if the first command is undone`() {
            // given
            val testCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() {}
            }

            DefaultCommandHistory.push(testCommand)
            DefaultCommandHistory.undo()

            // when
            val result = DefaultCommandHistory.isUnsaved()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns false if the given state has been saved`() {
            // given
            val testCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() {}
            }

            DefaultCommandHistory.push(testCommand)
            DefaultCommandHistory.push(testCommand)
            DefaultCommandHistory.undo()
            DefaultCommandHistory.save()

            // when
            val result = DefaultCommandHistory.isUnsaved()

            // then
            assertThat(result).isFalse()
        }
    }

    @Nested
    inner class IsSavedTests {

        @Test
        fun `returns true if the command history is empty`() {
            // when
            val result = DefaultCommandHistory.isSaved()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns false if the command history contains an element and is not saved`() {
            // given
            val testCommand = object: ReversibleCommand by TestReversibleCommand { }
            DefaultCommandHistory.push(testCommand)

            // when
            val result = DefaultCommandHistory.isSaved()

            // then
            assertThat(result).isFalse()
        }

        @Test
        fun `returns true if the first command is undone`() {
            // given
            val testCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() {}
            }

            DefaultCommandHistory.push(testCommand)
            DefaultCommandHistory.undo()

            // when
            val result = DefaultCommandHistory.isSaved()

            // then
            assertThat(result).isTrue()
        }

        @Test
        fun `returns true if the given state has been saved`() {
            // given
            val testCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() {}
            }

            DefaultCommandHistory.push(testCommand)
            DefaultCommandHistory.push(testCommand)
            DefaultCommandHistory.undo()
            DefaultCommandHistory.save()

            // when
            val result = DefaultCommandHistory.isSaved()

            // then
            assertThat(result).isTrue()
        }
    }

    @Nested
    inner class SaveTests {

        @Test
        fun `when called on initial element remove command and set initial NoOpCommand`() {
            // given
            val testCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() { }
            }

            DefaultCommandHistory.push(testCommand)
            DefaultCommandHistory.undo()

            // when
            DefaultCommandHistory.save()

            // then
            assertThat(DefaultCommandHistory.isRedoPossible()).isFalse()
            assertThat(DefaultCommandHistory.isUndoPossible()).isFalse()
            assertThat(DefaultCommandHistory.isSaved()).isTrue()
        }
    }
}