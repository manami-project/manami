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
            state.addAllAnimeListEntries(
                animeListMappings.map {
                    it.key.copy(link = it.value)
                }
            )

            animeListMappings.forEach {
                state.removeAnimeListEntry(it.key)
            }
        }

        if (watchListMappings.isNotEmpty()) {
            state.addAllWatchListEntries(
                watchListMappings.map {
                    it.key.copy(link = it.value)
                }
            )

            watchListMappings.forEach {
                state.removeWatchListEntry(it.key)
            }
        }

        if (ignoreListMappings.isNotEmpty()) {
            state.addAllIgnoreListEntries(
                ignoreListMappings.map {
                    it.key.copy(link = it.value)
                }
            )

            ignoreListMappings.forEach {
                state.removeIgnoreListEntry(it.key)
            }
        }

        return true
    }
}