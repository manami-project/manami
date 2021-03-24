package io.github.manamiproject.manami.app.lists

import io.github.manamiproject.modb.core.models.Title
import java.net.URI

interface AnimeEntry {
    val link: LinkEntry
    val title: Title
    val thumbnail: URI
}