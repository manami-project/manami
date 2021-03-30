package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.commands.Command
import java.net.URI
import java.nio.file.Paths

internal class CmdAddAnimeListEntry(
    private val state: State,
    private val animeListEntry: AnimeListEntry,
): Command {

    override fun execute(): Boolean {
        if (state.animeList().map { it.link }.filterIsInstance<Link>().any { it == animeListEntry.link }) {
            return false
        }

        val locationString = if (animeListEntry.location.toString().startsWith("/")) "/${animeListEntry.location}" else animeListEntry.location.toString()
        var location = Paths.get(locationString)

        val openedFile = state.openedFile()
        if (openedFile is CurrentFile) {
            val startDir = openedFile.regularFile.parent
            location = startDir.resolve(location)
            location = startDir.relativize(location)
        }

        val newLocation = URI(location.toString())

        state.addAllAnimeListEntries(setOf(animeListEntry.copy(location = newLocation)))
        return true
    }
}