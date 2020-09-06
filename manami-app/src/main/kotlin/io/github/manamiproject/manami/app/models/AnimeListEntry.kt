package io.github.manamiproject.manami.app.models

import io.github.manamiproject.modb.core.extensions.Directory
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Episodes
import java.net.URL

data class AnimeListEntry(
        private val source: URL,
        private val title: String,
        private val episodes: Episodes,
        private val type: Anime.Type,
        private val location: Directory,
)