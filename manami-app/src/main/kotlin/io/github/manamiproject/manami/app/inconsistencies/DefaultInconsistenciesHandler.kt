package io.github.manamiproject.manami.app.inconsistencies

import io.github.manamiproject.manami.app.commands.GenericReversibleCommand
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.InconsistenciesState
import io.github.manamiproject.manami.app.inconsistencies.animelist.deadentries.AnimeListDeadEntriesInconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.AnimeListEpisodesInconsistenciesHandler
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataDiff
import io.github.manamiproject.manami.app.inconsistencies.animelist.metadata.AnimeListMetaDataInconsistenciesHandler
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.animelist.CmdReplaceAnimeListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class DefaultInconsistenciesHandler(
    private val state: State = InternalState,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
    private val inconsistencyHandlers: List<InconsistencyHandler<*>> = listOf(
        AnimeListMetaDataInconsistenciesHandler.instance,
        AnimeListDeadEntriesInconsistenciesHandler.instance,
        AnimeListEpisodesInconsistenciesHandler.instance,
    ),
    private val eventBus: EventBus = CoroutinesFlowEventBus,
) : InconsistenciesHandler {

    override suspend fun findInconsistencies() {
        eventBus.inconsistenciesState.update { InconsistenciesState(isRunning = true) }

        withContext(LIMITED_FS) {
            inconsistencyHandlers.map {
                launch { it.execute() }
            }.joinAll()
        }

        eventBus.inconsistenciesState.update { current ->
            current.copy(
                isRunning = false,
            )
        }
    }

    override fun setForEdit(diff: AnimeListMetaDataDiff) {
        eventBus.fixAnimeListInconsistencyModificationState.update { diff }
    }

    override fun fixAnimeListEntryMetaDataInconsistencies(currentEntry: AnimeListEntry, replacementEntry: AnimeListEntry) {
        if (currentEntry == replacementEntry) return

        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdReplaceAnimeListEntry(
                state = state,
                currentEntry = currentEntry,
                replacementEntry = replacementEntry,
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