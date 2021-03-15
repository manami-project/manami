package io.github.manamiproject.manami.app.import

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.import.parser.ParsedFile
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
        private val cache: Cache<URI, Anime?>
) : Command {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun execute() {
        log.info("Adding imported anime list entries [{}]", parsedFile.animeListEntries.size)

        val watchListEntryJob = GlobalScope.async {
            parsedFile.watchListEntries.mapNotNull { cache.fetch(it) }
                .map { WatchListEntry(it) }
                .toSet() }

        val ignoreListEntryJob = GlobalScope.async {
            parsedFile.ignoreListEntries.mapNotNull { cache.fetch(it) }
                .map { IgnoreListEntry(it) }
                .toSet()
        }

        log.info("Converting watch list entries [{}] and ignore list entries [{}]", parsedFile.watchListEntries.size, parsedFile.ignoreListEntries.size)

        runBlocking {
            watchListEntryJob.join()
            ignoreListEntryJob.join()
        }

        val watchListEntriss = watchListEntryJob.getCompleted()
        val ignoreListEntries = ignoreListEntryJob.getCompleted()

        log.info("Adding watch list entries [{}] and ignore list entries [{}]", watchListEntriss, ignoreListEntries)

        state.addAllAnimeListEntries(parsedFile.animeListEntries)
        state.addAllWatchListEntries(watchListEntriss)
        state.addAllIgnoreListEntries(ignoreListEntries)
    }

    companion object {
        private val log by LoggerDelegate()
    }
}