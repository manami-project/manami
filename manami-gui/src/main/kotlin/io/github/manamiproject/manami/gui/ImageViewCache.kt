package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Empty
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

class ImageViewCache: Cache<URI, CacheEntry<ImageView>> {

    private val entries = ConcurrentHashMap<URI, CacheEntry<ImageView>>()

    override fun fetch(key: URI): CacheEntry<ImageView> {
        return when(val entry = entries[key]) {
            is PresentValue<ImageView>, is Empty<ImageView> -> entry
            null -> createEntry(key)
        }
    }

    override fun populate(key: URI, value: CacheEntry<ImageView>) {
        when {
            !entries.containsKey(key) -> entries[key] = value
            else -> log.warn("Not populating cache with key [{}], because it already exists", key)
        }
    }

    override fun clear() {
        log.info("Clearing cache for thumbnails")
        entries.clear()
    }

    private fun createEntry(uri: URI): CacheEntry<ImageView> {
        log.trace("No cache hit for [{}]. Creating a new entry.", uri)

        val image = Image(uri.toString(), true)
        val cachedImageView = ImageView(image).apply {
            isCache = true
        }

        val value = PresentValue(cachedImageView)
        populate(uri, value)

        return value
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}

object GuiCaches {
    val imageViewCache = ImageViewCache()
}