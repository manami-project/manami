package io.github.manamiproject.manami.app.state.commands.history

import io.github.manamiproject.manami.app.state.commands.ReversibleCommand
import io.github.manamiproject.manami.app.state.events.SimpleEventBus

internal object DefaultCommandHistory : CommandHistory {

    private val commandHistory: EventStream<ReversibleCommand> = SimpleEventStream()
    private var lastSavedCommand: ReversibleCommand = NoOpCommand

    override fun push(command: ReversibleCommand) {
        commandHistory.add(command)
        SimpleEventBus.post(FileSavedStatusChangedEvent(isSaved()))
        SimpleEventBus.post(UndoRedoStatusEvent(isUndoPossible(), isRedoPossible()))
    }

    override fun isUndoPossible(): Boolean = commandHistory.hasPrevious()

    override fun undo() {
        if (isUndoPossible()) {
            commandHistory.element().undo()
            commandHistory.previous()
            SimpleEventBus.post(FileSavedStatusChangedEvent(isSaved()))
            SimpleEventBus.post(UndoRedoStatusEvent(isUndoPossible(), isRedoPossible()))
        }
    }

    override fun isRedoPossible(): Boolean = commandHistory.hasNext()

    override fun redo() {
        if (isRedoPossible()) {
            commandHistory.next()
            commandHistory.element().execute()
            SimpleEventBus.post(FileSavedStatusChangedEvent(isSaved()))
            SimpleEventBus.post(UndoRedoStatusEvent(isUndoPossible(), isRedoPossible()))
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
            SimpleEventBus.post(FileSavedStatusChangedEvent(isSaved()))
            SimpleEventBus.post(UndoRedoStatusEvent(isUndoPossible(), isRedoPossible()))
        }
    }

    override fun clear() {
        commandHistory.clear()
        SimpleEventBus.post(FileSavedStatusChangedEvent(isSaved()))
        SimpleEventBus.post(UndoRedoStatusEvent(isUndoPossible(), isRedoPossible()))
    }
}

private object NoOpCommand : ReversibleCommand {
    override fun undo() {}
    override fun execute() {}
}