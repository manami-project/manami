package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command

internal class CmdRemoveAnimeListEntry(
    private val state: State = InternalState,
    private val animeListEntry: AnimeListEntry,
): Command {

    override fun execute(): Boolean {
        if (state.animeList().map { it.link }.filterIsInstance<Link>().none { it == animeListEntry.link }) {
            return false
        }

        state.removeAnimeListEntry(animeListEntry)
        return true
    }
}