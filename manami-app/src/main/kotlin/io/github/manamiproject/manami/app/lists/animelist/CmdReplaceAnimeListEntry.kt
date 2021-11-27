package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.commands.Command

internal class CmdReplaceAnimeListEntry(
    private val state: State = InternalState,
    private val currentEntry: AnimeListEntry,
    private val replacementEntry: AnimeListEntry,
): Command {

    override fun execute(): Boolean {
        if (!state.animeListEntrtyExists(currentEntry)) {
            return false
        }

        state.removeAnimeListEntry(currentEntry)
        state.addAllAnimeListEntries(setOf(replacementEntry.convertLocationToRelativePath(state.openedFile())))

        return true
    }
}