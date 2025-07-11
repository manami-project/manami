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
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.serde.json.deserializer.AnimeFromJsonLinesInputStreamDeserializer
import io.github.manamiproject.modb.serde.json.deserializer.Deserializer
import io.github.manamiproject.modb.serde.json.deserializer.FromRegularFileDeserializer
import io.github.manamiproject.modb.serde.json.deserializer.FromUrlDeserializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.io.path.Path

internal class AnimeCachePopulator(
    private val fileName: String = "anime-offline-database.jsonl.zst",
    private val uri: URI = URI("https://github.com/manami-project/anime-offline-database/releases/download/latest/$fileName"),
    private val fileDeserializer: Deserializer<RegularFile, Flow<Anime>> = FromRegularFileDeserializer(deserializer = AnimeFromJsonLinesInputStreamDeserializer.instance),
    private val urlDeserializer: Deserializer<URL, Flow<Anime>> = FromUrlDeserializer(deserializer = AnimeFromJsonLinesInputStreamDeserializer.instance),
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
        val animeFlow = if (isUseLocalFiles) {
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
                httpClient.get(uri.toURL()).bodyAsByteArray().writeToFile(file)
            }

            log.info {"Populating cache with anime." }

            fileDeserializer.deserialize(file)
        } else {
            log.info {"Populating cache with anime from [$uri]." }
            urlDeserializer.deserialize(uri.toURL())
        }

        val numberOfEntriesPerMetaDataProvider = mutableMapOf<Hostname, Int>()

        animeFlow.collect { anime ->
            anime.sources.forEach { source ->
                cache.populate(source, PresentValue(anime))
                numberOfEntriesPerMetaDataProvider[source.host]?.inc() ?: { numberOfEntriesPerMetaDataProvider[source.host] = 1 }.invoke()
            }
        }

        eventBus.post(NumberOfEntriesPerMetaDataProviderEvent(numberOfEntriesPerMetaDataProvider))

        eventBus.post(CachePopulatorFinishedEvent)
        log.info { "Finished populating cache with anime." }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}