package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.lists.watchlist.CmdAddWatchListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.GenericReversibleCommand
import io.github.manamiproject.manami.app.state.events.Event
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

internal class DefaultListHandler(
    private val state: State = InternalState,
    private val cache: Cache<URI, CacheEntry<Anime>> = Caches.animeCache,
): ListHandler {

    private val tasks = AtomicInteger(0)
    private val finishedTasks = AtomicInteger(0)
    private val pool = Executors.newSingleThreadExecutor()

    override fun addWatchListEntry(uris: Collection<URI>) {
        tasks.addAndGet(uris.size)
        pool.invokeAll(
            uris.map { uri ->
                Callable {
                    when(val anime = cache.fetch(uri)) {
                        is Empty -> {
                            log.warn("Unable to retrieve anime for [$uri]")
                        }
                        is Present -> {
                            GenericReversibleCommand(
                                command = CmdAddWatchListEntry(
                                    state = state,
                                    watchListEntry = WatchListEntry(anime.value),
                                )
                            ).execute()
                        }
                    }

                    SimpleEventBus.post(AddWatchListStatusUpdateEvent(finishedTasks.incrementAndGet(), tasks.get()))
                }
            }
        )
    }

    override fun watchList(): Set<WatchListEntry> = state.watchList()

    override fun ignoreList(): Set<IgnoreListEntry> = state.ignoreList()

    companion object {
        private val log by LoggerDelegate()
    }
}

data class AddWatchListStatusUpdateEvent(val finishedTasks: Int, val tasks: Int): Event