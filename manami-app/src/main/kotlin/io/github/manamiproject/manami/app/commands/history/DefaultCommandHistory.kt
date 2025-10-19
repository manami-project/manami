package io.github.manamiproject.manami.app.commands.history

import io.github.manamiproject.manami.app.commands.ReversibleCommand
import io.github.manamiproject.manami.app.events.SimpleEventBus

internal object DefaultCommandHistory : CommandHistory {

    private val commandHistory: EventStream<ReversibleCommand> = SimpleEventStream()
    private var lastSavedCommand: ReversibleCommand = NoOpCommand

    override fun push(command: ReversibleCommand) {
        commandHistory.add(command)
        SimpleEventBus.post(FileSavedStatusChangedEvent(isSaved())) // TODO 4.0.0: Migrate
        SimpleEventBus.post(UndoRedoStatusEvent(isUndoPossible(), isRedoPossible())) // TODO 4.0.0: Migrate
    }

    override fun isUndoPossible(): Boolean = commandHistory.hasPrevious()

    override fun undo() {
        if (isUndoPossible()) {
            commandHistory.element().undo()
            commandHistory.previous()
            SimpleEventBus.post(FileSavedStatusChangedEvent(isSaved())) // TODO 4.0.0: Migrate
            SimpleEventBus.post(UndoRedoStatusEvent(isUndoPossible(), isRedoPossible())) // TODO 4.0.0: Migrate
        }
    }

    override fun isRedoPossible(): Boolean = commandHistory.hasNext()

    override fun redo() {
        if (isRedoPossible()) {
            commandHistory.next()
            commandHistory.element().execute()
            SimpleEventBus.post(FileSavedStatusChangedEvent(isSaved())) // TODO 4.0.0: Migrate
            SimpleEventBus.post(UndoRedoStatusEvent(isUndoPossible(), isRedoPossible())) // TODO 4.0.0: Migrate
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
            SimpleEventBus.post(FileSavedStatusChangedEvent(isSaved())) // TODO 4.0.0: Migrate
            SimpleEventBus.post(UndoRedoStatusEvent(isUndoPossible(), isRedoPossible())) // TODO 4.0.0: Migrate
        }
    }

    override fun clear() {
        commandHistory.clear()
        SimpleEventBus.post(FileSavedStatusChangedEvent(isSaved())) // TODO 4.0.0: Migrate
        SimpleEventBus.post(UndoRedoStatusEvent(isUndoPossible(), isRedoPossible())) // TODO 4.0.0: Migrate
    }
}

private object NoOpCommand : ReversibleCommand {
    override fun undo() {}
    override fun execute() = true
}