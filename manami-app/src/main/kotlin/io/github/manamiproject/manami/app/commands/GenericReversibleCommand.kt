package io.github.manamiproject.manami.app.commands

import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.logging.LoggerDelegate

internal class GenericReversibleCommand(
    private val state: State = InternalState,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
    private val command: Command,
) : ReversibleCommand {

    private var snapshot: StatefulSnapshot = Uninitialized
    private var hasBeenPushedToCommandHistory: Boolean = false

    override fun undo() {
        when(snapshot) {
            Uninitialized -> throw IllegalStateException("Cannot undo command which hasn't been executed")
            is Initialized -> state.restore((snapshot as Initialized).snapshot)
        }
    }

    override fun execute(): Boolean {
        val unsavedSnapshot = Initialized(state.createSnapshot())

        val successfullyExecuted = command.execute()

        if (!successfullyExecuted) {
            log.warn { "Command wasn't executed successfully." }
            return successfullyExecuted
        }

        snapshot = unsavedSnapshot

        if (!hasBeenPushedToCommandHistory) {
            commandHistory.push(this)
            hasBeenPushedToCommandHistory = true
        }

        return successfullyExecuted
    }

    companion object {
        private val log by LoggerDelegate()
    }
}

private sealed class StatefulSnapshot
private data object Uninitialized : StatefulSnapshot()
private data class Initialized(val snapshot: Snapshot) : StatefulSnapshot()