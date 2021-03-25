package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Title
import java.net.URI

data class BigPicturedAnimeEntry(override val link: Link, override val title: Title, override val thumbnail: URI): AnimeEntry {

    constructor(anime: Anime): this(Link(anime.sources.first()), anime.title, anime.picture)
    constructor(animeEntry: AnimeEntry): this(animeEntry.link.asLink(), animeEntry.title, animeEntry.thumbnail)
}
