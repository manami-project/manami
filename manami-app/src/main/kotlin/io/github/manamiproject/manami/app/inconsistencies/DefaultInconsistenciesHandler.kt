package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.file.FileOpenedEvent
import io.github.manamiproject.manami.app.inconsistencies.deadentries.CmdFixDeadEntries
import io.github.manamiproject.manami.app.inconsistencies.deadentries.DeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.deadentries.DeadEntriesInconsistencyHandler
import io.github.manamiproject.manami.app.inconsistencies.metadata.CmdFixMetaData
import io.github.manamiproject.manami.app.inconsistencies.metadata.MetaDataDiff
import io.github.manamiproject.manami.app.inconsistencies.metadata.MetaDataInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.metadata.MetaDataInconsistencyHandler
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.GenericReversibleCommand
import io.github.manamiproject.manami.app.state.commands.history.CommandHistory
import io.github.manamiproject.manami.app.state.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.manami.app.state.events.Subscribe
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI

internal class DefaultInconsistenciesHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = Caches.animeCache,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
    private val metaDataInconsistencyHandler: InconsistencyHandler<MetaDataInconsistenciesResult> = MetaDataInconsistencyHandler(state, cache),
    private val deadEntriesInconsistencyHandler: InconsistencyHandler<DeadEntriesInconsistenciesResult> = DeadEntriesInconsistencyHandler(state, cache),
    private val eventBus: EventBus = SimpleEventBus,
) : InconsistenciesHandler {

    private val watchListMetaDataInconsistencies = mutableListOf<MetaDataDiff<WatchListEntry>>()
    private val ignoreListMetaDataInconsistencies = mutableListOf<MetaDataDiff<IgnoreListEntry>>()

    private val watchListDeadEntriesInconsistencies = mutableListOf<WatchListEntry>()
    private val ignoreListDeadEntriesInconsistencies = mutableListOf<IgnoreListEntry>()

    init {
        eventBus.subscribe(this)
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun subscribe(e: FileOpenedEvent) = reset()

    override fun findInconsistencies(config: InconsistenciesSearchConfig) {
        reset()

        val metaDataWorkload = metaDataInconsistencyHandler.calculateWorkload().takeIf { config.checkMetaData } ?: 0
        val deadEntriesWorkload = deadEntriesInconsistencyHandler.calculateWorkload().takeIf { config.checkDeadEntries } ?: 0
        val workload = metaDataWorkload + deadEntriesWorkload

        val metaDataInconsistencyResult = metaDataInconsistencyHandler.execute()
        watchListMetaDataInconsistencies.addAll(metaDataInconsistencyResult.watchListResults)
        ignoreListMetaDataInconsistencies.addAll(metaDataInconsistencyResult.ignoreListResults)
        eventBus.post(InconsistenciesProgressEvent(metaDataWorkload, workload))

        val deadEntriesInconsistencyResult = deadEntriesInconsistencyHandler.execute()
        watchListDeadEntriesInconsistencies.addAll(deadEntriesInconsistencyResult.watchListResults)
        ignoreListDeadEntriesInconsistencies.addAll(deadEntriesInconsistencyResult.ignoreListResults)
        eventBus.post(InconsistenciesProgressEvent(metaDataWorkload, workload))
    }

    override fun fixMetaDataInconsistencies() {
        if (watchListMetaDataInconsistencies.isEmpty() && ignoreListMetaDataInconsistencies.isEmpty()) {
            return
        }

        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdFixMetaData(
                state = state,
                diffWatchList = watchListMetaDataInconsistencies,
                diffIgnoreList = ignoreListMetaDataInconsistencies,
            )
        ).execute()

        watchListMetaDataInconsistencies.clear()
        ignoreListMetaDataInconsistencies.clear()
    }

    override fun fixDeadEntryInconsistencies() {
        if (watchListDeadEntriesInconsistencies.isEmpty() && ignoreListDeadEntriesInconsistencies.isEmpty()) {
            return
        }

        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdFixDeadEntries(
                state = state,
                removeWatchList = watchListDeadEntriesInconsistencies,
                removeIgnoreList = ignoreListDeadEntriesInconsistencies,
            )
        ).execute()

        watchListDeadEntriesInconsistencies.clear()
        ignoreListDeadEntriesInconsistencies.clear()
    }

    private fun reset() {
        watchListMetaDataInconsistencies.clear()
        ignoreListMetaDataInconsistencies.clear()
        watchListDeadEntriesInconsistencies.clear()
        ignoreListDeadEntriesInconsistencies.clear()
    }
}