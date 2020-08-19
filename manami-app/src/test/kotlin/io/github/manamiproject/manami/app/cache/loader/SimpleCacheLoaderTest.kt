package io.github.manamiproject.manami.app.cache.loader

import io.github.manamiproject.manami.app.cache.MetaDataProviderTestConfig
import io.github.manamiproject.manami.app.cache.TestAnimeConverter
import io.github.manamiproject.manami.app.cache.TestDownloader
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.converter.AnimeConverter
import io.github.manamiproject.modb.core.downloader.Downloader
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

internal class SimpleCacheLoaderTest {

    @Test
    fun `hostname returns the hostname of the given MetaDataProviderConfig`() {
        // given
        val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "example.org"
        }

        val simpleCacheLoader = SimpleCacheLoader(
                config = testConfig,
                downloader = TestDownloader,
                converter = TestAnimeConverter
        )

        // when
        val result = simpleCacheLoader.hostname()

        // then
        assertThat(result).isEqualTo(testConfig.hostname())
    }

    @Test
    fun `correctly load an anime`() {
        // given
        val anime = Anime("Death Note")

        val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun extractAnimeId(url: URL): AnimeId = "1535"
            override fun hostname(): Hostname = "example.org"
        }

        val testDownloader = object: Downloader by TestDownloader {
            override fun download(id: AnimeId, onDeadEntry: (AnimeId) -> Unit): String {
                return if (id == "1535") "{ }" else shouldNotBeInvoked()
            }
        }

        val testConverter = object: AnimeConverter by TestAnimeConverter {
            override fun convert(source: String): Anime {
                return if (source == "{ }") anime else shouldNotBeInvoked()
            }
        }

        val simpleCacheLoader = SimpleCacheLoader(
                config = testConfig,
                downloader = testDownloader,
                converter = testConverter
        )

        // when
        val result = simpleCacheLoader.loadAnime(URL("https://example.org/anime/1535"))

        // then
        assertThat(result).isEqualTo(anime)
    }
}