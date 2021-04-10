package io.github.manamiproject.manami.app.inconsistencies.metadata

import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command

internal class CmdFixMetaData(
    private val state: State,
    private val diffWatchList: List<MetaDataDiff<WatchListEntry>> = emptyList(),
    private val diffIgnoreList: List<MetaDataDiff<IgnoreListEntry>> = emptyList(),
): Command {

    override fun execute(): Boolean {
        diffWatchList.forEach {
            state.removeWatchListEntry(it.currentEntry)
        }
        if (diffWatchList.isNotEmpty()) {
            state.addAllWatchListEntries(diffWatchList.map { it.newEntry })
        }

        diffIgnoreList.forEach {
            state.removeIgnoreListEntry(it.currentEntry)
        }
        if (diffIgnoreList.isNotEmpty()) {
            state.addAllIgnoreListEntries(diffIgnoreList.map { it.newEntry })
        }

        return true
    }
}