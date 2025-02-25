package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.modb.core.anime.Title
import java.net.URI

interface AnimeEntry {
    val link: LinkEntry
    val title: Title
    val thumbnail: URI
}