package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command

internal class CmdAddAnimeListEntry(
    private val state: State,
    private val animeListEntry: AnimeListEntry): Command {

    override fun execute(): Boolean {
        if (state.animeList().map { it.link }.filterIsInstance<Link>().any { it == animeListEntry.link }) {
            return false
        }

        state.addAllAnimeListEntries(setOf(animeListEntry))
        return true
    }
}