package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.LinkEntry
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.state.CurrentFile
import io.github.manamiproject.manami.app.state.OpenedFile
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE_THUMBNAIL
import io.github.manamiproject.modb.core.anime.AnimeType
import io.github.manamiproject.modb.core.anime.Episodes
import io.github.manamiproject.modb.core.anime.Title
import io.github.manamiproject.modb.core.extensions.directoryExists
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.Path

data class AnimeListEntry(
    override val link: LinkEntry = NoLink,
    override val title: Title,
    override val thumbnail: URI = NO_PICTURE_THUMBNAIL,
    val episodes: Episodes,
    val type: AnimeType,
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
