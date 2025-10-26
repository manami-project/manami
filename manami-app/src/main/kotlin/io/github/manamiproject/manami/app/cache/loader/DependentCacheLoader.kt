package io.github.manamiproject.manami.app.cache.loader

import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeRawToAnimeTransformer
import io.github.manamiproject.modb.core.anime.DefaultAnimeRawToAnimeTransformer
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.extensions.writeToFile
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import java.net.URI
import java.nio.file.Path
import kotlin.io.path.deleteIfExists

internal class DependentCacheLoader(
    private val config: MetaDataProviderConfig,
    private val animeDownloader: Downloader,
    private val relationsDownloader: Downloader,
    private val relationsDir: Path,
    private val converter: AnimeConverter,
    private val transformer: AnimeRawToAnimeTransformer = DefaultAnimeRawToAnimeTransformer.instance,
) : CacheLoader {

    override suspend fun loadAnime(uri: URI): Anime {
        log.debug { "Loading anime from [$uri]" }

        val id = config.extractAnimeId(uri)

        loadRelations(id)

        val result = animeDownloader.download(id)
        val anime = converter.convert(result)

        relationsDir.resolve("$id.${config.fileSuffix()}").deleteIfExists()

        return transformer.transform(anime)
    }

    override fun hostname(): Hostname = config.hostname()

    private suspend fun loadRelations(id: AnimeId) = relationsDownloader.download(id).writeToFile(relationsDir.resolve("$id.${config.fileSuffix()}"))

    private companion object {
        private val log by LoggerDelegate()
    }
}