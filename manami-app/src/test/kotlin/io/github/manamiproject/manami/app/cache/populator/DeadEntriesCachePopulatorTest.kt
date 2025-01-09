package io.github.manamiproject.manami.app.cache.populator

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.MetaDataProviderTestConfig
import io.github.manamiproject.manami.app.cache.TestCacheLoader
import io.github.manamiproject.manami.app.cache.TestConfigRegistry
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.MockServerTestCase
import io.github.manamiproject.modb.test.WireMockServerCreator
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class DeadEntriesCachePopulatorTest: MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Test
    fun `successfully populate cache with dead entries`() {
        // given
        val testCache = DefaultAnimeCache(listOf(TestCacheLoader))

        val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "example.org"
            override fun buildAnimeLink(id: AnimeId): URI = URI("https://${hostname()}/anime/$id")
        }

        val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
            override fun boolean(key: String): Boolean = false
        }

        val cachePopulator = DeadEntriesCachePopulator(
            config = testConfig,
            url = URI("http://localhost:$port/dead-entires/all.json").toURL(),
            configRegistry = testConfigRegistry,
        )

        serverInstance.stubFor(
                get(urlPathEqualTo("/dead-entires/all.json")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("""
                            {
                                "license": {
                                  "name": "GNU Affero General Public License v3.0",
                                  "url": "https://github.com/manami-project/anime-offline-database/blob/master/LICENSE"
                                },
                                "repository": "https://github.com/manami-project/anime-offline-database",
                                "lastUpdate": "2020-01-01",
                                "deadEntries": [
                                    "12449",
                                    "65562"
                                ]
                            }
                            """.trimIndent())
                )
        )

        // when
        runBlocking {
            cachePopulator.populate(testCache)
        }

        // then
        assertThat(testCache.fetch(URI("https://example.org/anime/12449"))).isInstanceOf(DeadEntry::class.java)
        assertThat(testCache.fetch(URI("https://example.org/anime/65562"))).isInstanceOf(DeadEntry::class.java)
    }
}