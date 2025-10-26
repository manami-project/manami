package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.commands.GenericReversibleCommand
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.InconsistenciesState
import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.AnimeListEpisodesInconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.AnimeListEpisodesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataDiff
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.CmdFixDeadEntries
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.deadentries.DeadEntriesInconsistencyHandler
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.CmdFixMetaData
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistenciesResult
import io.github.manamiproject.manami.app.inconsistencies.lists.metadata.MetaDataInconsistencyHandler
import io.github.manamiproject.manami.app.lists.animelist.CmdReplaceAnimeListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.anime.Anime
import kotlinx.coroutines.flow.update
import java.net.URI

internal class DefaultInconsistenciesHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = DefaultAnimeCache.instance,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
    private val inconsistencyHandlers: List<InconsistencyHandler<*>> = listOf(
        AnimeListMetaDataInconsistenciesHandler(state, cache),
        AnimeListDeadEntriesInconsistenciesHandler(state, cache),
        AnimeListEpisodesInconsistenciesHandler(state),
        MetaDataInconsistencyHandler(state, cache),
        DeadEntriesInconsistencyHandler(state, cache),
    ),
    private val eventBus: EventBus = CoroutinesFlowEventBus,
) : InconsistenciesHandler {

    override suspend fun findInconsistencies(config: InconsistenciesSearchConfig) {
        eventBus.inconsistenciesState.update { InconsistenciesState(isRunning = true) }

        inconsistencyHandlers.filter { it.isExecutable(config) }
            .filter { it.calculateWorkload() > 0 }
            .map { it.execute() }
            .forEach { notifyResults(it!!) }

        eventBus.inconsistenciesState.update { current ->
            current.copy(
                isRunning = false,
            )
        }
    }

    private fun notifyResults(result: Any) {
        when(result) {
            is MetaDataInconsistenciesResult -> {
                if (result.watchListResults.isNotEmpty() || result.ignoreListResults.isNotEmpty()) {
                    eventBus.inconsistenciesState.update { current ->
                        current.copy(metaDataInconsistencies = result)
                    }
                }
            }
            is DeadEntriesInconsistenciesResult -> {
                if (result.watchListResults.isNotEmpty() || result.ignoreListResults.isNotEmpty()) {
                    eventBus.inconsistenciesState.update { current ->
                        current.copy(deadEntryInconsistencies = result)
                    }
                }
            }
            is AnimeListMetaDataInconsistenciesResult -> {
                if (result.entries.isNotEmpty()) {
                    eventBus.inconsistenciesState.update { current ->
                        current.copy(animeListMetaDataInconsistencies = result)
                    }
                }
            }
            is AnimeListDeadEntriesInconsistenciesResult -> {
                if (result.entries.isNotEmpty()) {
                    eventBus.inconsistenciesState.update { current ->
                        current.copy(animeListDeadEntriesInconsistencies = result)
                    }
                }
            }
            is AnimeListEpisodesInconsistenciesResult -> {
                if (result.entries.isNotEmpty()) {
                    eventBus.inconsistenciesState.update { current ->
                        current.copy(animeListEpisodesInconsistencies = result)
                    }
                }
            }
        }
    }

    override fun fixMetaDataInconsistencies() {
        val currentResult = eventBus.inconsistenciesState.value.metaDataInconsistencies
        if (currentResult.watchListResults.isEmpty() && currentResult.ignoreListResults.isEmpty()) return

        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdFixMetaData(
                state = state,
                diffWatchList = currentResult.watchListResults,
                diffIgnoreList = currentResult.ignoreListResults,
            )
        ).execute()
    }

    override fun fixDeadEntryInconsistencies() {
        val currentResult = eventBus.inconsistenciesState.value.deadEntryInconsistencies
        if (currentResult.watchListResults.isEmpty() && currentResult.ignoreListResults.isEmpty()) return

        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdFixDeadEntries(
                state = state,
                removeWatchList = currentResult.watchListResults,
                removeIgnoreList = currentResult.ignoreListResults,
            )
        ).execute()
    }

    override fun fixAnimeListEntryMetaDataInconsistencies(diff: AnimeListMetaDataDiff) {
        if (diff.currentEntry == diff.replacementEntry) return

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

    companion object {
        /**
         * Singleton of [DefaultInconsistenciesHandler]
         * @since 4.0.0
         */
        val instance: DefaultInconsistenciesHandler by lazy { DefaultInconsistenciesHandler() }
    }
}