package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.commands.Command
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.modb.core.extensions.RegularFile

internal class CmdOpenFile(
        private val state: State = InternalState,
        private val commandHistory: CommandHistory = DefaultCommandHistory,
        private val file: RegularFile,
        private val parsedFile: ParsedManamiFile,
) : Command {

    override fun execute(): Boolean {
        commandHistory.clear()
        state.clear()
        state.closeFile()
        state.addAllAnimeListEntries(parsedFile.animeListEntries)
        state.addAllWatchListEntries(parsedFile.watchListEntries)
        state.addAllIgnoreListEntries(parsedFile.ignoreListEntries)
        state.setOpenedFile(file)
        return true
    }
}