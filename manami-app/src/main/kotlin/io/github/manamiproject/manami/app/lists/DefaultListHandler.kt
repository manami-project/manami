package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.lists.watchlist.CmdAddWatchListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.GenericReversibleCommand
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI

internal class DefaultListHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, Anime?> = Caches.animeCache,
): ListHandler {

    override fun addWatchListEntry(uri: URI) {
        val anime = cache.fetch(uri) ?: throw IllegalStateException("Unable to retrieve anime for [$uri]")

        GenericReversibleCommand(
            command = CmdAddWatchListEntry(
                state = state,
                watchListEntry = WatchListEntry(anime),
            )
        ).execute()
    }

    override fun watchList(): Set<WatchListEntry> = state.watchList()

    override fun ignoreList(): Set<IgnoreListEntry> = state.ignoreList()
}