package io.github.manamiproject.manami.app.models

import io.github.manamiproject.modb.core.models.Title
import java.net.URI

interface AnimeEntry {
    val link: Link
    val title: Title
    val thumbnail: URI
}