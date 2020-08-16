package io.github.manamiproject.manami.cache

import io.github.manamiproject.manami.cache.loader.CacheLoader
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
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URL

internal object TestCacheLoader : CacheLoader {
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override fun loadAnime(url: URL): Anime = shouldNotBeInvoked()
}

internal object MetaDataProviderTestConfig: MetaDataProviderConfig {
    override fun isTestContext(): Boolean = shouldNotBeInvoked()
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override fun buildAnimeLinkUrl(id: AnimeId): URL = shouldNotBeInvoked()
    override fun buildDataDownloadUrl(id: String): URL = shouldNotBeInvoked()
    override fun extractAnimeId(url: URL): AnimeId = shouldNotBeInvoked()
    override fun fileSuffix(): FileSuffix = shouldNotBeInvoked()
}

internal object TestDownloader: Downloader {
    override fun download(id: AnimeId, onDeadEntry: (AnimeId) -> Unit): String = shouldNotBeInvoked()
}

internal object TestAnimeConverter: AnimeConverter {
    override fun convert(source: String): Anime = shouldNotBeInvoked()
}

internal object TestHttpClient: HttpClient {
    override fun executeRetryable(retryWith: String, func: () -> HttpResponse): HttpResponse  = shouldNotBeInvoked()
    override fun get(url: URL, headers: Map<String, List<String>>, retryWith: String): HttpResponse  = shouldNotBeInvoked()
    override fun post(url: URL, requestBody: RequestBody, headers: Map<String, List<String>>, retryWith: String): HttpResponse = shouldNotBeInvoked()
}