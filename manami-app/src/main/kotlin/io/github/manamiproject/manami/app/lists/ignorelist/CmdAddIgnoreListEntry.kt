package io.github.manamiproject.manami.app.lists.ignorelist

import io.github.manamiproject.manami.app.commands.Command
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State

internal class CmdAddIgnoreListEntry(
    private val state: State = InternalState,
    private val ignoreListEntry: IgnoreListEntry,
): Command {

    override fun execute(): Boolean {
        if (state.ignoreList().map { it.link }.any { it == ignoreListEntry.link }) {
            return false
        }

        state.addAllIgnoreListEntries(setOf(ignoreListEntry))
        return true
    }
}