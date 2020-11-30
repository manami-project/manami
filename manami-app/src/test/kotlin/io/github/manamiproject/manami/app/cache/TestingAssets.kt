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
    override fun download(id: AnimeId, onDeadEntry: (AnimeId) -> Unit): String = shouldNotBeInvoked()
}

internal object TestAnimeConverter: AnimeConverter {
    override fun convert(rawContent: String): Anime = shouldNotBeInvoked()
}

internal object TestHttpClient: HttpClient {
    override fun executeRetryable(retryWith: String, func: () -> HttpResponse): HttpResponse  = shouldNotBeInvoked()
    override fun get(url: URL, headers: Map<String, List<String>>, retryWith: String): HttpResponse  = shouldNotBeInvoked()
    override fun post(url: URL, requestBody: RequestBody, headers: Map<String, List<String>>, retryWith: String): HttpResponse = shouldNotBeInvoked()
}