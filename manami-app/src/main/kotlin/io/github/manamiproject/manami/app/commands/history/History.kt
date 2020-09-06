package io.github.manamiproject.manami.app.commands.history

import io.github.manamiproject.manami.app.commands.ReversibleCommand

internal interface History {

    fun push(command: ReversibleCommand)

    fun isUndoPossible(): Boolean

    fun undo()

    fun isRedoPossible(): Boolean

    fun redo()

    fun clear()
}
