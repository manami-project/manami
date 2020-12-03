package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.state.commands.Command
import io.github.manamiproject.manami.app.state.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.state.commands.history.CommandHistory
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State

internal class CmdNewFile(
        private val state: State = InternalState,
        private val commandHistory: CommandHistory = DefaultCommandHistory,
) : Command {

    override fun execute() {
        commandHistory.clear()
        state.closeFile()
        state.clear()
    }
}