package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.commands.Command
import io.github.manamiproject.manami.app.state.State

internal class CmdAddAnimeListEntry(
    private val state: State,
    private val animeListEntry: AnimeListEntry,
): Command {

    override fun execute(): Boolean {
        if (state.animeListEntryExists(animeListEntry)) {
            return false
        }

        state.addAllAnimeListEntries(setOf(animeListEntry.convertLocationToRelativePath(state.openedFile())))

        return true
    }
}