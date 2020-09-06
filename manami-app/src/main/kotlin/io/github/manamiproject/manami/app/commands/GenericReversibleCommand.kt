package io.github.manamiproject.manami.app.commands

import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.History
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.snapshot.Snapshot

internal class GenericReversibleCommand(
        private val state: State = InternalState,
        private val commandHistory: History = CommandHistory,
        private val command: Command,
) : ReversibleCommand {

    private var snapshot: StatefulSnapshot = Uninitialized

    override fun undo() {
        when(snapshot) {
            Uninitialized -> throw IllegalStateException("Cannot undo command which hasn't been executed")
            is Initialized -> state.restore((snapshot as Initialized).snapshot)
        }
    }

    override fun execute() {
        snapshot = Initialized(state.createSnapshot())
        command.execute()
        commandHistory.push(this)
    }
}

private sealed class StatefulSnapshot
private object Uninitialized : StatefulSnapshot()
private data class Initialized(val snapshot: Snapshot) : StatefulSnapshot()