package io.github.manamiproject.manami.app.commands.history

import io.github.manamiproject.manami.app.commands.ReversibleCommand

internal object CommandHistory : History {

    private val history: EventStream<ReversibleCommand> = SimpleEventStream()

    override fun push(command: ReversibleCommand) = history.add(command)

    override fun isUndoPossible(): Boolean = history.hasPrevious()

    override fun undo() {
        if (isUndoPossible()) {
            history.element().undo()
            history.previous()
        }
    }

    override fun isRedoPossible(): Boolean = history.hasNext()

    override fun redo() {
        if (isRedoPossible()) {
            history.next()
            history.element().execute()
        }
    }

    override fun clear() =  history.clear()
}