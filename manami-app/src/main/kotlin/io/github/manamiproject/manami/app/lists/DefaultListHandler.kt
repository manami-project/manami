package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.commands.GenericReversibleCommand
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.SimpleEventBus
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.animelist.CmdAddAnimeListEntry
import io.github.manamiproject.manami.app.lists.animelist.CmdRemoveAnimeListEntry
import io.github.manamiproject.manami.app.lists.animelist.CmdReplaceAnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.AddIgnoreListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.ignorelist.CmdAddIgnoreListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.CmdRemoveIgnoreListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.AddWatchListStatusUpdateEvent
import io.github.manamiproject.manami.app.lists.watchlist.CmdAddWatchListEntry
import io.github.manamiproject.manami.app.lists.watchlist.CmdRemoveWatchListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.net.URI
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

internal class DefaultListHandler(
    private val state: State = InternalState,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
    private val cache: AnimeCache = Caches.defaultAnimeCache,
    private val eventBus: EventBus = SimpleEventBus,
): ListHandler {

    private val totalNumberOfWatchListTasks = AtomicInteger(0)
    private val finishedAddWatchListTasks = AtomicInteger(0)

    private val totalNumberOfIgnoreListTasks = AtomicInteger(0)
    private val finishedAddIgnoreListTasks = AtomicInteger(0)

    private val pool = Executors.newSingleThreadExecutor()

    override fun addAnimeListEntry(entry: AnimeListEntry) {
        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdAddAnimeListEntry(
                state = state,
                animeListEntry = entry,
            )
        ).execute()
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

    override fun addWatchListEntry(uris: Collection<URI>) {
        totalNumberOfWatchListTasks.addAndGet(uris.size)
        pool.invokeAll(
            uris.map { uri ->
                Callable {
                    when(val anime = cache.fetch(uri)) {
                        is DeadEntry -> {
                            log.warn { "Unable to retrieve anime for [$uri]" }
                        }
                        is PresentValue -> {
                            GenericReversibleCommand(
                                state = state,
                                commandHistory = commandHistory,
                                command = CmdAddWatchListEntry(
                                    state = state,
                                    watchListEntry = WatchListEntry(anime.value),
                                )
                            ).execute()
                        }
                    }

                    eventBus.post(AddWatchListStatusUpdateEvent(finishedAddWatchListTasks.incrementAndGet(), totalNumberOfWatchListTasks.get()))
                }
            }
        )
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

    override fun addIgnoreListEntry(uris: Collection<URI>) {
        totalNumberOfIgnoreListTasks.addAndGet(uris.size)
        pool.invokeAll(
            uris.map { uri ->
                Callable {
                    when(val anime = cache.fetch(uri)) {
                        is DeadEntry -> {
                            log.warn { "Unable to retrieve anime for [$uri]" }
                        }
                        is PresentValue -> {
                            GenericReversibleCommand(
                                state = state,
                                commandHistory = commandHistory,
                                command = CmdAddIgnoreListEntry(
                                    state = state,
                                    ignoreListEntry = IgnoreListEntry(anime.value),
                                )
                            ).execute()
                        }
                    }

                    eventBus.post(AddIgnoreListStatusUpdateEvent(finishedAddIgnoreListTasks.incrementAndGet(), totalNumberOfIgnoreListTasks.get()))
                }
            }
        )
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
    }
}