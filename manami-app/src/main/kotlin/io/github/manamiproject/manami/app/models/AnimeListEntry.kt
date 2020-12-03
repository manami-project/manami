package io.github.manamiproject.manami.app.models

import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Episodes
import io.github.manamiproject.modb.core.models.Title
import java.net.URI

data class AnimeListEntry(
        private val link: LinkEntry = NoLink,
        private val title: Title,
        private val episodes: Episodes,
        private val type: Anime.Type,
        private val location: String,
)

sealed class LinkEntry
object NoLink: LinkEntry()
data class Link(val uri: URI): LinkEntry()