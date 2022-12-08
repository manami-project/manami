package io.github.manamiproject.manami.app.cache

import io.github.manamiproject.manami.app.cache.loader.CacheLoader
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Tag
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URI
import java.net.URL

internal object TestCacheLoader : CacheLoader {
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override fun loadAnime(uri: URI): Anime = shouldNotBeInvoked()
}

internal object MetaDataProviderTestConfig: MetaDataProviderConfig {
    override fun isTestContext(): Boolean = shouldNotBeInvoked()
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override fun buildAnimeLink(id: AnimeId): URI = shouldNotBeInvoked()
    override fun buildDataDownloadLink(id: String): URI = shouldNotBeInvoked()
    override fun extractAnimeId(uri: URI): AnimeId = shouldNotBeInvoked()
    override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
}

internal object TestDownloader: Downloader {
    override suspend fun download(id: AnimeId, onDeadEntry: suspend (AnimeId) -> Unit): String = shouldNotBeInvoked()
}

internal object TestAnimeConverter: AnimeConverter {
    override suspend fun convert(rawContent: String): Anime = shouldNotBeInvoked()
}

internal object TestHttpClient: HttpClient {
    override suspend fun executeRetryable(retryWith: String, func: suspend () -> HttpResponse): HttpResponse = shouldNotBeInvoked()
    override suspend fun get(url: URL, headers: Map<String, Collection<String>>, retryWith: String): HttpResponse = shouldNotBeInvoked()
    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>, retryWith: String): HttpResponse = shouldNotBeInvoked()
}

internal object TestAnimeCache: AnimeCache {
    override val availableMetaDataProvider: Set<Hostname>
        get() = shouldNotBeInvoked()
    override val availableTags: Set<Tag>
        get() = shouldNotBeInvoked()
    override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = shouldNotBeInvoked()
    override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = shouldNotBeInvoked()
    override fun fetch(key: URI): CacheEntry<Anime> = shouldNotBeInvoked()
    override fun populate(key: URI, value: CacheEntry<Anime>) = shouldNotBeInvoked()
    override fun clear() = shouldNotBeInvoked()
}