package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.LinkEntry
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.OpenedFile
import io.github.manamiproject.modb.core.extensions.directoryExists
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Episodes
import io.github.manamiproject.modb.core.models.Title
import java.net.URI
import kotlin.io.path.Path

data class AnimeListEntry(
    override val link: LinkEntry = NoLink,
    override val title: Title,
    override val thumbnail: URI = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
    val episodes: Episodes,
    val type: Anime.Type,
    val location: URI,
): AnimeEntry {

    init {
        validateLocation()
    }

    internal fun convertLocationToRelativePath(openedFile: OpenedFile): AnimeListEntry {
        val locationString = if (location.toString().startsWith("/")) "/$location" else location.toString()
        var location = Path(locationString)

        if (openedFile is CurrentFile) {
            val startDir = openedFile.regularFile.parent

            when (startDir == location) {
                true -> {
                    location = Path(".")
                }
                false -> {
                    location = startDir.resolve(location)
                    location = startDir.relativize(location)
                }
            }
        }

        val newLocation = URI(location.toString())
        return copy(location = newLocation)
    }

    private fun validateLocation() {
        if (location.toString().startsWith("/")) {
            require(Path(location.toString()).directoryExists()) { "Location is not a directory or does not exist." }
        }
    }
}
