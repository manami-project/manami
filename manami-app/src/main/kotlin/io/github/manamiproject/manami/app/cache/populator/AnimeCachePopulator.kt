package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.state.events.EventBus
import io.github.manamiproject.manami.app.state.events.SimpleEventBus
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.dbparser.AnimeDatabaseJsonStringParser
import io.github.manamiproject.modb.dbparser.DatabaseFileParser
import io.github.manamiproject.modb.dbparser.ExternalResourceParser
import java.net.URI

internal class AnimeCachePopulator(
    private val uri: URI = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/anime-offline-database-minified.json"),
    private val parser: ExternalResourceParser<Anime> = DatabaseFileParser(fileParser = AnimeDatabaseJsonStringParser()),
    private val eventBus: EventBus = SimpleEventBus,
) : CachePopulator<URI, CacheEntry<Anime>> {

    override fun populate(cache: Cache<URI, CacheEntry<Anime>>) {
        log.info {"Populating cache with anime from [$uri]." }

        val parsedAnime =parser.parse(uri.toURL())

        parsedAnime.forEach { anime ->
            anime.sources.forEach { source ->
                cache.populate(source, PresentValue(anime))
            }
        }

        val numberOfEntriesPerMetaDataProvider = parsedAnime.flatMap { anime -> anime.sources.map { it to anime } }
            .groupBy { it.first.host }
            .map { (key, value) ->
                key to value.size
            }.toMap()

        eventBus.post(NumberOfEntriesPerMetaDataProviderEvent(numberOfEntriesPerMetaDataProvider))

        eventBus.post(CachePopulatorFinishedEvent)
        log.info { "Finished populating cache with anime from [$uri]." }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}