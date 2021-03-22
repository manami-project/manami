package io.github.manamiproject.manami.app.lists.watchlist

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command

internal class CmdRemoveWatchListEntry(
    val state: State = InternalState,
    val watchListEntry: WatchListEntry,
) : Command {

    override fun execute() {
        state.removeWatchListEntry(watchListEntry)
    }
}