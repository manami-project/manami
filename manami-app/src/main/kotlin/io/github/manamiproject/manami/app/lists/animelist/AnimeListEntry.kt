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
import java.nio.file.Path
import kotlin.io.path.Path

data class AnimeListEntry(
    override val link: LinkEntry = NoLink,
    override val title: Title,
    override val thumbnail: URI = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
    val episodes: Episodes,
    val type: Anime.Type,
    val location: Path,
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

        return copy(location = location)
    }

    private fun validateLocation() {
        if (location.toString().startsWith("/")) {
            require(location.directoryExists()) { "Location is not a directory or does not exist." }
        }
    }
}
