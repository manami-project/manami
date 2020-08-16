package io.github.manamiproject.manami.cache.loader

import io.github.manamiproject.manami.cache.MetaDataProviderTestConfig
import io.github.manamiproject.manami.cache.TestAnimeConverter
import io.github.manamiproject.manami.cache.TestDownloader
import io.github.manamiproject.manami.cache.TestHttpClient
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.core.httpclient.HttpClient
import io.github.manamiproject.modb.core.httpclient.HttpResponse
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.CURRENTLY
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.SUMMER
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.notify.NotifyConfig
import io.github.manamiproject.modb.notify.NotifyDownloader
import io.github.manamiproject.modb.notify.NotifyRelationsConfig
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URL

internal class NotifyCacheLoaderTest {

    @Test
    fun `hostname returns the hostname of the given MetaDataProviderConfig`() {
        // given
        val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "example.org"
        }

        val notifyCacheLoader = NotifyCacheLoader(
                notifyConfig = testConfig,
                animeDownloader = TestDownloader,
                relationsDownloader = TestDownloader,
                converter = TestAnimeConverter
        )

        // when
        val result = notifyCacheLoader.hostname()

        // then
        assertThat(result).isEqualTo(testConfig.hostname())
    }

    @Test
    fun `correctly load an anime`() {
        // given
        val expectedAnime = Anime(
                _title = "Yahari Ore no Seishun Love Comedy wa Machigatteiru. Kan",
                type = TV,
                episodes = 12,
                status = CURRENTLY,
                animeSeason = AnimeSeason(
                        season = SUMMER,
                        _year = 2020
                ),
                picture = URL("https://media.notify.moe/images/anime/large/3lack4eiR.webp"),
                thumbnail = URL("https://media.notify.moe/images/anime/small/3lack4eiR.webp"),
                duration = Duration(24, MINUTES)
        ).apply {
            addSources(listOf(URL("https://notify.moe/anime/3lack4eiR")))
            addSynonyms(listOf(
                    "My Teen Romantic Comedy SNAFU 3",
                    "My youth romantic comedy is wrong as I expected 3",
                    "Oregairu 3",
                    "Yahari Ore no Seishun Love Comedy wa Machigatteiru. 3rd Season",
                    "やはり俺の青春ラブコメはまちがっている。第3期"))
            addRelations(listOf(URL("https://notify.moe/anime/Pk0AtFmmg")))
            addTags(listOf("comedy", "drama", "romance", "school", "slice of life"))
        }

        val testHttpClient = object: HttpClient by TestHttpClient {
            override fun get(url: URL, headers: Map<String, List<String>>, retryWith: String): HttpResponse {
                val response = when(url.toString()) {
                    "https://notify.moe/api/anime/3lack4eiR" -> loadTestResource("cache_tests/loader/notify/3lack4eiR.json")
                    "https://notify.moe/api/animerelations/3lack4eiR" -> loadTestResource("cache_tests/loader/notify/3lack4eiR_relations.json")
                    else -> shouldNotBeInvoked()
                }

                return HttpResponse(200, response)
            }
        }

        val notifyCacheLoader = NotifyCacheLoader(
                animeDownloader = NotifyDownloader(
                        config = NotifyConfig,
                        httpClient = testHttpClient
                ),
                relationsDownloader = NotifyDownloader(
                        config = NotifyRelationsConfig,
                        httpClient = testHttpClient
                )
        )

        // when
        val result = notifyCacheLoader.loadAnime(URL("https://notify.moe/anime/3lack4eiR"))

        // then
        assertThat(result).isEqualTo(expectedAnime)
    }
}