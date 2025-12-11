package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.versioning.ResourceBasedVersionProvider
import io.github.manamiproject.manami.app.versioning.SemanticVersion
import io.github.manamiproject.manami.app.versioning.VersionProvider
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeMedia.NO_PICTURE
import io.github.manamiproject.modb.core.anime.AnimeType
import io.github.manamiproject.modb.core.config.FileSuffix
import io.github.manamiproject.modb.core.extensions.RegularFile
import io.github.manamiproject.modb.core.extensions.fileSuffix
import io.github.manamiproject.modb.core.extensions.regularFileExists
import io.github.manamiproject.modb.core.extensions.toLifecycleAwareInputStream
import io.github.manamiproject.modb.core.json.Json
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.net.URI
import kotlin.io.path.Path
import kotlin.io.path.inputStream

internal class JsonFileParser(
    private val cache: AnimeCache = DefaultAnimeCache.instance,
    private val versionProvider: VersionProvider = ResourceBasedVersionProvider,
) : Parser<ManamiFile> {

    override fun handlesSuffix(): FileSuffix = "json"

    override suspend fun parse(file: RegularFile): ManamiFile {
        log.info { "Parsing JSON file." }

        require(file.regularFileExists()) { "Given path [${file.toAbsolutePath()}] is either not a file or doesn't exist." }
        require(file.fileSuffix() == handlesSuffix()) { "Parser doesn't support given file suffix." }

        val serializableManamiFile = Json.parseJson<SerializableManamiFile>(file.inputStream().toLifecycleAwareInputStream())!!
        val version = SemanticVersion(serializableManamiFile.version)
        require(version == minVersion || version.isNewerThan(minVersion)) { "Unable to parse manami file older than $minVersion" }
        require(version == versionProvider.version() || version.isOlderThan(versionProvider.version())) { "Cannot open a file created with version [${version}] in manami [${versionProvider.version()}]" }

        val animeList = serializableManamiFile.animeListEntries
            .map {
                val thumbnail = when (val cacheEntry: CacheEntry<Anime> = cache.fetch(URI(it.link))) {
                    is DeadEntry<*> -> NO_PICTURE
                    is PresentValue<*> -> (cacheEntry as PresentValue<Anime>).value.picture
                }

                val checkedEpisodes = if (it.episodes.toIntOrNull() == null || it.episodes.toInt() < 0) {
                    throw IllegalStateException("Episodes value [${it.episodes}] for [${it.title} - ${it.link}] is either not numeric or invalid.")
                } else {
                    it.episodes.toInt()
                }

                AnimeListEntry(
                    link = Link(URI(it.link)),
                    title = it.title,
                    thumbnail = thumbnail,
                    episodes = checkedEpisodes,
                    type = AnimeType.of(it.type),
                    location = Path(it.location),
                )
            }

        val watchList = serializableManamiFile.watchListEntries
            .map { cache.fetch(URI(it)) }
            .filterIsInstance<PresentValue<Anime>>()
            .map { WatchListEntry(it.value) }
        val ignoreList = serializableManamiFile.ignoreListEntries
            .map { cache.fetch(URI(it)) }
            .filterIsInstance<PresentValue<Anime>>()
            .map { IgnoreListEntry(it.value) }

        log.info { "Finished parsing JSON file." }

        return ManamiFile(
            version = SemanticVersion(serializableManamiFile.version),
            animeListEntries = animeList,
            watchListEntries = watchList,
            ignoreListEntries = ignoreList,
        )
    }

    companion object {
        private val log by LoggerDelegate()
        private val minVersion = SemanticVersion("4.0.0")

        /**
         * Singleton of [JsonFileParser]
         * @since 4.0.0
         */
        val instance: JsonFileParser by lazy { JsonFileParser() }
    }
}
