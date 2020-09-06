package io.github.manamiproject.manami.app.commands

internal interface ReversibleCommand : Command {

    fun undo()
}