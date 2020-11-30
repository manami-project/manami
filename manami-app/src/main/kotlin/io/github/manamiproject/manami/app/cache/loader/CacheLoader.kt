package io.github.manamiproject.manami.app.cache.loader

import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.Anime
import java.net.URI

internal interface CacheLoader {
    fun hostname(): Hostname
    fun loadAnime(uri: URI): Anime
}