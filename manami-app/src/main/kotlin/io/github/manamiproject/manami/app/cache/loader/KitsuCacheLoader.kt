package io.github.manamiproject.manami.app.cache.loader

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.createDirectory
import io.github.manamiproject.modb.core.extensions.deleteIfExists
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.kitsu.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

internal class KitsuCacheLoader(
        private val kitsuConfig: MetaDataProviderConfig = KitsuConfig,
        private val animeDownloader: Downloader = KitsuDownloader(config = kitsuConfig),
        private val relationsDownloader: Downloader = KitsuDownloader(config = KitsuRelationsConfig),
        private val tagsDownloader: Downloader = KitsuDownloader(config = KitsuTagsConfig),
        private val tempFolder: Path = Files.createTempDirectory("manami-kitsu_"),
        private val relationsDir: Path = tempFolder.resolve("relations").createDirectory(),
        private val tagsDir: Path = tempFolder.resolve("tags").createDirectory(),
        private val converter: AnimeConverter = KitsuConverter(
                relationsDir = relationsDir,
                tagsDir = tagsDir
        )
) : CacheLoader {

    override fun loadAnime(uri: URI): Anime {
        log.debug("Loading anime from [{}]", uri)

        val id = kitsuConfig.extractAnimeId(uri)

        val job1 = GlobalScope.async { loadRelations(id) }
        val job2 = GlobalScope.async { loadTags(id) }

        runBlocking {
            job1.join()
            job2.join()
        }

        val result = animeDownloader.download(id)
        val anime = converter.convert(result)

        relationsDir.resolve("$id.${kitsuConfig.fileSuffix()}").deleteIfExists()
        tagsDir.resolve("$id.${kitsuConfig.fileSuffix()}").deleteIfExists()

        return anime
    }

    override fun hostname(): Hostname {
        return kitsuConfig.hostname()
    }

    private fun loadRelations(id: AnimeId) {
        relationsDownloader.download(id).writeToFile(relationsDir.resolve("$id.${kitsuConfig.fileSuffix()}"))
    }

    private fun loadTags(id: AnimeId) {
        tagsDownloader.download(id).writeToFile(tagsDir.resolve("$id.${kitsuConfig.fileSuffix()}"))
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}