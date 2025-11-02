package io.github.manamiproject.manami.gui.cache

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import org.jetbrains.skia.Image
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

internal class ImageCache(
    private val httpClient: HttpClient = DefaultHttpClient()
): Cache<URI, CacheEntry<ImageBitmap>> {

    private val entries = ConcurrentHashMap<URI, CacheEntry<ImageBitmap>>()
    private lateinit var defaultImage: ImageBitmap

    override suspend fun fetch(key: URI): CacheEntry<ImageBitmap> {
        return when(val entry = entries[key]) {
            is PresentValue<ImageBitmap>, is DeadEntry<ImageBitmap> -> entry
            null -> createEntry(key)
        }
    }

    override fun populate(key: URI, value: CacheEntry<ImageBitmap>) {
        when {
            !entries.containsKey(key) -> entries[key] = value
            else -> log.warn { "Not populating cache with key [$key], because it already exists" }
        }
    }

    override fun clear() {
        log.info { "Clearing cache for thumbnails" }
        entries.clear()
    }

    private suspend fun createEntry(uri: URI): CacheEntry<ImageBitmap> {
        log.trace { "No cache hit for [$uri]. Creating a new entry." }

        val value = try {
            val bytes = httpClient.get(uri.toURL()).bodyAsByteArray()
            val skiaImage = Image.makeFromEncoded(bytes)
            val imageBitmap = skiaImage.toComposeImageBitmap()
            PresentValue(imageBitmap)
        } catch (e: Exception) {
            log.error(e) { "Error when trying to retrieve thumbnail from [$uri]. Retrying with default image." }
            PresentValue(fetchDefaultImage())
        }

        populate(uri, value)

        return value
    }

    fun fetchDefaultImage(): ImageBitmap {
        if (!this::defaultImage.isInitialized) {
            val bytes = NO_PICTURE.toURL().readBytes()
            val skiaImage = Image.makeFromEncoded(bytes)
            defaultImage = skiaImage.toComposeImageBitmap()
        }

        return defaultImage
    }

    internal companion object {
        private val log by LoggerDelegate()

        /**
         * Singleton of [ImageCache]
         * @since 4.0.0
         */
        val instance: ImageCache by lazy { ImageCache() }
    }
}