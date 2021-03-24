package io.github.manamiproject.manami.app.lists.ignorelist

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command

internal class CmdRemoveIgnoreListEntry(
    val state: State = InternalState,
    val ignoreListEntry: IgnoreListEntry,
): Command {

    override fun execute() {
        state.removeIgnoreListEntry(ignoreListEntry)
    }
}