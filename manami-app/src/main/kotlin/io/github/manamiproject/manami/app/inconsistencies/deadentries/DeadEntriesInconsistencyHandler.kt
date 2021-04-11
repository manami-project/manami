package io.github.manamiproject.manami.app.inconsistencies.deadentries

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.Empty
import io.github.manamiproject.manami.app.inconsistencies.InconsistencyHandler
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI

internal class DeadEntriesInconsistencyHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = Caches.animeCache,
): InconsistencyHandler<DeadEntriesInconsistenciesResult> {

    override fun calculateWorkload(): Int = state.watchList().size + state.ignoreList().size

    override fun execute(progressUpdate: (Int) -> Unit): DeadEntriesInconsistenciesResult {
        var progress = 0

        val watchListResults: List<WatchListEntry> = state.watchList()
            .asSequence()
            .map {
                progressUpdate.invoke(++progress)
                it
            }
            .map { watchListEntry -> watchListEntry to cache.fetch(watchListEntry.link.uri) }
            .filter { it.second is Empty }
            .map { it.first }
            .toList()

        val ignoreListResults: List<IgnoreListEntry> = state.ignoreList()
            .asSequence()
            .map {
                progressUpdate.invoke(++progress)
                it
            }
            .map { ignoreListEntry -> ignoreListEntry to cache.fetch(ignoreListEntry.link.uri) }
            .filter { it.second is Empty }
            .map { it.first }
            .toList()

        return DeadEntriesInconsistenciesResult(
            watchListResults = watchListResults,
            ignoreListResults = ignoreListResults,
        )
    }
}