package io.github.manamiproject.manami.app.migration

import io.github.manamiproject.manami.app.commands.Command
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State

internal class CmdRemoveUnmappedMigrationEntries(
    private val state: State = InternalState,
    private val animeListEntriesWithoutMapping: Collection<AnimeListEntry>,
    private val watchListEntriesWithoutMapping: Collection<WatchListEntry>,
    private val ignoreListEntriesWithoutMapping: Collection<IgnoreListEntry>,
): Command {

    override fun execute(): Boolean {
        animeListEntriesWithoutMapping.forEach {
            state.removeAnimeListEntry(it)
        }

        watchListEntriesWithoutMapping.forEach {
            state.removeWatchListEntry(it)
        }

        ignoreListEntriesWithoutMapping.forEach {
            state.removeIgnoreListEntry(it)
        }

        return true
    }
}