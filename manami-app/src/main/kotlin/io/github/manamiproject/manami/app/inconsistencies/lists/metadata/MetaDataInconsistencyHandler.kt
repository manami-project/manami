package io.github.manamiproject.manami.app.inconsistencies.lists.metadata

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.app.inconsistencies.InconsistencyHandler
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI

internal class MetaDataInconsistencyHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = Caches.animeCache,
) : InconsistencyHandler<MetaDataInconsistenciesResult> {

    override fun calculateWorkload(): Int = state.watchList().size + state.ignoreList().size

    override fun isExecutable(config: InconsistenciesSearchConfig): Boolean = config.checkMetaData

    override fun execute(progressUpdate: (Int) -> Unit): MetaDataInconsistenciesResult {
        var progress = 0

        val watchListResults: List<MetaDataDiff<WatchListEntry>> = state.watchList()
            .asSequence()
            .map {
                progressUpdate.invoke(++progress)
                it
            }
            .map { watchListEntry -> watchListEntry to cache.fetch(watchListEntry.link.uri) }
            .filter { it.second is PresentValue<Anime> }
            .map { it.first to (it.second as PresentValue<Anime>).value }
            .map { it.first to WatchListEntry(it.second) }
            .filter { it.first.link == it.second.link }
            .filter { it.first != it.second }
            .map { MetaDataDiff(currentEntry = it.first, newEntry = it.second) }
            .toList()

        val ignoreListResults: List<MetaDataDiff<IgnoreListEntry>> = state.ignoreList()
            .asSequence()
            .map {
                progressUpdate.invoke(++progress)
                it
            }
            .map { ignoreListEntry -> ignoreListEntry to cache.fetch(ignoreListEntry.link.uri) }
            .filter { it.second is PresentValue<Anime> }
            .map { it.first to (it.second as PresentValue<Anime>).value }
            .map { it.first to IgnoreListEntry(it.second) }
            .filter { it.first.link == it.second.link }
            .filter { it.first != it.second }
            .map { MetaDataDiff(currentEntry = it.first, newEntry = it.second) }
            .toList()

        return MetaDataInconsistenciesResult(
            watchListResults = watchListResults,
            ignoreListResults = ignoreListResults,
        )
    }
}