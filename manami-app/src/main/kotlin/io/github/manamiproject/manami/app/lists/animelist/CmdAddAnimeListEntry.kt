package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.commands.Command

internal class CmdAddAnimeListEntry(
    private val state: State,
    private val animeListEntry: AnimeListEntry,
): Command {

    override fun execute(): Boolean {
        if (state.animeListEntrtyExists(animeListEntry)) {
            return false
        }

        state.addAllAnimeListEntries(setOf(animeListEntry.convertLocationToRelativePath(state.openedFile())))

        return true
    }
}