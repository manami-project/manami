package io.github.manamiproject.manami.app.cache

import io.github.manamiproject.manami.app.cache.loader.CacheLoader
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI

internal class AnimeCacheTest {

    @Nested
    inner class FetchTests {

        @Test
        fun `externally load entry using a fitting cache loader if the key doesn't exist in the cache`() {
            // given
            var loadExternallyHasBeenTriggered = false

            val testCacheLoader = object: CacheLoader by TestCacheLoader {
                override fun hostname(): Hostname = "example.org"

                override fun loadAnime(uri: URI): Anime {
                    loadExternallyHasBeenTriggered = true
                    return Anime("Death Note")
                }
            }
            val cache = AnimeCache(cacheLoader = listOf(testCacheLoader))

            // when
            cache.fetch(URI("https://example.org/anime/1535"))

            // then
            assertThat(loadExternallyHasBeenTriggered).isTrue()
        }

        @Test
        fun `trigger cache hit if the cache has been populated beforehand`() {
            // given
            var timesLoadExternallyHasBeenTriggered = 0
            val source = URI("https://example.org/anime/1535")
            val anime = Anime("Death Note").apply {
                addSources(source)
            }

            val testCacheLoader = object: CacheLoader by TestCacheLoader {
                override fun hostname(): Hostname = "example.org"

                override fun loadAnime(uri: URI): Anime {
                    timesLoadExternallyHasBeenTriggered++
                    return anime
                }
            }
            val cache = AnimeCache(cacheLoader = listOf(testCacheLoader))
            cache.fetch(source)

            // when
            val result = cache.fetch(URI("https://example.org/anime/1535"))

            // then
            assertThat(timesLoadExternallyHasBeenTriggered).isOne()
            assertThat(result).isInstanceOf(PresentValue::class.java)
            assertThat((result as PresentValue).value).isEqualTo(anime)
        }

        @Test
        fun `a key can also return null`() {
            // given
            val source = URI("https://example.org/anime/1535")

            val cache = AnimeCache(cacheLoader = listOf(TestCacheLoader))
            cache.populate(source, Empty())

            // when
            val result = cache.fetch(URI("https://example.org/anime/1535"))

            // then
            assertThat(result).isInstanceOf(Empty::class.java)
        }

        @Test
        fun `return null if the key doesn't exist in the cache and there is no matching cache loader to populate the cache`() {
            // given
            val cache = AnimeCache(cacheLoader = emptyList())

            // when
            val result = cache.fetch(URI("https://example.org/anime/1535"))

            // then
            assertThat(result).isInstanceOf(Empty::class.java)
        }
    }

    @Nested
    inner class PopulateTests {

        @Test
        fun `populate cache`() {
            // given
            val source = URI("https://example.org/anime/1535")
            val anime = Anime("Death Note").apply {
                addSources(source)
            }

            val cache = AnimeCache(cacheLoader = listOf(TestCacheLoader))

            // when
            cache.populate(source, PresentValue(anime))

            // then
            val result = cache.fetch(source)
            assertThat((result as PresentValue).value).isEqualTo(anime)
        }

        @Test
        fun `don't override an existing entry`() {
            // given
            val source = URI("https://example.org/anime/1535")
            val anime = Anime("Death Note").apply {
                addSources(source)
            }

            val cache = AnimeCache(cacheLoader = listOf(TestCacheLoader))
            cache.populate(source, PresentValue(anime))

            val otherAnime = Anime("Different title").apply {
                addSources(source)
            }

            // when
            cache.populate(source, PresentValue(otherAnime))

            // then
            val result = cache.fetch(source)
            assertThat((result as PresentValue).value).isEqualTo(anime)
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `clear all entries`() {
            // given
            val source1 = URI("https://example.org/anime/1")
            val anime1 = Anime("Entry1").apply {
                addSources(source1)
            }

            val source2 = URI("https://example.org/anime/2")
            val anime2 = Anime("Entry2").apply {
                addSources(source2)
            }

            var loadCacheHasBeenTriggeredFor1 = false
            var loadCacheHasBeenTriggeredFor2 = false

            val testCacheLoader = object: CacheLoader by TestCacheLoader {
                override fun hostname(): Hostname = "example.org"
                override fun loadAnime(uri: URI): Anime {
                    return when(uri) {
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
                populate(source1, PresentValue(anime1))
                populate(source1, PresentValue(anime2))
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