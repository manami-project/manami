package io.github.manamiproject.manami.app.migration

import io.github.manamiproject.manami.app.commands.Command
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State

internal class CmdMigrateEntries(
    private val state: State = InternalState,
    private val animeListMappings: Map<AnimeListEntry, Link>,
    private val watchListMappings: Map<WatchListEntry, Link>,
    private val ignoreListMappings: Map<IgnoreListEntry, Link>,
): Command {

    override fun execute(): Boolean {
        if (animeListMappings.isNotEmpty()) {
            animeListMappings.forEach {
                state.addAllAnimeListEntries(listOf(it.key.copy(link = it.value)))
                state.removeAnimeListEntry(it.key)
            }
        }

        if (watchListMappings.isNotEmpty()) {
            watchListMappings.forEach {
                state.addAllWatchListEntries(listOf( it.key.copy(link = it.value)))
                state.removeWatchListEntry(it.key)
            }
        }

        if (ignoreListMappings.isNotEmpty()) {
            ignoreListMappings.forEach {
                state.addAllIgnoreListEntries(listOf(it.key.copy(link = it.value)))
                state.removeIgnoreListEntry(it.key)
            }
        }

        return true
    }
}