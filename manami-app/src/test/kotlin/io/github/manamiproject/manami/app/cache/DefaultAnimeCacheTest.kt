package io.github.manamiproject.manami.app.cache

import io.github.manamiproject.manami.app.cache.loader.CacheLoader
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeStatus.UPCOMING
import io.github.manamiproject.modb.core.anime.AnimeType.*
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import java.net.URI

internal class DefaultAnimeCacheTest {

    @Nested
    inner class FetchTests {

        @Test
        fun `externally load entry using a fitting cache loader if the key doesn't exist in the cache`() {
            runBlocking {
                // given
                var loadExternallyHasBeenTriggered = false

                val testCacheLoader = object: CacheLoader by TestCacheLoader {
                    override fun hostname(): Hostname = "example.org"

                    override suspend fun loadAnime(uri: URI): Anime {
                        loadExternallyHasBeenTriggered = true
                        return Anime("Death Note")
                    }
                }
                val cache = DefaultAnimeCache(cacheLoader = listOf(testCacheLoader))

                // when
                cache.fetch(URI("https://example.org/anime/1535"))

                // then
                assertThat(loadExternallyHasBeenTriggered).isTrue()
            }
        }

        @Test
        fun `trigger cache hit if the cache has been populated beforehand`() {
            runBlocking {
                // given
                var timesLoadExternallyHasBeenTriggered = 0
                val source = URI("https://example.org/anime/1535")
                val anime = Anime(
                    title = "Death Note",
                    sources = hashSetOf(source)
                )

                val testCacheLoader = object: CacheLoader by TestCacheLoader {
                    override fun hostname(): Hostname = "example.org"

                    override suspend fun loadAnime(uri: URI): Anime {
                        timesLoadExternallyHasBeenTriggered++
                        return anime
                    }
                }
                val cache = DefaultAnimeCache(cacheLoader = listOf(testCacheLoader))
                cache.fetch(source)

                // when
                val result = cache.fetch(URI("https://example.org/anime/1535"))

                // then
                assertThat(timesLoadExternallyHasBeenTriggered).isOne()
                assertThat(result).isInstanceOf(PresentValue::class.java)
                assertThat((result as PresentValue).value).isEqualTo(anime)
            }
        }

        @Test
        fun `a key can also return null`() {
            runBlocking {
                // given
                val source = URI("https://example.org/anime/1535")

                val cache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader))
                cache.populate(source, DeadEntry())

                // when
                val result = cache.fetch(URI("https://example.org/anime/1535"))

                // then
                assertThat(result).isInstanceOf(DeadEntry::class.java)
            }
        }

        @Test
        fun `return null if the key doesn't exist in the cache and there is no matching cache loader to populate the cache`() {
            runBlocking {
                // given
                val cache = DefaultAnimeCache(cacheLoader = emptyList())

                // when
                val result = cache.fetch(URI("https://example.org/anime/1535"))

                // then
                assertThat(result).isInstanceOf(DeadEntry::class.java)
            }
        }
    }

    @Nested
    inner class PopulateTests {

        @Test
        fun `populate cache`() {
            runBlocking {
                // given
                val source = URI("https://example.org/anime/1535")
                val anime = Anime(
                    title = "Death Note",
                    sources = hashSetOf(source)
                )

                val cache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader))

                // when
                cache.populate(source, PresentValue(anime))

                // then
                val result = cache.fetch(source)
                assertThat((result as PresentValue).value).isEqualTo(anime)
            }
        }

        @Test
        fun `don't override an existing entry`() {
            runBlocking {
                // given
                val source = URI("https://example.org/anime/1535")
                val anime = Anime(
                    title = "Death Note",
                    sources = hashSetOf(source)
                )

                val cache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader))
                cache.populate(source, PresentValue(anime))

                val otherAnime = Anime(
                    title = "Different title",
                    sources = hashSetOf(source)
                )

                // when
                cache.populate(source, PresentValue(otherAnime))

                // then
                val result = cache.fetch(source)
                assertThat((result as PresentValue).value).isEqualTo(anime)
            }
        }
    }

    @Nested
    inner class ClearTests {

        @Test
        fun `clear all entries`() {
            runBlocking {
                // given
                val source1 = URI("https://example.org/anime/1")
                val anime1 = Anime(
                    title = "Entry1",
                    sources = hashSetOf(
                        source1,
                    )
                )

                val source2 = URI("https://example.org/anime/2")
                val anime2 = Anime(
                    title = "Entry2",
                    sources = hashSetOf(
                        source2,
                    )
                )

                var loadCacheHasBeenTriggeredFor1 = false
                var loadCacheHasBeenTriggeredFor2 = false

                val testCacheLoader = object: CacheLoader by TestCacheLoader {
                    override fun hostname(): Hostname = "example.org"
                    override suspend fun loadAnime(uri: URI): Anime {
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
                val cache = DefaultAnimeCache(cacheLoader = listOf(testCacheLoader)).apply {
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

    @Nested
    inner class AvailableMetaDataProviderTests {

        @Test
        fun `return the values from the AnimeCache`() {
            // given
            val entry = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.app/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                    URI("https://notify.moe/anime/_RdVrLpGR"),
                ),
                title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                type = OVA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val defaultAnimeCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                entry.sources.forEach {
                    populate(it, PresentValue(entry))
                }
            }

            // when
            val result = defaultAnimeCache.availableMetaDataProvider

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "anidb.net",
                "anilist.co",
                "anime-planet.com",
                "kitsu.app",
                "myanimelist.net",
                "notify.moe",
            )
        }
    }

    @Nested
    inner class AvailableTagsTests {

        @Test
        fun `return the values from the AnimeCache`() {
            // given
            val entry = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.app/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                    URI("https://notify.moe/anime/_RdVrLpGR"),
                ),
                title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                type = OVA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
                tags = hashSetOf(
                    "based on a manga",
                    "comedy",
                    "ensemble cast",
                    "food",
                    "high school",
                    "psychological",
                    "romance",
                    "romantic comedy",
                    "school",
                    "school clubs",
                    "school life",
                    "seinen",
                    "slice of life",
                    "student government",
                    "tsundere",
                )
            )

            val defaultAnimeCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                entry.sources.forEach {
                    populate(it, PresentValue(entry))
                }
            }

            // when
            val result = defaultAnimeCache.availableTags

            // then
            assertThat(result).containsExactlyInAnyOrder(
                "based on a manga",
                "comedy",
                "ensemble cast",
                "food",
                "high school",
                "psychological",
                "romance",
                "romantic comedy",
                "school",
                "school clubs",
                "school life",
                "seinen",
                "slice of life",
                "student government",
                "tsundere",
            )
        }
    }

    @Nested
    inner class AllEntriesTests {

        @Test
        fun `return all entries of a specific provider`() {
            // given
            val entry1 = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15738"),
                    URI("https://anilist.co/anime/124194"),
                    URI("https://anime-planet.com/anime/fruits-basket-the-final"),
                    URI("https://kitsu.app/anime/43578"),
                    URI("https://myanimelist.net/anime/42938"),
                    URI("https://notify.moe/anime/YiySZ9OMg"),
                ),
                title = "Fruits Basket: The Final",
                type = TV,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val entry2 = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.app/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                    URI("https://notify.moe/anime/_RdVrLpGR"),
                ),
                title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                type = OVA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = SPRING,
                    year = 2021,
                ),
            )

            val entry3 = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15070"),
                    URI("https://anime-planet.com/anime/the-rising-of-the-shield-hero-2nd-season"),
                    URI("https://myanimelist.net/anime/40356"),
                    URI("https://notify.moe/anime/rBaaLj2Wg"),
                ),
                title = "Tate no Yuusha no Nariagari Season 2",
                type = TV,
                episodes = 0,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = FALL,
                    year = 2021,
                ),
            )

            val entry4 = Anime(
                sources = hashSetOf(
                    URI("https://myanimelist.net/anime/46587"),
                ),
                title = "Tenchi Souzou Design-bu Special",
                type = ONA,
                episodes = 1,
                status = UPCOMING,
                animeSeason = AnimeSeason(
                    season = UNDEFINED,
                    year = 2021
                ),
            )

            val entry5 = Anime(
                sources = hashSetOf(
                    URI("https://kitsu.app/anime/40614"),
                    URI("https://myanimelist.net/anime/34705"),
                    URI("https://notify.moe/anime/3I2v2FmiR"),
                ),
                title = "Tejina Shi",
                type = MOVIE,
                episodes = 1,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = UNDEFINED,
                    year = 0
                ),
            )

            val defaultAnimeCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                entry1.sources.forEach {
                    populate(it, PresentValue(entry1))
                }
                entry2.sources.forEach {
                    populate(it, PresentValue(entry2))
                }
                entry3.sources.forEach {
                    populate(it, PresentValue(entry3))
                }
                entry4.sources.forEach {
                    populate(it, PresentValue(entry4))
                }
                entry5.sources.forEach {
                    populate(it, PresentValue(entry5))
                }
            }

            // when
            val result = defaultAnimeCache.allEntries("anime-planet.com").toList()

            // then
            assertThat(result).hasSize(3)
            assertThat(result.flatMap { it.sources }.map { it.host }.distinct()).containsExactly("anime-planet.com")
            assertThat(result.map { it.title }).containsExactlyInAnyOrder(
                "Fruits Basket: The Final",
                "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
                "Tate no Yuusha no Nariagari Season 2",
            )
        }

        @Test
        fun `create individual entries for duplicates`() {
            // given
            val entry = Anime(
                sources = hashSetOf(
                    URI("https://myanimelist.net/anime/48670"),
                    URI("https://myanimelist.net/anime/48671"),
                    URI("https://myanimelist.net/anime/48672"),
                ),
                title = "Tsugumomo Mini Anime",
                type = ONA,
                episodes = 59,
                status = FINISHED,
                animeSeason = AnimeSeason(
                    season = UNDEFINED,
                    year = 2019,
                ),
                picture = URI("https://cdn.myanimelist.net/images/anime/1469/114202.jpg"),
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1469/114202t.jpg"),
                synonyms = hashSetOf(
                    "Tsugu Tsugumomo Mini Anime",
                    "継つぐももミニアニメ"
                ),
                relatedAnime = hashSetOf(
                    URI("https://myanimelist.net/anime/34019"),
                    URI("https://myanimelist.net/anime/39469"),
                ),
                tags = hashSetOf("comedy"),
            )

            val defaultAnimeCache = DefaultAnimeCache(cacheLoader = listOf(TestCacheLoader)).apply {
                entry.sources.forEach {
                    populate(it, PresentValue(entry))
                }
            }

            // when
            val result = defaultAnimeCache.allEntries("myanimelist.net").toList()

            // then
            assertThat(result).hasSize(3)
            assertThat(result.map { it.sources.first() }).containsExactlyInAnyOrder(
                URI("https://myanimelist.net/anime/48670"),
                URI("https://myanimelist.net/anime/48671"),
                URI("https://myanimelist.net/anime/48672"),
            )
        }
    }

    @Nested
    inner class MapToMetaDataProviderTests {

        @Test
        fun `returns empty set if there is no mapping`() {
            // given
            val defaultAnimeCache = DefaultAnimeCache(
                cacheLoader = emptyList(),
            )

            // when
            val result = defaultAnimeCache.mapToMetaDataProvider(MyanimelistConfig.buildAnimeLink("1535"), "kitsu.app")

            // then
            assertThat(result).isEmpty()
        }

        @Test
        fun `successfully returns an entry`() {
            // given
            val testAnime = Anime(
                sources = hashSetOf(
                    URI("https://anidb.net/anime/15807"),
                    URI("https://anilist.co/anime/125368"),
                    URI("https://anime-planet.com/anime/kaguya-sama-love-is-war-ova"),
                    URI("https://kitsu.app/anime/43731"),
                    URI("https://myanimelist.net/anime/43609"),
                    URI("https://notify.moe/anime/_RdVrLpGR"),
                ),
                title = "Kaguya-sama wa Kokurasetai: Tensai-tachi no Renai Zunousen OVA",
            )

            val defaultAnimeCache = DefaultAnimeCache(
                cacheLoader = emptyList(),
            )

            testAnime.sources.forEach {
                defaultAnimeCache.populate(it, PresentValue(testAnime))
            }

            // when
            val result = defaultAnimeCache.mapToMetaDataProvider(MyanimelistConfig.buildAnimeLink("43609"), "kitsu.app")

            // then
            assertThat(result).containsExactly(URI("https://kitsu.app/anime/43731"))
        }

        @Test
        fun `successfully returns an entry having multiple mapping entries`() {
            // given
            val testAnime = Anime(
                sources = hashSetOf(
                    URI("https://kitsu.app/anime/45724"),
                    URI("https://myanimelist.net/anime/50731"),
                    URI("https://myanimelist.net/anime/50814"),
                ),
                title = "Kimi Shika Inai",
            )

            val defaultAnimeCache = DefaultAnimeCache(
                cacheLoader = emptyList(),
            )

            testAnime.sources.forEach {
                defaultAnimeCache.populate(it, PresentValue(testAnime))
            }

            // when
            val result = defaultAnimeCache.mapToMetaDataProvider(KitsuConfig.buildAnimeLink("45724"), "myanimelist.net")

            // then
            assertThat(result).containsExactlyInAnyOrder(
                URI("https://myanimelist.net/anime/50731"),
                URI("https://myanimelist.net/anime/50814"),
            )
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultAnimeCache.instance

            // when
            val result = DefaultAnimeCache.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultAnimeCache::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}