package io.github.manamiproject.manami.cache.loader

import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.Anime
import java.net.URL

interface CacheLoader {
    fun hostname(): Hostname
    fun loadAnime(url: URL): Anime
}