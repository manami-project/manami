package io.github.manamiproject.manami.app.commands.history

import io.github.manamiproject.manami.app.commands.ReversibleCommand
import io.github.manamiproject.manami.app.commands.TestReversibleCommand
import io.github.manamiproject.manami.app.events.SimpleEventBus
import io.github.manamiproject.manami.app.events.Subscribe
import kotlinx.coroutines.delay
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep

internal class DefaultCommandHistoryTest {

    private var testEventSubscriber = TestEventSubscriber()

    @BeforeEach
    fun before() {
        SimpleEventBus.subscribe(testEventSubscriber)
    }

    @AfterEach
    fun afterEach() {
        DefaultCommandHistory.clear()
        testEventSubscriber.fileSavedEvents.clear()
        testEventSubscriber.undoRedoStatusEvents.clear()
        SimpleEventBus.unsubscribe(testEventSubscriber)
    }

    @Nested
    inner class PushTests {

        @Test
        fun `successfully add a new command`() {
            // when
            DefaultCommandHistory.push(TestReversibleCommand)

            // then
            assertThat(DefaultCommandHistory.isUndoPossible()).isTrue()
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents.first()).isFalse()
            assertThat(testEventSubscriber.undoRedoStatusEvents.first()).isEqualTo(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
            )
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
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false, false, false, true)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = false, isRedoPossible = false),
            )
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
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
            )
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
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false, true)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = false, isRedoPossible = true),
            )
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
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false, true)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = false, isRedoPossible = true),
            )
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
                override fun execute(): Boolean {
                    commandRedone++
                    return true
                }
            }

            DefaultCommandHistory.push(testReversibleCommand)
            DefaultCommandHistory.undo()

            // when
            DefaultCommandHistory.redo()

            // then
            assertThat(commandRedone).isOne()
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false, false, false, false)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = true),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
            )
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
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
            )
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
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false, true)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = false, isRedoPossible = true),
            )
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
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false, false, false, true)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = true),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
            )
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
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
            )
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
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false, true)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = false, isRedoPossible = true),
            )
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
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false, false, false, true)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = true),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
            )
        }
    }

    @Nested
    inner class SaveTests {

        @Test
        fun `when called on initial element after undoing a command, the following commands are not cropped`() {
            // given
            val testCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() { }
            }

            DefaultCommandHistory.push(testCommand)
            DefaultCommandHistory.undo()

            // when
            DefaultCommandHistory.save()

            // then
            assertThat(DefaultCommandHistory.isRedoPossible()).isTrue()
            assertThat(DefaultCommandHistory.isUndoPossible()).isFalse()
            assertThat(DefaultCommandHistory.isSaved()).isTrue()
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false, true)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = false, isRedoPossible = true),
            )
        }

        @Test
        fun `crop following commands when save is called`() {
            // given
            val testCommand = object: ReversibleCommand by TestReversibleCommand {
                override fun undo() { }
            }

            DefaultCommandHistory.push(testCommand)
            DefaultCommandHistory.push(testCommand)
            DefaultCommandHistory.undo()

            // when
            DefaultCommandHistory.save()

            // then
            assertThat(DefaultCommandHistory.isRedoPossible()).isFalse()
            assertThat(DefaultCommandHistory.isUndoPossible()).isTrue()
            assertThat(DefaultCommandHistory.isSaved()).isTrue()
            sleep(1000)
            assertThat(testEventSubscriber.fileSavedEvents).containsExactly(false, false, false, true)
            assertThat(testEventSubscriber.undoRedoStatusEvents).containsExactly(
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = true),
                UndoRedoStatusEvent(isUndoPossible = true, isRedoPossible = false),
            )
        }
    }
}

internal class TestEventSubscriber {

    val fileSavedEvents = mutableListOf<Boolean>()
    val undoRedoStatusEvents = mutableListOf<UndoRedoStatusEvent>()

    @Subscribe(FileSavedStatusChangedEvent::class)
    fun receive(event: FileSavedStatusChangedEvent) {
        fileSavedEvents.add(event.isFileSaved)
    }

    @Subscribe(UndoRedoStatusEvent::class)
    fun receive(event: UndoRedoStatusEvent) {
        undoRedoStatusEvents.add(event)
    }
}