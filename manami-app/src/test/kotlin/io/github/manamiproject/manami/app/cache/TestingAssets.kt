package io.github.manamiproject.manami.app.cache

import io.github.manamiproject.manami.app.cache.loader.CacheLoader
import io.github.manamiproject.modb.core.config.*
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.httpclient.RequestBody
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeRaw
import io.github.manamiproject.modb.core.anime.Tag
import io.github.manamiproject.modb.core.httpclient.RetryCase
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import java.net.URI
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

internal object TestCacheLoader : CacheLoader {
    override fun hostname(): Hostname = shouldNotBeInvoked()
    override suspend fun loadAnime(uri: URI): Anime = shouldNotBeInvoked()
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
    override suspend fun convert(rawContent: String): AnimeRaw = shouldNotBeInvoked()
}

internal object TestHttpClient: HttpClient {
    override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse = shouldNotBeInvoked()
    override fun addRetryCases(vararg retryCases: RetryCase): HttpClient = shouldNotBeInvoked()
    override suspend fun post(url: URL, requestBody: RequestBody, headers: Map<String, Collection<String>>): HttpResponse = shouldNotBeInvoked()
}

internal object TestAnimeCache: AnimeCache {
    override val availableMetaDataProvider: Set<Hostname>
        get() = shouldNotBeInvoked()
    override val availableTags: Set<Tag>
        get() = shouldNotBeInvoked()
    override fun allEntries(metaDataProvider: Hostname): Sequence<Anime> = shouldNotBeInvoked()
    override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = shouldNotBeInvoked()
    override suspend fun fetch(key: URI): CacheEntry<Anime> = shouldNotBeInvoked()
    override fun populate(key: URI, value: CacheEntry<Anime>) = shouldNotBeInvoked()
    override fun clear() = shouldNotBeInvoked()
}

internal object TestConfigRegistry: ConfigRegistry {
    override fun boolean(key: String): Boolean = shouldNotBeInvoked()
    override fun double(key: String): Double = shouldNotBeInvoked()
    override fun int(key: String): Int = shouldNotBeInvoked()
    override fun <T : Any> list(key: String): List<T> = shouldNotBeInvoked()
    override fun localDate(key: String): LocalDate = shouldNotBeInvoked()
    override fun localDateTime(key: String): LocalDateTime = shouldNotBeInvoked()
    override fun long(key: String): Long = shouldNotBeInvoked()
    override fun <T : Any> map(key: String): Map<String, T> = shouldNotBeInvoked()
    override fun offsetDateTime(key: String): OffsetDateTime = shouldNotBeInvoked()
    override fun string(key: String): String = shouldNotBeInvoked()
}