package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Empty
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import javafx.scene.image.Image
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

class ImageViewCache: Cache<URI, CacheEntry<Image>> {

    private val entries = ConcurrentHashMap<URI, CacheEntry<Image>>()

    override fun fetch(key: URI): CacheEntry<Image> {
        return when(val entry = entries[key]) {
            is PresentValue<Image>, is Empty<Image> -> entry
            null -> createEntry(key)
        }
    }

    override fun populate(key: URI, value: CacheEntry<Image>) {
        when {
            !entries.containsKey(key) -> entries[key] = value
            else -> log.warn { "Not populating cache with key [$key], because it already exists" }
        }
    }

    override fun clear() {
        log.info { "Clearing cache for thumbnails" }
        entries.clear()
    }

    private fun createEntry(uri: URI): CacheEntry<Image> {
        log.trace { "No cache hit for [$uri]. Creating a new entry." }

        val image = Image(uri.toString(), true)
        val value = PresentValue(image)
        populate(uri, value)

        return value
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}

object GuiCaches {
    val imageCache = ImageViewCache()
}