package io.github.manamiproject.manami.app.cache.loader

import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.logging.LoggerDelegate
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeRawToAnimeTransformer
import io.github.manamiproject.modb.core.anime.DefaultAnimeRawToAnimeTransformer
import kotlinx.coroutines.runBlocking
import java.net.URI

internal class SimpleCacheLoader(
    private val config: MetaDataProviderConfig,
    private val downloader: Downloader,
    private val converter: AnimeConverter,
    private val transformer: AnimeRawToAnimeTransformer = DefaultAnimeRawToAnimeTransformer.instance,
) : CacheLoader {

    override fun hostname(): Hostname = config.hostname()

    override fun loadAnime(uri: URI): Anime {
        log.debug { "Loading anime from [$uri]" }

        val id = config.extractAnimeId(uri)
        val rawContent = runBlocking { downloader.download(id) }
        return runBlocking { transformer.transform(converter.convert(rawContent)) }
    }

    private companion object {
        private val log by LoggerDelegate()
    }
}