package io.github.manamiproject.manami.app.cache

import io.github.manamiproject.AnimenewsnetworkAnimeConverter
import io.github.manamiproject.AnimenewsnetworkConfig
import io.github.manamiproject.AnimenewsnetworkDownloader
import io.github.manamiproject.manami.app.cache.loader.CacheLoader
import io.github.manamiproject.manami.app.cache.loader.DependentCacheLoader
import io.github.manamiproject.manami.app.cache.loader.SimpleCacheLoader
import io.github.manamiproject.modb.anidb.AnidbAnimeConverter
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anidb.AnidbDownloader
import io.github.manamiproject.modb.anilist.AnilistAnimeConverter
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.anilist.AnilistDownloader
import io.github.manamiproject.modb.animeplanet.AnimePlanetAnimeConverter
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetDownloader
import io.github.manamiproject.modb.anisearch.AnisearchAnimeConverter
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.anisearch.AnisearchDownloader
import io.github.manamiproject.modb.anisearch.AnisearchRelationsConfig
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.Producer
import io.github.manamiproject.modb.core.anime.Studio
import io.github.manamiproject.modb.core.anime.Tag
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpProtocol.HTTP_1_1
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.kitsu.KitsuAnimeConverter
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.kitsu.KitsuDownloader
import io.github.manamiproject.modb.livechart.LivechartAnimeConverter
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.livechart.LivechartDownloader
import io.github.manamiproject.modb.myanimelist.MyanimelistAnimeConverter
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistDownloader
import io.github.manamiproject.modb.simkl.SimklAnimeConverter
import io.github.manamiproject.modb.simkl.SimklConfig
import io.github.manamiproject.modb.simkl.SimklDownloader
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory

private val anisearchRelationsDir = createTempDirectory("manami-anisearch_").resolve("relations").createDirectory()

internal class DefaultAnimeCache(
    private val cacheLoader: List<CacheLoader> = listOf(
        SimpleCacheLoader(AnidbConfig, AnidbDownloader.instance, AnidbAnimeConverter.instance),
        SimpleCacheLoader(AnilistConfig, AnilistDownloader.instance, AnilistAnimeConverter.instance),
        SimpleCacheLoader(AnimePlanetConfig, AnimePlanetDownloader.instance, AnimePlanetAnimeConverter.instance),
        SimpleCacheLoader(AnimenewsnetworkConfig, AnimenewsnetworkDownloader.instance, AnimenewsnetworkAnimeConverter.instance),
        DependentCacheLoader(
            config = AnisearchConfig,
            animeDownloader = AnisearchDownloader(metaDataProviderConfig = AnisearchConfig),
            relationsDownloader = AnisearchDownloader(metaDataProviderConfig = AnisearchRelationsConfig),
            relationsDir = anisearchRelationsDir,
            converter = AnisearchAnimeConverter(relationsDir = anisearchRelationsDir),
        ),
        SimpleCacheLoader(KitsuConfig, KitsuDownloader.instance, KitsuAnimeConverter.instance),
        SimpleCacheLoader(
            config = LivechartConfig,
            downloader = LivechartDownloader(
                metaDataProviderConfig = LivechartConfig,
                httpClient = DefaultHttpClient(
                    protocols = mutableListOf(HTTP_1_1),
                )
            ),
            converter = LivechartAnimeConverter(),
        ),
        SimpleCacheLoader(MyanimelistConfig, MyanimelistDownloader.instance, MyanimelistAnimeConverter.instance),
        SimpleCacheLoader(SimklConfig, SimklDownloader.instance, SimklAnimeConverter.instance),
    ),
) : AnimeCache {

    private val entries = ConcurrentHashMap<URI, CacheEntry<Anime>>()

    private val _availableMetaDataProvider = mutableSetOf<Hostname>()
    override val availableMetaDataProvider: Set<Hostname>
        get() = _availableMetaDataProvider

    private val _availableTags = mutableSetOf<Tag>()
    override val availableTags: Set<Tag>
        get() = _availableTags

    private val _availableStudios = mutableSetOf<Studio>()
    override val availableStudios: Set<Studio>
        get() = _availableStudios

    private val _availableProducers = mutableSetOf<Producer>()
    override val availableProducers: Set<Producer>
        get() = _availableProducers

    override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> {
        return entries.asSequence()
            .filter { it.key.host == metaDataProvider }
            .map { it.value }
            .filterIsInstance<PresentValue<Anime>>()
            .map { it.value }
            .flatMap { anime ->
                anime.sources.filter { it.toString().contains(metaDataProvider) }.map { link ->
                    anime.copy(
                        sources = hashSetOf(link),
                        relatedAnime = anime.relatedAnime.filter { it.toString().contains(metaDataProvider) }.toHashSet(),
                    )
                }
            }
            .distinct()
    }

    override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> {
        val cacheEntry = entries[uri] ?: return emptySet()

        return when (cacheEntry) {
            is PresentValue -> cacheEntry.value.sources.filter { it.host == metaDataProvider }.toSet()
            is DeadEntry -> emptySet()
        }
    }

    override suspend fun fetch(key: URI): CacheEntry<Anime> {
        return when(val entry = entries[key]) {
            is PresentValue<Anime> -> removeUnrequestedMetaDataProvider(entry, key)
            is DeadEntry<Anime> -> entry
            null -> loadEntry(key)
        }
    }

    override fun populate(key: URI, value: CacheEntry<Anime>) {
        if (!availableMetaDataProvider.contains(key.host)) {
            _availableMetaDataProvider.add(key.host)
        }

        if (value is PresentValue) {
            _availableTags.addAll(value.value.tags)
            _availableStudios.addAll(value.value.studios)
            _availableProducers.addAll(value.value.producers)
        }

        when {
            !entries.containsKey(key) -> entries[key] = value
            else -> log.warn { "Not populating cache with key [$key], because it already exists" }
        }
    }

    override fun clear() {
        log.info { "Clearing anime cache" }
        entries.clear()
    }

    private suspend fun loadEntry(uri: URI): CacheEntry<Anime> {
        log.info { "No cache hit for [$uri]" }

        val cacheLoader = cacheLoader.find { uri.toString().contains(it.hostname()) }

        if (cacheLoader == null) {
            log.warn { "Unable to find a CacheLoader for URI [$uri]" }
            return DeadEntry()
        }

        return try {
            val anime = cacheLoader.loadAnime(uri)
            val cacheEntry = PresentValue(anime)
            anime.sources.forEach {
                populate(it, cacheEntry)
            }
            removeUnrequestedMetaDataProvider(cacheEntry, uri)
        } catch (t: Throwable) {
            populate(uri, DeadEntry())
            DeadEntry()
        }
    }

    private fun removeUnrequestedMetaDataProvider(entry: PresentValue<Anime>, requestedKey: URI): PresentValue<Anime> {
        val source = entry.value.sources.filter { it == requestedKey }.toHashSet()
        check(source.isNotEmpty())

        val relatedAnime = entry.value.relatedAnime.filter { it.toString().contains(requestedKey.host) }.toHashSet()
        val entryWithRequestedUri = entry.value.copy(
            sources = source,
            relatedAnime = relatedAnime,
        )

        return PresentValue(entryWithRequestedUri)
    }

    companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [DefaultAnimeCache]
         * @since 3.12.7
         */
        val instance: DefaultAnimeCache by lazy { DefaultAnimeCache() }
    }
}