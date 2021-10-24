package io.github.manamiproject.manami.app.import

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Caches
import io.github.manamiproject.manami.app.import.parser.ParsedFile
import io.github.manamiproject.manami.app.import.parser.Parser
import io.github.manamiproject.manami.app.state.commands.GenericReversibleCommand
import io.github.manamiproject.manami.app.import.parser.manami.ManamiLegacyFileParser
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.history.CommandHistory
import io.github.manamiproject.manami.app.state.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.fileSuffix
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI

internal class DefaultImportHandler(
    private val parserList: List<Parser<ParsedFile>> = listOf(ManamiLegacyFileParser()),
    private val cache: Cache<URI, CacheEntry<Anime>> = Caches.animeCache,
    private val state: State = InternalState,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
    private val eventBus: EventBus = SimpleEventBus,
) : ImportHandler {

    init {
        require(parserList.isNotEmpty()) { "List of parser must not be empty" }
        require(hasOnlyOneParserPerSuffix()) { "Only one parser per file suffix" }
    }

    override fun import(file: RegularFile) {
        require(file.regularFileExists()) { "Given path doesn't exist or is not a file [${file.toAbsolutePath()}]" }

        val parser = parserList.find { it.handlesSuffix() == file.fileSuffix() } ?: throw IllegalArgumentException("No suitable parser for file type [${file.fileSuffix()}]")
        val content = parser.parse(file)

        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdAddEntriesFromParsedFile(
                parsedFile = content,
                cache = cache,
            )
        ).execute()

        eventBus.post(ImportFinishedEvent)
    }

    private fun hasOnlyOneParserPerSuffix(): Boolean {
        return parserList.groupBy { it.handlesSuffix() }.filter { it.value.size > 1 }.isEmpty()
    }
}