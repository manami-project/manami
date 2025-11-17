package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.commands.GenericReversibleCommand
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.FindByTitleState
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.animelist.CmdAddAnimeListEntry
import io.github.manamiproject.manami.app.lists.animelist.CmdRemoveAnimeListEntry
import io.github.manamiproject.manami.app.lists.animelist.CmdReplaceAnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.CmdAddIgnoreListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.CmdRemoveIgnoreListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.CmdAddWatchListEntry
import io.github.manamiproject.manami.app.lists.watchlist.CmdRemoveWatchListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.yield
import java.net.URI

internal class DefaultListHandler(
    private val state: State = InternalState,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
    private val cache: AnimeCache = DefaultAnimeCache.instance,
    private val eventBus: EventBus = CoroutinesFlowEventBus,
): ListHandler {

    override fun addAnimeListEntry(entry: AnimeListEntry) {
        if (GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdAddAnimeListEntry(
                state = state,
                animeListEntry = entry,
            )
        ).execute()) {
            eventBus.animeListModificationState.update { current ->
                current.copy(addAnimeEntryData = null)
            }
            eventBus.findRelatedAnimeState.update { current -> current.copy(entries = current.entries.filterNot { it.link == entry.link } ) }
            eventBus.findByTitleState.update { FindByTitleState() }
            eventBus.findSeasonState.update { current -> current.copy(entries = current.entries.filterNot { it.link == entry.link } ) }
            eventBus.findByCriertiaState.update { current -> current.copy(entries = current.entries.filterNot { it.link == entry.link } ) }
            eventBus.findSimilarAnimeState.update { current -> current.copy(entries = current.entries.filterNot { it.link == entry.link } ) }
        }
    }

    override fun animeList(): List<AnimeListEntry> = state.animeList()

    override fun removeAnimeListEntry(entry: AnimeListEntry) {
        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdRemoveAnimeListEntry(
                state = state,
                animeListEntry = entry,
            )
        ).execute()
    }

    override fun replaceAnimeListEntry(current: AnimeListEntry, replacement: AnimeListEntry) {
        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdReplaceAnimeListEntry(
                state = state,
                currentEntry = current,
                replacementEntry = replacement,
            )
        ).execute()
    }

    override suspend fun findAnimeDetailsForAddingAnEntry(uri: URI) {
        eventBus.animeListModificationState.update { current ->
            current.copy(isAddAnimeEntryDataRunning = true)
        }
        yield()

        val entry = cache.fetch(uri)
        if (entry is PresentValue) {
            eventBus.animeListModificationState.update { current ->
                current.copy(
                    isAddAnimeEntryDataRunning = false,
                    addAnimeEntryData = entry.value,
                )
            }
        } else {
            eventBus.animeListModificationState.update { current ->
                current.copy(isAddAnimeEntryDataRunning = false)
            }
        }
    }

    override fun prepareAnimeListEntryForEditingAnEntry(entry: AnimeListEntry) {
        eventBus.animeListModificationState.update { current ->
            current.copy(
                editAnimeListEntryData = entry,
            )
        }
    }

    override suspend fun addWatchListEntry(uris: Collection<URI>) {
        eventBus.watchListState.update { current -> current.copy(isAdditionRunning = true) }
        yield()

        uris.forEach { uri ->
            when(val anime = cache.fetch(uri)) {
                is DeadEntry -> {
                    log.warn { "Unable to retrieve anime for [$uri]" }
                }
                is PresentValue -> {
                    if (GenericReversibleCommand(
                        state = state,
                        commandHistory = commandHistory,
                        command = CmdAddWatchListEntry(
                            state = state,
                            watchListEntry = WatchListEntry(anime.value),
                        )
                    ).execute()) {
                        eventBus.findRelatedAnimeState.update { current -> current.copy(entries = current.entries.filterNot { it.link.uri == uri } ) }
                        eventBus.findByTitleState.update { FindByTitleState() }
                        eventBus.findSeasonState.update { current -> current.copy(entries = current.entries.filterNot { it.link.uri == uri } ) }
                        eventBus.findByCriertiaState.update { current -> current.copy(entries = current.entries.filterNot { it.link.uri == uri } ) }
                        eventBus.findSimilarAnimeState.update { current -> current.copy(entries = current.entries.filterNot { it.link.uri == uri } ) }
                    }
                }
            }
        }

        eventBus.watchListState.update { current -> current.copy(isAdditionRunning = false) }
    }

    override fun watchList(): Set<WatchListEntry> = state.watchList()

    override fun removeWatchListEntry(entry: WatchListEntry) {
        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdRemoveWatchListEntry(
                state = state,
                watchListEntry = entry,
            )
        ).execute()
    }

    override suspend fun addIgnoreListEntry(uris: Collection<URI>) {
        eventBus.ignoreListState.update { current -> current.copy(isAdditionRunning = true) }
        yield()

        uris.forEach { uri ->
            when(val anime = cache.fetch(uri)) {
                is DeadEntry -> {
                    log.warn { "Unable to retrieve anime for [$uri]" }
                }
                is PresentValue -> {
                    if (GenericReversibleCommand(
                        state = state,
                        commandHistory = commandHistory,
                        command = CmdAddIgnoreListEntry(
                            state = state,
                            ignoreListEntry = IgnoreListEntry(anime.value),
                        )
                    ).execute()) {
                        eventBus.findRelatedAnimeState.update { current -> current.copy(entries = current.entries.filterNot { it.link.uri == uri } ) }
                        eventBus.findByTitleState.update { FindByTitleState() }
                        eventBus.findSeasonState.update { current -> current.copy(entries = current.entries.filterNot { it.link.uri == uri } ) }
                        eventBus.findByCriertiaState.update { current -> current.copy(entries = current.entries.filterNot { it.link.uri == uri } ) }
                        eventBus.findSimilarAnimeState.update { current -> current.copy(entries = current.entries.filterNot { it.link.uri == uri } ) }
                    }
                }
            }
        }

        eventBus.ignoreListState.update { current -> current.copy(isAdditionRunning = false)}
    }

    override fun ignoreList(): Set<IgnoreListEntry> = state.ignoreList()

    override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdRemoveIgnoreListEntry(
                state = state,
                ignoreListEntry = entry,
            )
        ).execute()
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultListHandler]
         * @since 4.0.0
         */
        val instance: DefaultListHandler by lazy { DefaultListHandler() }
    }
}