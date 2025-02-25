package io.github.manamiproject.manami.app.lists.watchlist

import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeStatus
import io.github.manamiproject.modb.core.anime.AnimeStatus.UNKNOWN
import io.github.manamiproject.modb.core.anime.Title
import java.net.URI

data class WatchListEntry(
    override val link: Link,
    override val title: Title,
    override val thumbnail: URI,
    val status: AnimeStatus = UNKNOWN,
) : AnimeEntry {

    constructor(anime: Anime): this(
        link = Link(anime.sources.first()),
        title = anime.title,
        thumbnail = anime.thumbnail,
    )

    constructor(animeEntry: AnimeEntry): this(
        link = animeEntry.link.asLink(),
        title = animeEntry.title,
        thumbnail = animeEntry.thumbnail,
    )
}