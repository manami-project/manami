package io.github.manamiproject.manami.app.state

interface StateHandler {

    fun undo()

    fun redo()
}