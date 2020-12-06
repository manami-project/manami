package io.github.manamiproject.manami.app.models

import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Title
import java.net.URI

data class WatchListEntry(
    val link: Link,
    val title: Title,
    val thumbnail: URI,
) {

    constructor(anime: Anime): this(
        link = Link(anime.sources.first()),
        title = anime.title,
        thumbnail = anime.thumbnail,
    )
}