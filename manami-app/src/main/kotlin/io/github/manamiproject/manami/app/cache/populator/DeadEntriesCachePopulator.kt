package io.github.manamiproject.manami.app.cache.populator

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.modb.core.config.BooleanPropertyDelegate
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.DefaultConfigRegistry
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.coroutines.ModbDispatchers.LIMITED_FS
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.httpclient.DefaultHttpClient
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.serde.json.DeadEntriesJsonStringDeserializer
import io.github.manamiproject.modb.serde.json.DefaultExternalResourceJsonDeserializer
import io.github.manamiproject.modb.serde.json.ExternalResourceJsonDeserializer
import io.github.manamiproject.modb.serde.json.models.DeadEntries
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.attribute.BasicFileAttributes
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.io.path.Path

internal class DeadEntriesCachePopulator(
    private val config: MetaDataProviderConfig,
    private val url: URL,
    private val parser: ExternalResourceJsonDeserializer<DeadEntries> = DefaultExternalResourceJsonDeserializer(deserializer = DeadEntriesJsonStringDeserializer.instance),
    private val httpClient: HttpClient = DefaultHttpClient.instance,
    configRegistry: ConfigRegistry = DefaultConfigRegistry.instance,
) : CachePopulator<URI, CacheEntry<Anime>> {

    private val isUseLocalFiles by BooleanPropertyDelegate(
        namespace = "manami.cache.useLocalFiles",
        configRegistry = configRegistry,
        default = true
    )

    override suspend fun populate(cache: Cache<URI, CacheEntry<Anime>>) {
        log.info { "Populating cache with dead entries from [${config.hostname()}]" }

        val parsedData = if (isUseLocalFiles) {
            val file = Path("${config.hostname()}.zip")

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
                log.info {"Downloading dead entries file from [$url], because a local file doesn't exist." }
                httpClient.get(url).body.writeToFile(file)
            }

            parser.deserialize(file)
        } else {
            parser.deserialize(url)
        }

        parsedData.deadEntries.forEach { animeId ->
            val source = config.buildAnimeLink(animeId)
            cache.populate(source, DeadEntry())
        }

        log.info { "Finished populating cache with dead entries from [${config.hostname()}]" }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}