package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.SimpleEventBus
import io.github.manamiproject.modb.core.config.BooleanPropertyDelegate
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.DefaultConfigRegistry
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.serde.json.AnimeListJsonStringDeserializer
import io.github.manamiproject.modb.serde.json.DefaultExternalResourceJsonDeserializer
import io.github.manamiproject.modb.serde.json.ExternalResourceJsonDeserializer
import io.github.manamiproject.modb.serde.json.models.Dataset
import kotlinx.coroutines.withContext
import java.net.URI
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.io.path.Path

internal class AnimeCachePopulator(
    private val fileName: String = "anime-offline-database.zip",
    private val uri: URI = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/$fileName"),
    private val parser: ExternalResourceJsonDeserializer<Dataset> = DefaultExternalResourceJsonDeserializer(deserializer = AnimeListJsonStringDeserializer.instance),
    private val eventBus: EventBus = SimpleEventBus,
    private val httpClient: HttpClient = DefaultHttpClient.instance,
    configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
) : CachePopulator<URI, CacheEntry<Anime>> {

    private val isUseLocalFiles by BooleanPropertyDelegate(
        namespace = "manami.cache.useLocalFiles",
        configRegistry = configRegistry,
        default = true
    )

    override suspend fun populate(cache: Cache<URI, CacheEntry<Anime>>) {
        val parsedAnime = if (isUseLocalFiles) {
            val file = Path(fileName)

            val isDownloadFile = if (!file.regularFileExists()) {
                true
            } else {
                val attrs = withContext(LIMITED_FS) {
                    Files.readAttributes(file, BasicFileAttributes::class.java)
                }
                val creationTime = attrs.creationTime()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                ChronoUnit.DAYS.between(creationTime, LocalDate.now()) >= 1L
            }

            if (isDownloadFile) {
                log.info {"Downloading dataset from [$uri], because a local file doesn't exist." }
                httpClient.get(uri.toURL()).body.writeToFile(file)
            }

            log.info {"Populating cache with anime." }

            parser.deserialize(file).data
        } else {
            log.info {"Populating cache with anime from [$uri]." }
            parser.deserialize(uri.toURL()).data
        }

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
        log.info { "Finished populating cache with anime." }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}