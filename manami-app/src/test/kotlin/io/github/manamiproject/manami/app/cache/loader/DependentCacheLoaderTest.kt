package io.github.manamiproject.manami.app.cache.loader

import io.github.manamiproject.manami.app.cache.MetaDataProviderTestConfig
import io.github.manamiproject.manami.app.cache.TestAnimeConverter
import io.github.manamiproject.manami.app.cache.TestDownloader
import io.github.manamiproject.manami.app.cache.TestHttpClient
import io.github.manamiproject.modb.anisearch.AnisearchAnimeConverter
import io.github.manamiproject.modb.anisearch.AnisearchConfig
import io.github.manamiproject.modb.anisearch.AnisearchDownloader
import io.github.manamiproject.modb.anisearch.AnisearchRelationsConfig
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.SUMMER
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.anime.ScoreValue
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import java.net.URL
import kotlin.test.Test

internal class DependentCacheLoaderTest {

    @Test
    fun `hostname returns the hostname of the given MetaDataProviderConfig`() {
        tempDirectory {
            // given
            val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
                override fun hostname(): Hostname = "example.org"
            }

            val cacheLoader = DependentCacheLoader(
                config = testConfig,
                animeDownloader = TestDownloader,
                relationsDownloader = TestDownloader,
                converter = TestAnimeConverter,
                relationsDir = tempDir,
            )

            // when
            val result = cacheLoader.hostname()

            // then
            assertThat(result).isEqualTo(testConfig.hostname())
        }
    }

    @Test
    fun `correctly load an anime`() {
        tempDirectory {
            // given
            val expectedAnime = Anime(
                title = "Yahari Ore no Seishun Lovecome wa Machigatte Iru. Kan",
                type = TV,
                episodes = 12,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = SUMMER,
                    year = 2020,
                ),
                picture = URI("https://cdn.anisearch.com/images/anime/cover/14/14263_600.webp"),
                thumbnail = URI("https://cdn.anisearch.com/images/anime/cover/14/14263_300.webp"),
                duration = Duration(24, MINUTES),
                score = ScoreValue(
                    arithmeticGeometricMean = 8.346938775510203,
                    arithmeticMean = 8.346938775510203,
                    median = 8.346938775510203,
                ),
                sources = hashSetOf(
                    URI("https://anisearch.com/anime/14263"),
                ),
                synonyms = hashSetOf(
                    "My Teen Romantic Comedy SNAFU : Climax",
                    "My Teen Romantic Comedy SNAFU: Climax",
                    "My Teen Romantic Comedy SNAFU: Climax!",
                    "My Teen Romantic Comedy: SNAFU Climax!",
                    "My Teenage RomCom SNAFU 3",
                    "My Youth Romantic Comedy Is Wrong as I Expected 3",
                    "Oregairu 3",
                    "Yahari Ore no Seishun Love Comedy wa Machigatteiru. Kan",
                    "やはり俺の青春ラブコメはまちがっている。完",
                ),
                relatedAnime = hashSetOf(
                    URI("https://anisearch.com/anime/15934"),
                    URI("https://anisearch.com/anime/9606"),
                ),
                studios = hashSetOf(
                    "feel.",
                ),
                tags = hashSetOf(
                    "club",
                    "comedy",
                    "coming of age",
                    "delinquent",
                    "drama",
                    "genius",
                    "hero of strong character",
                    "high school",
                    "present",
                    "romance",
                    "school",
                    "slice of life",
                    "slice of life drama",
                    "tsundere",
                    "verbal comedy",
                )
            )

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                    val response = when(url.toString()) {
                        "https://anisearch.com/anime/14263" -> loadTestResource<String>("cache_tests/loader/dependent/14263.html")
                        "https://anisearch.com/anime/14263/relations" -> loadTestResource<String>("cache_tests/loader/dependent/14263_relations.html")
                        else -> shouldNotBeInvoked()
                    }

                    return HttpResponse(200, response.toByteArray())
                }
            }

            val cacheLoader = DependentCacheLoader(
                config = AnisearchConfig,
                animeDownloader = AnisearchDownloader(
                    metaDataProviderConfig = AnisearchConfig,
                    httpClient = testHttpClient,
                ),
                relationsDownloader = AnisearchDownloader(
                    metaDataProviderConfig = AnisearchRelationsConfig,
                    httpClient = testHttpClient,
                ),
                converter = AnisearchAnimeConverter(relationsDir = tempDir),
                relationsDir = tempDir,
            )

            // when
            val result = cacheLoader.loadAnime(URI("https://anisearch.com/anime/14263"))

            // then
            assertThat(result).isEqualTo(expectedAnime)
        }
    }
}