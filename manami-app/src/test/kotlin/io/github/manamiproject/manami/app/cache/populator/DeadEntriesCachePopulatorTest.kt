package io.github.manamiproject.manami.app.cache.populator

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.DeadEntry
import io.github.manamiproject.manami.app.cache.MetaDataProviderTestConfig
import io.github.manamiproject.manami.app.cache.TestCacheLoader
import io.github.manamiproject.modb.core.config.AnimeId
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.config.MetaDataProviderConfig
import io.github.manamiproject.modb.test.MockServerTestCase
import io.github.manamiproject.modb.test.WireMockServerCreator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.URL

internal class DeadEntriesCachePopulatorTest: MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Test
    fun `successfully populate cache with dead entries`() {
        // given
        val testCache = AnimeCache(listOf(TestCacheLoader))

        val testConfig = object: MetaDataProviderConfig by MetaDataProviderTestConfig {
            override fun hostname(): Hostname = "example.org"
            override fun buildAnimeLink(id: AnimeId): URI = URI("https://${hostname()}/anime/$id")
        }

        val animeCachePopulator = DeadEntriesCachePopulator(
                config = testConfig,
                url = URL("http://localhost:$port/dead-entires/all.json")
        )

        serverInstance.stubFor(
                get(urlPathEqualTo("/dead-entires/all.json")).willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(200)
                                .withBody("""
                                    {
                                        "deadEntries": [
                                            "12449",
                                            "65562"
                                        ]
                                    }
                                """.trimIndent())
                )
        )

        // when
        animeCachePopulator.populate(testCache)

        // then
        assertThat(testCache.fetch(URI("https://example.org/anime/12449"))).isInstanceOf(DeadEntry::class.java)
        assertThat(testCache.fetch(URI("https://example.org/anime/65562"))).isInstanceOf(DeadEntry::class.java)
    }
}