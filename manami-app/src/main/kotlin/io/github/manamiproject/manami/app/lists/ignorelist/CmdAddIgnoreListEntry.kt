package io.github.manamiproject.manami.app.lists.ignorelist

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command

internal class CmdAddIgnoreListEntry(
    private val state: State = InternalState,
    private val ignoreListEntry: IgnoreListEntry,
): Command {

    override fun execute() {
        state.addAllIgnoreListEntries(setOf(ignoreListEntry))
    }
}