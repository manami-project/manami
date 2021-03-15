package io.github.manamiproject.manami.app.lists.watchlist

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command

internal class CmdAddWatchListEntry(
    private val state: State = InternalState,
    private val watchListEntry: WatchListEntry,
): Command {

    override fun execute() {
        state.addAllWatchListEntries(setOf(watchListEntry))
    }
}