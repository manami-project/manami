package io.github.manamiproject.manami.app.state.commands.history

import io.github.manamiproject.manami.app.state.commands.ReversibleCommand

internal interface CommandHistory {

    fun push(command: ReversibleCommand)

    fun isUndoPossible(): Boolean

    fun undo()

    fun isRedoPossible(): Boolean

    fun redo()

    fun isSaved(): Boolean

    fun isUnsaved(): Boolean

    fun save()

    fun clear()
}
