package io.github.manamiproject.manami.cache.loader

import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.models.Anime
import java.net.URL

class SimpleCacheLoader(
        private val config: MetaDataProviderConfig,
        private val downloader: Downloader,
        private val converter: AnimeConverter
) : CacheLoader {

    override fun hostname(): Hostname = config.hostname()

    override fun loadAnime(url: URL): Anime {
        val id = config.extractAnimeId(url)
        val rawContent = downloader.download(id)
        return converter.convert(rawContent)
    }
}