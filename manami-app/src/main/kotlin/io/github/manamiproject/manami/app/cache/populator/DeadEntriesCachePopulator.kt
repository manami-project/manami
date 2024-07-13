package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.serde.json.DeadEntriesJsonStringDeserializer
import io.github.manamiproject.modb.serde.json.DefaultExternalResourceJsonDeserializer
import io.github.manamiproject.modb.serde.json.ExternalResourceJsonDeserializer
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.net.URL

internal class DeadEntriesCachePopulator(
    private val config: MetaDataProviderConfig,
    private val url: URL,
    private val parser: ExternalResourceJsonDeserializer<DeadEntries> = DefaultExternalResourceJsonDeserializer(deserializer = DeadEntriesJsonStringDeserializer.instance),
) : CachePopulator<URI, CacheEntry<Anime>> {

    override fun populate(cache: Cache<URI, CacheEntry<Anime>>) {
        log.info { "Populating cache with dead entries from [${config.hostname()}]" }

        runBlocking {
            parser.deserialize(url).deadEntries.forEach { animeId ->
                val source = config.buildAnimeLink(animeId)
                cache.populate(source, DeadEntry())
            }
        }

        log.info { "Finished populating cache with dead entries from [${config.hostname()}]" }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}