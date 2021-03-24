package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.LinkEntry
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Episodes
import io.github.manamiproject.modb.core.models.Title
import java.net.URI

data class AnimeListEntry(
    override val link: LinkEntry = NoLink,
    override val title: Title,
    override val thumbnail: URI = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
    val episodes: Episodes,
    val type: Anime.Type,
    val location: URI,
): AnimeEntry
