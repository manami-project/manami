package io.github.manamiproject.manami.app.inconsistencies.animelist.episodes

import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.inconsistencies.InconsistencyHandler
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.flow.update
import kotlin.io.path.listDirectoryEntries

internal class AnimeListEpisodesInconsistenciesHandler(
    private val state: State = InternalState,
    private val eventBus: EventBus = CoroutinesFlowEventBus,
) : InconsistencyHandler<List<EpisodeDiff>> {

    override suspend fun execute(): List<EpisodeDiff> {
        log.info { "Starting check for differing episodes in AnimeList." }

        val results = state.animeList()
            .filter { it.link is Link }
            .map { it to fetchNumberOfEpisodes(it) }
            .filter { it.first.episodes != it.second }
            .map {
                EpisodeDiff(
                    animeListEntry = it.first,
                    numberOfFiles = it.second,
                )
            }
            .toList()

        eventBus.inconsistenciesState.update { current ->
            current.copy(animeListEpisodesInconsistencies = results)
        }

        log.info { "Finished check for differing episodes in AnimeList." }

        return results
    }

    private fun fetchNumberOfEpisodes(entry: AnimeListEntry): Int {
        val folder = (state.openedFile() as CurrentFile).regularFile.parent.resolve(entry.location.toString())

        return folder.listDirectoryEntries()
            .asSequence()
            .filter { it.regularFileExists() }
            .filterNot { it.fileName.toString().startsWith('.') }
            .count()
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [AnimeListEpisodesInconsistenciesHandler]
         * @since 4.0.0
         */
        val instance: AnimeListEpisodesInconsistenciesHandler by lazy { AnimeListEpisodesInconsistenciesHandler() }
    }
}