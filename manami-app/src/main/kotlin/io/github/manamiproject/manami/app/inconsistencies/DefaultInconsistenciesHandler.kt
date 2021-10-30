package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.file.FileOpenedEvent
import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataDiff
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.CmdFixDeadEntries
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistencyHandler
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.CmdFixMetaData
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistenciesResultEvent
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistencyHandler
import io.github.manamiproject.manami.app.lists.animelist.CmdReplaceAnimeListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.commands.GenericReversibleCommand
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.SimpleEventBus
import io.github.manamiproject.manami.app.events.Subscribe
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI

internal class DefaultInconsistenciesHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = Caches.animeCache,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
    private val inconsistencyHandlers: List<InconsistencyHandler<*>> = listOf(
        AnimeListMetaDataInconsistenciesHandler(state, cache),
        AnimeListDeadEntriesInconsistenciesHandler(state, cache),
        MetaDataInconsistencyHandler(state, cache),
        DeadEntriesInconsistencyHandler(state, cache),
    ),
    private val eventBus: EventBus = SimpleEventBus,
) : InconsistenciesHandler {

    private val inconsistencyResults = mutableListOf<Any>()

    init {
        eventBus.subscribe(this)
    }

    @Suppress("UNUSED_PARAMETER")
    @Subscribe(FileOpenedEvent::class)
    fun subscribe(e: FileOpenedEvent) = inconsistencyResults.clear()

    override fun findInconsistencies(config: InconsistenciesSearchConfig) {
        inconsistencyResults.clear()

        var totalWorkload = 0

        inconsistencyHandlers.filter { it.isExecutable(config) }
            .map { it to it.calculateWorkload() }
            .filter { it.second > 0 }
            .map {
                totalWorkload += it.second
                it.first
            }
            .forEach {
                val result = it.execute { progress ->
                    eventBus.post(InconsistenciesProgressEvent(progress, totalWorkload))
                }!!
                inconsistencyResults.add(result)
                notifyResults(result)
            }

        eventBus.post(InconsistenciesCheckFinishedEvent)
    }

    private fun notifyResults(result: Any) {
        when(result) {
            is MetaDataInconsistenciesResult -> {
                val numberOfEntries = result.watchListResults.size + result.ignoreListResults.size
                if (numberOfEntries > 0) {
                    eventBus.post(MetaDataInconsistenciesResultEvent(numberOfEntries))
                }
            }
            is DeadEntriesInconsistenciesResult -> {
                val numberOfEntries = result.watchListResults.size + result.ignoreListResults.size
                if (numberOfEntries > 0) {
                    eventBus.post(DeadEntriesInconsistenciesResultEvent(numberOfEntries))
                }
            }
            is AnimeListMetaDataInconsistenciesResult -> {
                result.entries.forEach {
                    eventBus.post(AnimeListMetaDataInconsistenciesResultEvent(it))
                }
            }
            is AnimeListDeadEntriesInconsistenciesResult -> {
                if (result.entries.isNotEmpty()) {
                    eventBus.post(AnimeListDeadEntriesInconsistenciesResultEvent(result.entries))
                }
            }
        }
    }

    override fun fixMetaDataInconsistencies() {
        val result = inconsistencyResults.find { it is MetaDataInconsistenciesResult }?.let { it as MetaDataInconsistenciesResult } ?: return

        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdFixMetaData(
                state = state,
                diffWatchList = result.watchListResults,
                diffIgnoreList = result.ignoreListResults,
            )
        ).execute()
    }

    override fun fixDeadEntryInconsistencies() {
        val result = inconsistencyResults.find { it is DeadEntriesInconsistenciesResult }?.let { it as DeadEntriesInconsistenciesResult } ?: return

        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdFixDeadEntries(
                state = state,
                removeWatchList = result.watchListResults,
                removeIgnoreList = result.ignoreListResults,
            )
        ).execute()
    }

    override fun fixAnimeListEntryMetaDataInconsistencies(diff: AnimeListMetaDataDiff) {
        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdReplaceAnimeListEntry(
                state = state,
                currentEntry = diff.currentEntry,
                replacementEntry = diff.replacementEntry,
            )
        ).execute()
    }
}