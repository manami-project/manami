package io.github.manamiproject.manami.app.commands

import io.github.manamiproject.manami.app.commands.history.History
import io.github.manamiproject.modb.test.shouldNotBeInvoked

internal object TestReversibleCommand : ReversibleCommand {
    override fun undo() = shouldNotBeInvoked()
    override fun execute() = shouldNotBeInvoked()
}

internal object TestHistory : History {
    override fun push(command: ReversibleCommand) = shouldNotBeInvoked()
    override fun isUndoPossible(): Boolean = shouldNotBeInvoked()
    override fun undo() = shouldNotBeInvoked()
    override fun isRedoPossible(): Boolean = shouldNotBeInvoked()
    override fun redo() = shouldNotBeInvoked()
    override fun clear() = shouldNotBeInvoked()
}