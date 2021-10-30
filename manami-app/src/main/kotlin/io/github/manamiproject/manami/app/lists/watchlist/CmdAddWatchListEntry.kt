package io.github.manamiproject.manami.app.lists.watchlist

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.commands.Command

internal class CmdAddWatchListEntry(
    private val state: State = InternalState,
    private val watchListEntry: WatchListEntry,
): Command {

    override fun execute(): Boolean {
        if (state.watchList().map { it.link }.any { it == watchListEntry.link }) {
            return false
        }

        state.addAllWatchListEntries(setOf(watchListEntry))
        return true
    }
}