package io.github.manamiproject.manami.app.cache

import io.github.manamiproject.manami.app.cache.loader.CacheLoader
import io.github.manamiproject.manami.app.cache.loader.KitsuCacheLoader
import io.github.manamiproject.manami.app.cache.loader.NotifyCacheLoader
import io.github.manamiproject.manami.app.cache.loader.SimpleCacheLoader
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anidb.AnidbConverter
import io.github.manamiproject.modb.anidb.AnidbDownloader
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.anilist.AnilistConverter
import io.github.manamiproject.modb.anilist.AnilistDownloader
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConverter
import io.github.manamiproject.modb.animeplanet.AnimePlanetDownloader
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.mal.MalConfig
import io.github.manamiproject.modb.mal.MalConverter
import io.github.manamiproject.modb.mal.MalDownloader
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

internal class AnimeCache(
        private val cacheLoader: List<CacheLoader> = listOf(
                SimpleCacheLoader(AnidbConfig, AnidbDownloader(AnidbConfig), AnidbConverter()),
                SimpleCacheLoader(AnilistConfig, AnilistDownloader(AnilistConfig), AnilistConverter()),
                SimpleCacheLoader(AnimePlanetConfig, AnimePlanetDownloader(AnimePlanetConfig), AnimePlanetConverter()),
                KitsuCacheLoader(),
                SimpleCacheLoader(MalConfig, MalDownloader(MalConfig), MalConverter()),
                NotifyCacheLoader()
        )
) : Cache<URI, CacheEntry<Anime>> {

    private val entries = ConcurrentHashMap<URI, CacheEntry<Anime>>()

    override fun fetch(key: URI): CacheEntry<Anime> {
        return when(val entry = entries[key]) {
            is PresentValue<Anime>, is Empty<Anime> -> entry
            null -> loadEntry(key)
        }
    }

    override fun populate(key: URI, value: CacheEntry<Anime>) {
        if (!entries.containsKey(key)) {
            entries[key] = value
        } else {
            log.warn("Not populating cache with key [{}], because it already exists", key)
        }
    }

    override fun clear() {
        log.info("Clearing cache")
        entries.clear()
    }

    private fun loadEntry(uri: URI): CacheEntry<Anime> {
        log.info("No cache hit for [{}]", uri)

        val cacheLoader = cacheLoader.find { uri.toString().contains(it.hostname()) }

        if (cacheLoader == null) {
            log.warn("Unable to find a CacheLoader for URI [{}]", uri)
            return Empty()
        }

        return try {
            val anime = cacheLoader.loadAnime(uri)
            val cacheEntry = PresentValue(anime)
            anime.sources.forEach {
                populate(it, cacheEntry)
            }
            cacheEntry
        } catch (t: Throwable) {
            populate(uri, Empty())
            Empty()
        }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}