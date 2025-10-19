package io.github.manamiproject.manami.app.cache.loader

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.config.Hostname
import java.net.URI

internal interface CacheLoader {
    fun hostname(): Hostname
    fun loadAnime(uri: URI): Anime
}