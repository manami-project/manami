package io.github.manamiproject.manami.app.inconsistencies.lists.deadentries

import io.github.manamiproject.manami.app.commands.Command
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State

internal class CmdFixDeadEntries(
    private val state: State,
    private val removeWatchList: List<WatchListEntry> = emptyList(),
    private val removeIgnoreList: List<IgnoreListEntry> = emptyList(),
) : Command {

    override fun execute(): Boolean {
        removeWatchList.forEach {
            state.removeWatchListEntry(it)
        }

        removeIgnoreList.forEach {
            state.removeIgnoreListEntry(it)
        }

        return true
    }
}