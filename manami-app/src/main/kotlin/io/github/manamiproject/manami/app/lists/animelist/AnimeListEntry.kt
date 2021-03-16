package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.LinkEntry
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Episodes
import io.github.manamiproject.modb.core.models.Title
import java.net.URI

data class AnimeListEntry(
    val link: LinkEntry = NoLink,
    val title: Title,
    val episodes: Episodes,
    val type: Anime.Type,
    val location: URI,
)