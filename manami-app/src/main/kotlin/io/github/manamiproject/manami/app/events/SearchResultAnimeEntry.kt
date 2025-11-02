package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.Title
import java.net.URI
import kotlin.collections.first

data class SearchResultAnimeEntry(override val link: Link, override val title: Title, override val thumbnail: URI): AnimeEntry {

    constructor(anime: Anime): this(Link(anime.sources.first()), anime.title, anime.picture)
    constructor(animeEntry: AnimeEntry): this(animeEntry.link.asLink(), animeEntry.title, animeEntry.thumbnail)
}
