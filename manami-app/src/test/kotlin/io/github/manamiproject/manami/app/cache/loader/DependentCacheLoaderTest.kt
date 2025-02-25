package io.github.manamiproject.manami.app.cache.loader

import io.github.manamiproject.manami.app.cache.MetaDataProviderTestConfig
import io.github.manamiproject.manami.app.cache.TestAnimeConverter
import io.github.manamiproject.manami.app.cache.TestDownloader
import io.github.manamiproject.manami.app.cache.TestHttpClient
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeStatus.ONGOING
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.SUMMER
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.anime.ScoreValue
import io.github.manamiproject.modb.notify.NotifyAnimeConverter
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.notify.NotifyDownloader
import io.github.manamiproject.modb.notify.NotifyRelationsConfig
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.URL

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
                title = "Yahari Ore no Seishun Love Comedy wa Machigatteiru. Kan",
                type = TV,
                episodes = 12,
                status = ONGOING,
                animeSeason = AnimeSeason(
                    season = SUMMER,
                    year = 2020,
                ),
                picture = URI("https://media.notify.moe/images/anime/large/3lack4eiR.jpg"),
                thumbnail = URI("https://media.notify.moe/images/anime/small/3lack4eiR.jpg"),
                duration = Duration(24, MINUTES),
                score = ScoreValue(
                    arithmeticGeometricMean = 7.5,
                    arithmeticMean = 7.5,
                    median = 7.5,
                ),
                sources = hashSetOf(
                    URI("https://notify.moe/anime/3lack4eiR"),
                ),
                synonyms = hashSetOf(
                    "My Teen Romantic Comedy SNAFU 3",
                    "My youth romantic comedy is wrong as I expected 3",
                    "Oregairu 3",
                    "Yahari Ore no Seishun Love Comedy wa Machigatteiru. 3rd Season",
                    "やはり俺の青春ラブコメはまちがっている。第3期",
                ),
                relatedAnime = hashSetOf(
                    URI("https://notify.moe/anime/Pk0AtFmmg"),
                ),
                tags = hashSetOf(
                    "comedy",
                    "drama",
                    "romance",
                    "school",
                    "slice of life",
                )
            )

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                    val response = when(url.toString()) {
                        "https://notify.moe/api/anime/3lack4eiR" -> loadTestResource<String>("cache_tests/loader/notify/3lack4eiR.json")
                        "https://notify.moe/api/animerelations/3lack4eiR" -> loadTestResource<String>("cache_tests/loader/notify/3lack4eiR_relations.json")
                        else -> shouldNotBeInvoked()
                    }

                    return HttpResponse(200, response.toByteArray())
                }
            }

            val cacheLoader = DependentCacheLoader(
                config = NotifyConfig,
                animeDownloader = NotifyDownloader(
                    metaDataProviderConfig = NotifyConfig,
                    httpClient = testHttpClient,
                ),
                relationsDownloader = NotifyDownloader(
                    metaDataProviderConfig = NotifyRelationsConfig,
                    httpClient = testHttpClient,
                ),
                converter = NotifyAnimeConverter(relationsDir = tempDir),
                relationsDir = tempDir,
            )

            // when
            val result = cacheLoader.loadAnime(URI("https://notify.moe/anime/3lack4eiR"))

            // then
            assertThat(result).isEqualTo(expectedAnime)
        }
    }
}