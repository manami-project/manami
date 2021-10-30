package io.github.manamiproject.manami.app.lists.ignorelist

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.commands.Command

internal class CmdRemoveIgnoreListEntry(
    val state: State = InternalState,
    val ignoreListEntry: IgnoreListEntry,
): Command {

    override fun execute(): Boolean {
        if (state.ignoreList().map { it.link }.none { it == ignoreListEntry.link }) {
            return false
        }

        state.removeIgnoreListEntry(ignoreListEntry)
        return true
    }
}