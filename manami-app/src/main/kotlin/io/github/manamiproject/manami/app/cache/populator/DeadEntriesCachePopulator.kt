package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Empty
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.dbparser.DatabaseFileParser
import io.github.manamiproject.modb.dbparser.DeadEntriesJsonStringParser
import io.github.manamiproject.modb.dbparser.ExternalResourceParser
import java.net.URI
import java.net.URL

internal class DeadEntriesCachePopulator(
        private val config: MetaDataProviderConfig,
        private val url: URL,
        private val parser: ExternalResourceParser<AnimeId> = DatabaseFileParser(fileParser = DeadEntriesJsonStringParser())
) : CachePopulator<URI, CacheEntry<Anime>> {

    override fun populate(cache: Cache<URI, CacheEntry<Anime>>) {
        log.info("Populating cache with dead entries from [{}]", config.hostname())

        parser.parse(url).forEach { animeId ->
            val source = config.buildAnimeLink(animeId)
            cache.populate(source, Empty())
        }

        log.info("Finished populating cache with dead entries from [{}]", config.hostname())
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}