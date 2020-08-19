package io.github.manamiproject.manami.app.cache

import io.github.manamiproject.manami.app.cache.loader.CacheLoader
import io.github.manamiproject.manami.app.cache.loader.KitsuCacheLoader
import io.github.manamiproject.manami.app.cache.loader.NotifyCacheLoader
import io.github.manamiproject.manami.app.cache.loader.SimpleCacheLoader
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anidb.AnidbConverter
import io.github.manamiproject.modb.anidb.AnidbDownloader
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConverter
import io.github.manamiproject.modb.animeplanet.AnimePlanetDownloader
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.mal.MalConfig
import io.github.manamiproject.modb.mal.MalConverter
import io.github.manamiproject.modb.mal.MalDownloader
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

internal class AnimeCache(
        private val cacheLoader: List<CacheLoader> = listOf(
                SimpleCacheLoader(AnidbConfig, AnidbDownloader(AnidbConfig), AnidbConverter()),
                SimpleCacheLoader(AnimePlanetConfig, AnimePlanetDownloader(AnimePlanetConfig), AnimePlanetConverter()),
                KitsuCacheLoader(),
                SimpleCacheLoader(MalConfig, MalDownloader(MalConfig), MalConverter()),
                NotifyCacheLoader()
        )
) : Cache<URL, Anime?> {

    private val entries = ConcurrentHashMap<URL, Container>()

    override fun fetch(key: URL): Anime? {
        return when(val entry = entries[key]) {
            is Present -> entry.value
            Empty -> null
            null -> loadEntry(key)
        }
    }

    override fun populate(key: URL, value: Anime?) {
        if (!entries.containsKey(key)) {
            entries[key] = if (value == null) {
                Empty
            } else {
                Present(value)
            }
        } else {
            log.warn("Not populating cache with key [{}], because it already exists", key)
        }
    }

    override fun clear() {
        log.info("Clearing cache")
        entries.clear()
    }

    private fun loadEntry(url: URL): Anime? {
        log.info("No cache hit for [{}]", url)

        val cacheLoader = cacheLoader.find { url.toString().contains(it.hostname()) }

        if (cacheLoader == null) {
            log.warn("Unable to find a CacheLoader for URL [{}]", url)
            return null
        }

        val anime = cacheLoader.loadAnime(url)
        anime.sources.forEach {
            populate(it, anime)
        }

        return anime
    }

    companion object {
        private val log by LoggerDelegate()
    }
}

private sealed class Container
private class Present(val value: Anime) : Container()
private object Empty: Container()