package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.commands.Command
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State

internal class CmdRemoveAnimeListEntry(
    private val state: State = InternalState,
    private val animeListEntry: AnimeListEntry,
): Command {

    override fun execute(): Boolean {
        if (!state.animeListEntryExists(animeListEntry)) {
            return false
        }

        state.removeAnimeListEntry(animeListEntry)

        return true
    }
}