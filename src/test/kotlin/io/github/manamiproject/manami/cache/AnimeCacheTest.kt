package io.github.manamiproject.manami.cache

import io.github.manamiproject.manami.cache.loader.CacheLoader
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URL

internal class AnimeCacheTest {

    @Nested
    inner class FetchTests {

        @Test
        fun `externally load entry using a fitting cache loader if the key doesn't exist in the cache`() {
            // given
            var loadExternallyHasBeenTriggered = false

            val testCacheLoader = object: CacheLoader by TestCacheLoader {
                override fun hostname(): Hostname = "example.org"

                override fun loadAnime(url: URL): Anime {
                    loadExternallyHasBeenTriggered = true
                    return Anime("Death Note")
                }
            }
            val cache = AnimeCache(cacheLoader = listOf(testCacheLoader))

            // when
            cache.fetch(URL("https://example.org/anime/1535"))

            // then
            assertThat(loadExternallyHasBeenTriggered).isTrue()
        }

        @Test
        fun `trigger cache hit if the cache has been populated beforehand`() {
            // given
            var timesLoadExternallyHasBeenTriggered = 0
            val source = URL("https://example.org/anime/1535")
            val anime = Anime("Death Note").apply {
                addSources(listOf(source))
            }

            val testCacheLoader = object: CacheLoader by TestCacheLoader {
                override fun hostname(): Hostname = "example.org"

                override fun loadAnime(url: URL): Anime {
                    timesLoadExternallyHasBeenTriggered++
                    return anime
                }
            }
            val cache = AnimeCache(cacheLoader = listOf(testCacheLoader))
            cache.fetch(source)

            // when
            val result = cache.fetch(URL("https://example.org/anime/1535"))

            // then
            assertThat(timesLoadExternallyHasBeenTriggered).isOne()
            assertThat(result).isEqualTo(anime)
        }

        @Test
        fun `return null if the key doesn't exist in the cache and there is no matching cache loader to populate the cache`() {
            // given
            val cache = AnimeCache(cacheLoader = emptyList())

            // when
            val result = cache.fetch(URL("https://example.org/anime/1535"))

            // then
            assertThat(result).isNull()
        }
    }

    @Nested
    inner class PopulateTests {

        @Test
        fun `populate cache`() {
            // given
            val source = URL("https://example.org/anime/1535")
            val anime = Anime("Death Note").apply {
                addSources(listOf(source))
            }

            val cache = AnimeCache(cacheLoader = listOf(TestCacheLoader))

            // when
            cache.populate(source, anime)

            // then
            val result = cache.fetch(source)
            assertThat(result).isEqualTo(anime)
        }

        @Test
        fun `don't override an existing entry`() {
            // given
            val source = URL("https://example.org/anime/1535")
            val anime = Anime("Death Note").apply {
                addSources(listOf(source))
            }

            val cache = AnimeCache(cacheLoader = listOf(TestCacheLoader))
            cache.populate(source, anime)

            val otherAnime = Anime("Different title").apply {
                addSources(listOf(source))
            }

            // when
            cache.populate(source, otherAnime)

            // then
            val result = cache.fetch(source)
            assertThat(result).isEqualTo(anime)
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `clear all entries`() {
            // given
            val source1 = URL("https://example.org/anime/1")
            val anime1 = Anime("Entry1").apply {
                addSources(listOf(source1))
            }

            val source2 = URL("https://example.org/anime/2")
            val anime2 = Anime("Entry2").apply {
                addSources(listOf(source2))
            }

            var loadCacheHasBeenTriggeredFor1 = false
            var loadCacheHasBeenTriggeredFor2 = false

            val testCacheLoader = object: CacheLoader by TestCacheLoader {
                override fun hostname(): Hostname = "example.org"
                override fun loadAnime(url: URL): Anime {
                    return when(url) {
                        source1 -> {
                            loadCacheHasBeenTriggeredFor1 = true
                            anime1
                        }
                        source2 -> {
                            loadCacheHasBeenTriggeredFor2 = true
                            anime2
                        }
                        else -> shouldNotBeInvoked()
                    }
                }
            }
            val cache = AnimeCache(cacheLoader = listOf(testCacheLoader)).apply {
                populate(source1, anime1)
                populate(source1, anime2)
            }

            // when
            cache.clear()

            // then
            cache.fetch(source1)
            cache.fetch(source2)
            assertThat(loadCacheHasBeenTriggeredFor1).isTrue()
            assertThat(loadCacheHasBeenTriggeredFor2).isTrue()
        }
    }
}