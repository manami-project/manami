package io.github.manamiproject.manami.app.models

import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Episodes
import java.net.URL

data class AnimeListEntry(
        private val source: SourceEntry = NoSource,
        private val title: String,
        private val episodes: Episodes,
        private val type: Anime.Type,
        private val location: String,
)

sealed class SourceEntry
object NoSource: SourceEntry()
data class Source(val url: URL): SourceEntry()