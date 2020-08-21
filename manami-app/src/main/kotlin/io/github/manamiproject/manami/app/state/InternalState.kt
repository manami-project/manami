package io.github.manamiproject.manami.app.state

import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.models.Anime
import java.net.URL

internal object InternalState : State {

    private var openedFile: RegularFile? = null

    override fun upsertAnimeListEntry(anime: Anime) {
        TODO("Not yet implemented")
    }

    override fun removeAnimeListEntry(anime: Anime) {
        TODO("Not yet implemented")
    }

    override fun upsertWatchListEntry(url: URL) {
        TODO("Not yet implemented")
    }

    override fun removeWatchListEntry(url: URL) {
        TODO("Not yet implemented")
    }

    override fun upsertIgnoreListEntry(url: URL) {
        TODO("Not yet implemented")
    }

    override fun removeIgnoreListEntry(url: URL) {
        TODO("Not yet implemented")
    }

    override fun openedFile(file: RegularFile) {
        check(file.regularFileExists())
        openedFile = file
    }

    override fun closeFile() {
        openedFile = null
    }
}