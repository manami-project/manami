package io.github.manamiproject.manami.app.cache

import io.github.manamiproject.manami.app.cache.loader.CacheLoader
import io.github.manamiproject.manami.app.cache.loader.DependentCacheLoader
import io.github.manamiproject.manami.app.cache.loader.KitsuCacheLoader
import io.github.manamiproject.manami.app.cache.loader.SimpleCacheLoader
import io.github.manamiproject.manami.app.cache.populator.NumberOfEntriesPerMetaDataProviderEvent
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.SimpleEventBus
import io.github.manamiproject.manami.app.events.Subscribe
import io.github.manamiproject.modb.anidb.AnidbConfig
import io.github.manamiproject.modb.anidb.AnidbConverter
import io.github.manamiproject.modb.anidb.AnidbDownloader
import io.github.manamiproject.modb.anilist.AnilistConfig
import io.github.manamiproject.modb.anilist.AnilistConverter
import io.github.manamiproject.modb.anilist.AnilistDownloader
import io.github.manamiproject.modb.animeplanet.AnimePlanetConfig
import io.github.manamiproject.modb.animeplanet.AnimePlanetConverter
import io.github.manamiproject.modb.animeplanet.AnimePlanetDownloader
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.anisearch.AnisearchConverter
import io.github.manamiproject.modb.anisearch.AnisearchDownloader
import io.github.manamiproject.modb.anisearch.AnisearchRelationsConfig
import io.github.manamiproject.modb.core.collections.SortedList
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpProtocol.HTTP_1_1
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Tag
import io.github.manamiproject.modb.livechart.LivechartConfig
import io.github.manamiproject.modb.livechart.LivechartConverter
import io.github.manamiproject.modb.livechart.LivechartDownloader
import io.github.manamiproject.modb.mal.MalConfig
import io.github.manamiproject.modb.mal.MalConverter
import io.github.manamiproject.modb.mal.MalDownloader
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.notify.NotifyConverter
import io.github.manamiproject.modb.notify.NotifyDownloader
import io.github.manamiproject.modb.notify.NotifyRelationsConfig
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createDirectory
import kotlin.io.path.createTempDirectory

private val anisearchRelationsDir = createTempDirectory("manami-anisearch_").resolve("relations").createDirectory()
private val notifyRelationsDir = createTempDirectory("manami-notify_").resolve("relations").createDirectory()

internal class DefaultAnimeCache(
    private val cacheLoader: List<CacheLoader> = listOf(
        SimpleCacheLoader(AnidbConfig, AnidbDownloader(AnidbConfig), AnidbConverter()),
        SimpleCacheLoader(AnilistConfig, AnilistDownloader(AnilistConfig), AnilistConverter()),
        SimpleCacheLoader(AnimePlanetConfig, AnimePlanetDownloader(AnimePlanetConfig), AnimePlanetConverter()),
        DependentCacheLoader(
            config = AnisearchConfig,
            animeDownloader = AnisearchDownloader(config = AnisearchConfig),
            relationsDownloader = AnisearchDownloader(config = AnisearchRelationsConfig),
            relationsDir = anisearchRelationsDir,
            converter = AnisearchConverter(relationsDir = anisearchRelationsDir),
        ),
        KitsuCacheLoader(),
        SimpleCacheLoader(
            config = LivechartConfig,
            downloader = LivechartDownloader(
                config = LivechartConfig,
                httpClient = DefaultHttpClient(
                    protocols = mutableListOf(HTTP_1_1),
                )
            ),
            converter = LivechartConverter(),
        ),
        SimpleCacheLoader(MalConfig, MalDownloader(MalConfig), MalConverter()),
        DependentCacheLoader(
            config = NotifyConfig,
            animeDownloader = NotifyDownloader(config = NotifyConfig),
            relationsDownloader = NotifyDownloader(config = NotifyRelationsConfig),
            relationsDir = notifyRelationsDir,
            converter = NotifyConverter(relationsDir = notifyRelationsDir),
        ),
    ),
    eventBus: EventBus = SimpleEventBus,
) : AnimeCache {

    private val entries = ConcurrentHashMap<URI, CacheEntry<Anime>>()

    private val _availableMetaDataProvider = mutableListOf<Hostname>()
    override val availableMetaDataProvider
        get() = _availableMetaDataProvider.toSet()

    private val _availableTags = mutableSetOf<Tag>()
    override val availableTags
        get() = _availableTags.toSet()

    init {
        eventBus.subscribe(this)
    }

    override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> {
        return entries.asSequence()
            .filter { it.key.host == metaDataProvider }
            .map { it.value }
            .filterIsInstance<PresentValue<Anime>>()
            .map { it.value }
            .flatMap { anime ->
                anime.sources.filter { it.toString().contains(metaDataProvider) }.map { link ->
                    anime.copy(
                        sources = SortedList(link),
                        relatedAnime = SortedList(anime.relatedAnime.filter { it.toString().contains(metaDataProvider) }.toMutableList()),
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

    override fun fetch(key: URI): CacheEntry<Anime> {
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

    @Subscribe(NumberOfEntriesPerMetaDataProviderEvent::class)
    fun sortMetaDataProviders(event: NumberOfEntriesPerMetaDataProviderEvent) {
        _availableMetaDataProvider.sortByDescending { event.entries[it] }
    }

    private fun loadEntry(uri: URI): CacheEntry<Anime> {
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
        val source = entry.value.sources.filter { it == requestedKey }.toMutableList()
        check(source.isNotEmpty())

        val relatedAnime = entry.value.relatedAnime.filter { it.toString().contains(requestedKey.host) }.toMutableList()
        val entryWithRequestedUri = entry.value.copy(
            sources = SortedList(source),
            relatedAnime = SortedList(relatedAnime),
        )

        return PresentValue(entryWithRequestedUri)
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}