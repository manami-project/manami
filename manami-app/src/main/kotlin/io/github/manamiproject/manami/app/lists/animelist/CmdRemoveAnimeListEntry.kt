package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command

internal class CmdRemoveAnimeListEntry(
    private val state: State = InternalState,
    private val animeListEntry: AnimeListEntry,
): Command {

    override fun execute() {
        state.removeAnimeListEntry(animeListEntry)
    }
}