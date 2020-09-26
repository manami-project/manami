package io.github.manamiproject.manami.app.fileimport

import io.github.manamiproject.manami.app.commands.Command
import io.github.manamiproject.manami.app.fileimport.parser.ParsedFile
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State

internal class CmdAddEntriesFromParsedFile(
        private val state: State = InternalState,
        private val parsedFile: ParsedFile,
) : Command {

    override fun execute() {
        state.addAllAnimeListEntries(parsedFile.animeListEntries)
        state.addAllWatchListEntries(parsedFile.watchListEntries)
        state.addAllIgnoreListEntries(parsedFile.ignoreListEntries)
    }
}