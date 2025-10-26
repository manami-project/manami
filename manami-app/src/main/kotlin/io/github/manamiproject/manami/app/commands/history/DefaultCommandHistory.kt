package io.github.manamiproject.manami.app.commands.history

import io.github.manamiproject.manami.app.commands.ReversibleCommand
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import kotlinx.coroutines.flow.update

internal object DefaultCommandHistory : CommandHistory {

    private val commandHistory: EventStream<ReversibleCommand> = SimpleEventStream()
    private var lastSavedCommand: ReversibleCommand = NoOpCommand

    override fun push(command: ReversibleCommand) {
        commandHistory.add(command)
        CoroutinesFlowEventBus.generalAppState.update { current ->
            current.copy(
                isFileSaved = isSaved(),
                isUndoPossible = isUndoPossible(),
                isRedoPossible = isRedoPossible(),
            )
        }
    }

    override fun isUndoPossible(): Boolean = commandHistory.hasPrevious()

    override fun undo() {
        if (isUndoPossible()) {
            commandHistory.element().undo()
            commandHistory.previous()
            CoroutinesFlowEventBus.generalAppState.update { current ->
                current.copy(
                    isFileSaved = isSaved(),
                    isUndoPossible = isUndoPossible(),
                    isRedoPossible = isRedoPossible(),
                )
            }
        }
    }

    override fun isRedoPossible(): Boolean = commandHistory.hasNext()

    override fun redo() {
        if (isRedoPossible()) {
            commandHistory.next()
            commandHistory.element().execute()
            CoroutinesFlowEventBus.generalAppState.update { current ->
                current.copy(
                    isFileSaved = isSaved(),
                    isUndoPossible = isUndoPossible(),
                    isRedoPossible = isRedoPossible(),
                )
            }
        }
    }

    override fun isSaved(): Boolean = !isUnsaved()

    override fun isUnsaved(): Boolean {
        val isStreamEmpty = !commandHistory.hasPrevious() && !commandHistory.hasNext()
        val isFirstElement = !commandHistory.hasPrevious() && commandHistory.hasNext()

        return if (isStreamEmpty || isFirstElement) {
            false
        } else {
            commandHistory.element() != lastSavedCommand
        }
    }

    override fun save() {
        if (isUnsaved()) {
            lastSavedCommand = if (commandHistory.hasPrevious()) {
                commandHistory.element()
            } else {
                NoOpCommand
            }

            commandHistory.crop()
            CoroutinesFlowEventBus.generalAppState.update { current ->
                current.copy(
                    isFileSaved = isSaved(),
                    isUndoPossible = isUndoPossible(),
                    isRedoPossible = isRedoPossible(),
                )
            }
        }
    }

    override fun clear() {
        commandHistory.clear()
        CoroutinesFlowEventBus.generalAppState.update { current ->
            current.copy(
                isFileSaved = isSaved(),
                isUndoPossible = isUndoPossible(),
                isRedoPossible = isRedoPossible(),
            )
        }
    }
}

private object NoOpCommand : ReversibleCommand {
    override fun undo() {}
    override fun execute() = true
}