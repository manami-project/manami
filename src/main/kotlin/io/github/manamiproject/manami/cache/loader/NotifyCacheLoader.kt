package io.github.manamiproject.manami.cache.loader

import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.createDirectory
import io.github.manamiproject.modb.core.extensions.deleteIfExists
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.notify.NotifyConverter
import io.github.manamiproject.modb.notify.NotifyDownloader
import io.github.manamiproject.modb.notify.NotifyRelationsConfig
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

class NotifyCacheLoader(
        private val notifyConfig: MetaDataProviderConfig = NotifyConfig,
        private val animeDownloader: Downloader = NotifyDownloader(config = notifyConfig),
        private val relationsDownloader: Downloader = NotifyDownloader(config = NotifyRelationsConfig),
        private val relationsDir: Path = Files.createTempDirectory("manami-notify_").resolve("relations").createDirectory(),
        private val converter: AnimeConverter = NotifyConverter(relationsDir = relationsDir)
) : CacheLoader {

    override fun loadAnime(url: URL): Anime {
        val id = notifyConfig.extractAnimeId(url)

        loadRelations(id)

        val result = animeDownloader.download(id)
        val anime = converter.convert(result)

        relationsDir.resolve("$id.${notifyConfig.fileSuffix()}").deleteIfExists()

        return anime
    }

    override fun hostname(): Hostname {
        return notifyConfig.hostname()
    }

    private fun loadRelations(id: AnimeId) {
        relationsDownloader.download(id).writeToFile(relationsDir.resolve("$id.${notifyConfig.fileSuffix()}"))
    }
}