package io.github.manamiproject.manami.app.cache.loader

import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URI

object TestCacheLoader: CacheLoader {
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override fun loadAnime(uri: URI): Anime = shouldNotBeInvoked()
}