package io.github.manamiproject.manami.app.inconsistencies.lists.deadentries

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.app.inconsistencies.InconsistencyHandler
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.anime.Anime
import java.net.URI

internal class DeadEntriesInconsistencyHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = DefaultAnimeCache.instance,
): InconsistencyHandler<DeadEntriesInconsistenciesResult> {

    override fun calculateWorkload(): Int = state.watchList().size + state.ignoreList().size

    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = config.checkDeadEntries

    override fun execute(progressUpdate: (Int) -> Unit): DeadEntriesInconsistenciesResult {
        log.info { "Starting check for dead entries in WatchList and IgnoreList." }

        var progress = 0

        val watchListResults: List<WatchListEntry> = state.watchList()
            .asSequence()
            .map {
                progressUpdate.invoke(++progress)
                it
            }
            .map { watchListEntry -> watchListEntry to cache.fetch(watchListEntry.link.uri) }
            .filter { it.second is DeadEntry }
            .map { it.first }
            .toList()

        val ignoreListResults: List<IgnoreListEntry> = state.ignoreList()
            .asSequence()
            .map {
                progressUpdate.invoke(++progress)
                it
            }
            .map { ignoreListEntry -> ignoreListEntry to cache.fetch(ignoreListEntry.link.uri) }
            .filter { it.second is DeadEntry }
            .map { it.first }
            .toList()

        log.info { "Finished check for dead entries in WatchList and IgnoreList." }

        return DeadEntriesInconsistenciesResult(
            watchListResults = watchListResults,
            ignoreListResults = ignoreListResults,
        )
    }

    companion object {
        private val log by LoggerDelegate()
    }
}