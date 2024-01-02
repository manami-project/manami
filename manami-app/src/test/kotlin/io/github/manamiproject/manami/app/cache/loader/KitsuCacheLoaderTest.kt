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
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.kitsu.KitsuDownloader
import io.github.manamiproject.modb.kitsu.KitsuRelationsConfig
import io.github.manamiproject.modb.kitsu.KitsuTagsConfig
import io.github.manamiproject.modb.test.loadTestResource
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.URL

internal class KitsuCacheLoaderTest {

    @Test
    fun `hostname returns the hostname of the given MetaDataProviderConfig`() {
        // given
        val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "example.org"
        }

        val kitsuCacheLoader = KitsuCacheLoader(
            kitsuConfig = testConfig,
            animeDownloader = TestDownloader,
            relationsDownloader = TestDownloader,
            tagsDownloader = TestDownloader,
            converter = TestAnimeConverter,
        )

        // when
        val result = kitsuCacheLoader.hostname()

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
            status = ONGOING,
            animeSeason = AnimeSeason(
                season = SUMMER,
                year = 2020,
            ),
            picture = URI("https://media.kitsu.io/anime/poster_images/42194/small.jpg?1592953088"),
            thumbnail = URI("https://media.kitsu.io/anime/poster_images/42194/tiny.jpg?1592953088"),
            duration = Duration(24, MINUTES),
        ).apply {
            addSources(URI("https://kitsu.io/anime/42194"))
            addSynonyms(
                    "My Teen Romantic Comedy SNAFU 3",
                    "My Teen Romantic Comedy SNAFU Climax",
                    "My youth romantic comedy is wrong as I expected 3",
                    "Oregairu 3",
                    "やはり俺の青春ラブコメはまちがっている。完",
            )
            addRelations(URI("https://kitsu.io/anime/8478"))
            addTags(
                "asia",
                "comedy",
                "drama",
                "earth",
                "friendship",
                "high school",
                "japan",
                "present",
                "romance",
                "slice of life",
            )
        }

        val testHttpClient = object: HttpClient by TestHttpClient {
            override suspend fun get(url: URL, headers: Map<String, Collection<String>>): HttpResponse {
                val response = when(url.toString()) {
                    "https://kitsu.io/api/edge/anime/42194" -> loadTestResource("cache_tests/loader/kitsu/42194.json")
                    "https://kitsu.io/api/edge/media-relationships?filter[source_id]=42194&filter[source_type]=Anime&include=destination&sort=role" -> loadTestResource("cache_tests/loader/kitsu/42194_relations.json")
                    "https://kitsu.io/api/edge/anime/42194/categories" -> loadTestResource("cache_tests/loader/kitsu/42194_tags.json")
                    else -> shouldNotBeInvoked()
                }

                return HttpResponse(200, response.toByteArray())
            }
        }

        val notifyCacheLoader = KitsuCacheLoader(
                animeDownloader = KitsuDownloader(
                    config = KitsuConfig,
                    httpClient = testHttpClient
                ),
                relationsDownloader = KitsuDownloader(
                    config = KitsuRelationsConfig,
                    httpClient = testHttpClient
                ),
                tagsDownloader = KitsuDownloader(
                    config = KitsuTagsConfig,
                    httpClient = testHttpClient
                ),
        )

        // when
        val result = notifyCacheLoader.loadAnime(URI("https://kitsu.io/anime/42194"))

        // then
        assertThat(result).isEqualTo(expectedAnime)
    }
}