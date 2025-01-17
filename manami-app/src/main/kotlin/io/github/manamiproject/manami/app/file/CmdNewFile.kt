package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.commands.Command
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State

internal class CmdNewFile(
    private val state: State = InternalState,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
) : Command {

    override fun execute(): Boolean {
        commandHistory.clear()
        state.closeFile()
        state.clear()
        return true
    }
}