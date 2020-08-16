package io.github.manamiproject.manami.cache.populator

import io.github.manamiproject.manami.cache.Cache
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.dbparser.AnimeDatabaseJsonStringParser
import io.github.manamiproject.modb.dbparser.DatabaseFileParser
import io.github.manamiproject.modb.dbparser.ExternalResourceParser
import java.net.URL

class AnimeCachePopulator(
        private val url: URL = URL("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database.json"),
        private val parser: ExternalResourceParser<Anime> = DatabaseFileParser(fileParser = AnimeDatabaseJsonStringParser())
) : CachePopulator<URL, Anime?> {

    override fun populate(cache: Cache<URL, Anime?>) {
        parser.parse(url).forEach { anime ->
            anime.sources.forEach { source ->
                cache.populate(source, anime)
            }
        }
    }
}