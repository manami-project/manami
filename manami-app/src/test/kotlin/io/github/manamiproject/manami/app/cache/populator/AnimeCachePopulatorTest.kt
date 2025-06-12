package io.github.manamiproject.manami.app.cache.populator

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.cache.TestCacheLoader
import io.github.manamiproject.manami.app.cache.TestConfigRegistry
import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.TestEventBus
import io.github.manamiproject.modb.core.config.ConfigRegistry
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeStatus.FINISHED
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.FALL
import io.github.manamiproject.modb.core.anime.Duration
import io.github.manamiproject.modb.core.anime.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.core.anime.ScoreValue
import io.github.manamiproject.modb.test.MockServerTestCase
import io.github.manamiproject.modb.test.WireMockServerCreator
import io.github.manamiproject.modb.test.loadTestResource
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class AnimeCachePopulatorTest: MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Test
    fun `successfully populate cache`() {
        // given
        val expectedAnime = Anime(
            title = "Death Note",
            type = TV,
            episodes = 37,
            status = FINISHED,
            animeSeason = AnimeSeason(
                season = FALL,
                year = 2006,
            ),
            picture = URI("https://cdn.myanimelist.net/images/anime/1079/138100.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1079/138100t.jpg"),
            duration = Duration(
                value = 23,
                unit = MINUTES,
            ),
            score = ScoreValue(
                arithmeticGeometricMean = 8.631697859409492,
                arithmeticMean = 8.631818181818183,
                median = 8.65,
            ),
            studios = hashSetOf(
                "madhouse",
            ),
            producers = hashSetOf(
                "d.n. dream partners",
                "nippon television network",
                "shueisha",
                "vap",
            ),
            synonyms = hashSetOf(
                "Bilježnica smrti",
                "Caderno da Morte",
                "Carnet de la Mort",
                "Cuốn sổ tử thần",
                "DEATH NOTE",
                "DN",
                "Death Note - A halállista",
                "Death Note - Carnetul morţii",
                "Death Note - Zápisník smrti",
                "Death Note(デスノート)",
                "Mirties Užrašai",
                "Notatnik śmierci",
                "Notes Śmierci",
                "Quaderno della Morte",
                "Sveska Smrti",
                "Ölüm Defteri",
                "Τετράδιο Θανάτου",
                "Бележник на Смъртта",
                "Записник Смерті",
                "Свеска Смрти",
                "Тетрадка на Смъртта",
                "Тетрадь cмерти",
                "Үхлийн Тэмдэглэл",
                "डेथ नोट",
                "สมุดโน้ตกระชากวิญญาณ",
                "ですのーと",
                "デスノート",
                "死亡笔记",
                "死亡筆記本",
                "데스노트",
            ),
            tags = hashSetOf(
                "achronological order",
                "acting",
                "adapted into japanese movie",
                "adapted into jdrama",
                "adapted into other media",
                "adults are useless",
                "alternative present",
                "americas",
                "amnesia",
                "anti-hero",
                "antihero",
                "asexual",
                "asia",
                "assassins",
                "based on a manga",
                "battle of wits",
                "bishounen",
                "canon filler",
                "contemporary fantasy",
                "contractor",
                "cops",
                "crime",
                "crime fiction",
                "criminals",
                "death",
                "detective",
                "detectives",
                "drama",
                "earth",
                "espionage",
                "everybody dies",
                "fantasy",
                "feet",
                "following one`s dream",
                "genius",
                "gods",
                "grail in the garbage",
                "hero of strong character",
                "horror",
                "insane",
                "japan",
                "japanese production",
                "journalism",
                "just as planned",
                "kamis",
                "kuudere",
                "law and order",
                "male protagonist",
                "manga",
                "memory manipulation",
                "mind games",
                "mundane made awesome",
                "murder",
                "mystery",
                "overpowered main characters",
                "philosophy",
                "place",
                "plot continuity",
                "police",
                "police are useless",
                "policeman",
                "predominantly adult cast",
                "present",
                "primarily adult cast",
                "primarily male cast",
                "psychological",
                "psychological drama",
                "psychopaths",
                "real-world location",
                "rivalries",
                "rivalry",
                "romance",
                "school life",
                "secret identity",
                "serial killers",
                "shinigami",
                "shounen",
                "speculative fiction",
                "suicide",
                "supernatural",
                "supernatural drama",
                "supernatural thriller",
                "suspense",
                "tennis",
                "thriller",
                "time",
                "time skip",
                "tragedy",
                "tropes",
                "twisted story",
                "united states",
                "university",
                "unrequited love",
                "unusual weapons -- to be split and deleted",
                "urban",
                "urban fantasy",
                "vigilantes",
                "weekly shounen jump",
                "world domination",
                "yandere",
            ),
        )

        val testCache = DefaultAnimeCache(listOf(TestCacheLoader))

        val receivedEvents = mutableListOf<Event>()
        val testEventBus = object: EventBus by TestEventBus {
            override fun post(event: Event) {
                receivedEvents.add(event)
            }
        }

        val testConfigRegistry = object: ConfigRegistry by TestConfigRegistry {
            override fun boolean(key: String): Boolean = false
        }

        val animeCachePopulator = AnimeCachePopulator(
            uri = URI("http://localhost:$port/anime/1535"),
            eventBus = testEventBus,
            configRegistry = testConfigRegistry,
        )

        serverInstance.stubFor(
                get(urlPathEqualTo("/anime/1535")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(loadTestResource<String>("cache_tests/populator/test-database.json"))
                )
        )

        // when
        runBlocking {
            animeCachePopulator.populate(testCache)
        }

        // then
        val expectedAnidbEntry = expectedAnime.copy(
            sources = hashSetOf(URI("https://anidb.net/anime/4563")),
            relatedAnime = hashSetOf(
                URI("https://anidb.net/anime/8146"),
                URI("https://anidb.net/anime/8147")
            ),
        )
        assertThat((testCache.fetch(URI("https://anidb.net/anime/4563")) as PresentValue).value).isEqualTo(expectedAnidbEntry)

        val expectedAnilistEntry = expectedAnime.copy(
            sources = hashSetOf(URI("https://anilist.co/anime/1535")),
            relatedAnime = hashSetOf(
                URI("https://anilist.co/anime/20931"),
                URI("https://anilist.co/anime/2994"),
            ),
        )
        assertThat((testCache.fetch(URI("https://anilist.co/anime/1535")) as PresentValue).value).isEqualTo(expectedAnilistEntry)

        val expectedAnimePlanetEntry = expectedAnime.copy(
            sources = hashSetOf(URI("https://anime-planet.com/anime/death-note")),
            relatedAnime = hashSetOf(
                URI("https://anime-planet.com/anime/death-note-rewrite-1-visions-of-a-god"),
                URI("https://anime-planet.com/anime/death-note-rewrite-2-ls-successors"),
            ),
        )
        assertThat((testCache.fetch(URI("https://anime-planet.com/anime/death-note")) as PresentValue).value).isEqualTo(expectedAnimePlanetEntry)

        val expectedAnimecountdownEntry = expectedAnime.copy(
            sources = hashSetOf(URI("https://animecountdown.com/40190")),
            relatedAnime = hashSetOf(
                URI("https://animecountdown.com/36687"),
                URI("https://animecountdown.com/40690"),
            ),
        )
        assertThat((testCache.fetch(URI("https://animecountdown.com/40190")) as PresentValue).value).isEqualTo(expectedAnimecountdownEntry)

        val expectedAnisearchEntry = expectedAnime.copy(
            sources = hashSetOf(URI("https://anisearch.com/anime/3633")),
            relatedAnime = hashSetOf(
                URI("https://anisearch.com/anime/4441"),
                URI("https://anisearch.com/anime/5194"),
            ),
        )
        assertThat((testCache.fetch(URI("https://anisearch.com/anime/3633")) as PresentValue).value).isEqualTo(expectedAnisearchEntry)

        val expectedKitsuEntry = expectedAnime.copy(
            sources = hashSetOf(URI("https://kitsu.app/anime/1376")),
            relatedAnime = hashSetOf(URI("https://kitsu.app/anime/2707")),
        )
        assertThat((testCache.fetch(URI("https://kitsu.app/anime/1376")) as PresentValue).value).isEqualTo(expectedKitsuEntry)

        val expectedLivecahartEntry = expectedAnime.copy(
            sources = hashSetOf(URI("https://livechart.me/anime/3437")),
            relatedAnime = hashSetOf(URI("https://livechart.me/anime/3808")),
        )
        assertThat((testCache.fetch(URI("https://livechart.me/anime/3437")) as PresentValue).value).isEqualTo(expectedLivecahartEntry)

        val expectedMalEntry = expectedAnime.copy(
            sources = hashSetOf(URI("https://myanimelist.net/anime/1535")),
            relatedAnime = hashSetOf(URI("https://myanimelist.net/anime/2994")),
        )
        assertThat((testCache.fetch(URI("https://myanimelist.net/anime/1535")) as PresentValue).value).isEqualTo(expectedMalEntry)

        val expectedNotifyEntry = expectedAnime.copy(
            sources = hashSetOf(URI("https://notify.moe/anime/0-A-5Fimg")),
            relatedAnime = hashSetOf(URI("https://notify.moe/anime/DBBU5Kimg")),
        )
        assertThat((testCache.fetch(URI("https://notify.moe/anime/0-A-5Fimg")) as PresentValue).value).isEqualTo(expectedNotifyEntry)

        val expectedSimklEntry = expectedAnime.copy(
            sources = hashSetOf(URI("https://simkl.com/anime/40190")),
            relatedAnime = hashSetOf(
                URI("https://simkl.com/anime/36687"),
                URI("https://simkl.com/anime/40690"),
            ),
        )
        assertThat((testCache.fetch(URI("https://simkl.com/anime/40190")) as PresentValue).value).isEqualTo(expectedSimklEntry)

        assertThat(receivedEvents).hasSize(2)
        assertThat(receivedEvents.first()).isInstanceOf(NumberOfEntriesPerMetaDataProviderEvent::class.java)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["anidb.net"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["anilist.co"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["anime-planet.com"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["animecountdown.com"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["animenewsnetwork.com"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["anisearch.com"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["kitsu.app"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["livechart.me"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["myanimelist.net"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["notify.moe"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["simkl.com"]).isEqualTo(1)
        assertThat(receivedEvents.last()).isInstanceOf(CachePopulatorFinishedEvent::class.java)
    }
}