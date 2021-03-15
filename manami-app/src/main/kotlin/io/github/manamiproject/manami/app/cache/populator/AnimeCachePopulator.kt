package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.Present
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.dbparser.AnimeDatabaseJsonStringParser
import io.github.manamiproject.modb.dbparser.DatabaseFileParser
import io.github.manamiproject.modb.dbparser.ExternalResourceParser
import java.net.URI

internal class AnimeCachePopulator(
    private val uri: URI = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database.json"),
    private val parser: ExternalResourceParser<Anime> = DatabaseFileParser(fileParser = AnimeDatabaseJsonStringParser())
) : CachePopulator<URI, CacheEntry<Anime>> {

    override fun populate(cache: Cache<URI, CacheEntry<Anime>>) {
        log.info("Populating cache with anime from [{}].", uri)

        parser.parse(uri.toURL()).forEach { anime ->
            anime.sources.forEach { source ->
                cache.populate(source, Present(anime))
            }
        }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}