package io.github.manamiproject.manami.app.state.commands

internal interface ReversibleCommand : Command {

    fun undo()
}