package io.github.manamiproject.manami.app.import

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Empty
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.import.parser.ParsedFile
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.net.URI

internal class CmdAddEntriesFromParsedFile(
        private val state: State = InternalState,
        private val parsedFile: ParsedFile,
        private val cache: Cache<URI, CacheEntry<Anime>>
) : Command {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute(): Boolean {
        log.info("Adding imported anime list entries [{}]", parsedFile.animeListEntries.size)

        val animeListEntryJob = GlobalScope.async {
            parsedFile.animeListEntries.map { animeListEntry ->
                val cacheEntry = when(animeListEntry.link) {
                    is Link -> cache.fetch(animeListEntry.link.uri)
                    else -> Empty()
                }

                return@map when(cacheEntry) {
                    is PresentValue -> animeListEntry.copy(thumbnail = cacheEntry.value.thumbnail)
                    else -> animeListEntry
                }
            }
            .toSet() }

        val watchListEntryJob = GlobalScope.async {
            parsedFile.watchListEntries.map { cache.fetch(it) }
                .filterIsInstance<PresentValue<Anime>>()
                .map { WatchListEntry(it.value) }
                .toSet() }

        val ignoreListEntryJob = GlobalScope.async {
            parsedFile.ignoreListEntries.map { cache.fetch(it) }
                .filterIsInstance<PresentValue<Anime>>()
                .map { IgnoreListEntry(it.value) }
                .toSet()
        }

        log.info("Converting [{}] anime list entries, [{}] watch list entries and [{}] ignore list entries", parsedFile.animeListEntries.size, parsedFile.watchListEntries.size, parsedFile.ignoreListEntries.size)

        runBlocking {
            animeListEntryJob.join()
            watchListEntryJob.join()
            ignoreListEntryJob.join()
        }

        val animeListEntries = animeListEntryJob.getCompleted()
        val watchListEntries = watchListEntryJob.getCompleted()
        val ignoreListEntries = ignoreListEntryJob.getCompleted()

        log.trace("Adding [{}] to anime list entries, [{}] to watch list entries and [{}] to ignore list entries", animeListEntries, watchListEntries, ignoreListEntries)

        state.addAllAnimeListEntries(animeListEntries)
        state.addAllWatchListEntries(watchListEntries)
        state.addAllIgnoreListEntries(ignoreListEntries)
        return true
    }

    companion object {
        private val log by LoggerDelegate()
    }
}