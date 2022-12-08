package io.github.manamiproject.manami.app.cache.loader

import io.github.manamiproject.manami.app.cache.MetaDataProviderTestConfig
import io.github.manamiproject.manami.app.cache.TestAnimeConverter
import io.github.manamiproject.manami.app.cache.TestDownloader
import io.github.manamiproject.manami.app.cache.TestHttpClient
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.ONGOING
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.SUMMER
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.notify.NotifyConverter
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
                _title = "Yahari Ore no Seishun Love Comedy wa Machigatteiru. Kan",
                type = TV,
                episodes = 12,
                status = ONGOING,
                animeSeason = AnimeSeason(
                    season = SUMMER,
                    year = 2020,
                ),
                picture = URI("https://media.notify.moe/images/anime/large/3lack4eiR.webp"),
                thumbnail = URI("https://media.notify.moe/images/anime/small/3lack4eiR.webp"),
                duration = Duration(24, MINUTES),
            ).apply {
                addSources(URI("https://notify.moe/anime/3lack4eiR"))
                addSynonyms(
                        "My Teen Romantic Comedy SNAFU 3",
                        "My youth romantic comedy is wrong as I expected 3",
                        "Oregairu 3",
                        "Yahari Ore no Seishun Love Comedy wa Machigatteiru. 3rd Season",
                        "やはり俺の青春ラブコメはまちがっている。第3期",
                )
                addRelations(URI("https://notify.moe/anime/Pk0AtFmmg"))
                addTags(
                    "comedy",
                    "drama",
                    "romance",
                    "school",
                    "slice of life",
                )
            }

            val testHttpClient = object: HttpClient by TestHttpClient {
                override suspend fun get(url: URL, headers: Map<String, Collection<String>>, retryWith: String): HttpResponse {
                    val response = when(url.toString()) {
                        "https://notify.moe/api/anime/3lack4eiR" -> loadTestResource("cache_tests/loader/notify/3lack4eiR.json")
                        "https://notify.moe/api/animerelations/3lack4eiR" -> loadTestResource("cache_tests/loader/notify/3lack4eiR_relations.json")
                        else -> shouldNotBeInvoked()
                    }

                    return HttpResponse(200, response)
                }
            }

            val cacheLoader = DependentCacheLoader(
                config = NotifyConfig,
                animeDownloader = NotifyDownloader(
                    config = NotifyConfig,
                    httpClient = testHttpClient,
                ),
                relationsDownloader = NotifyDownloader(
                    config = NotifyRelationsConfig,
                    httpClient = testHttpClient,
                ),
                converter = NotifyConverter(relationsDir = tempDir),
                relationsDir = tempDir,
            )

            // when
            val result = cacheLoader.loadAnime(URI("https://notify.moe/anime/3lack4eiR"))

            // then
            assertThat(result).isEqualTo(expectedAnime)
        }
    }
}