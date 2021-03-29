package io.github.manamiproject.manami.app.lists.watchlist

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command

internal class CmdRemoveWatchListEntry(
    val state: State = InternalState,
    val watchListEntry: WatchListEntry,
) : Command {

    override fun execute(): Boolean {
        if (state.watchList().map { it.link }.none { it == watchListEntry.link }) {
            return false
        }

        state.removeWatchListEntry(watchListEntry)
        return true
    }
}