package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.dbparser.AnimeDatabaseJsonStringParser
import io.github.manamiproject.modb.dbparser.DatabaseFileParser
import io.github.manamiproject.modb.dbparser.ExternalResourceParser
import java.net.URI
import java.net.URL

internal class AnimeCachePopulator(
        private val url: URL = URL("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database.json"),
        private val parser: ExternalResourceParser<Anime> = DatabaseFileParser(fileParser = AnimeDatabaseJsonStringParser())
) : CachePopulator<URI, Anime?> {

    override fun populate(cache: Cache<URI, Anime?>) {
        log.info("Populating cache with anime from [{}].", url)

        parser.parse(url).forEach { anime ->
            anime.sources.forEach { source ->
                cache.populate(source, anime)
            }
        }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}