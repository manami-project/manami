package io.github.manamiproject.manami.app.cache.populator

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.cache.TestCacheLoader
import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.TestEventBus
import io.github.manamiproject.modb.core.collections.SortedList
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.FALL
import io.github.manamiproject.modb.test.MockServerTestCase
import io.github.manamiproject.modb.test.WireMockServerCreator
import io.github.manamiproject.modb.test.loadTestResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class AnimeCachePopulatorTest: MockServerTestCase<WireMockServer> by WireMockServerCreator() {

    @Test
    fun `successfully populate cache`() {
        // given
        val expectedAnime = Anime(
            _title = "Death Note",
            type = TV,
            episodes = 37,
            status = FINISHED,
            animeSeason = AnimeSeason(
                season = FALL,
                year = 2006,
            ),
            picture = URI("https://cdn.myanimelist.net/images/anime/9/9453.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg"),
        ).apply {
            addSynonyms(
                "DEATH NOTE",
                "DN",
                "Death Note - A halállista",
                "Death Note - Carnetul morţii",
                "Death Note - Zápisník smrti",
                "Notatnik śmierci",
                "Τετράδιο Θανάτου",
                "Бележник на Смъртта",
                "Тетрадь cмерти",
                "Үхлийн Тэмдэглэл",
                "دفترچه یادداشت مرگ",
                "كـتـاب الـموت",
                "डेथ नोट",
                "ですのーと",
                "デスノート",
                "死亡笔记",
                "데스노트",
            )
            addTags(
                "alternative present",
                "amnesia",
                "anti-hero",
                "asia",
                "based on a manga",
                "contemporary fantasy",
                "cops",
                "crime",
                "criminals",
                "demons",
                "detective",
                "detectives",
                "drama",
                "earth",
                "espionage",
                "gods",
                "japan",
                "male protagonist",
                "manga",
                "mind games",
                "mystery",
                "overpowered main characters",
                "philosophy",
                "plot continuity",
                "police",
                "present",
                "primarily adult cast",
                "primarily male cast",
                "psychological",
                "psychopaths",
                "revenge",
                "rivalries",
                "secret identity",
                "serial killers",
                "shinigami",
                "shounen",
                "supernatural",
                "thriller",
                "time skip",
                "tragedy",
                "urban",
                "urban fantasy",
                "vigilantes",
                "work",
            )
        }

        val testCache = DefaultAnimeCache(listOf(TestCacheLoader))

        val receivedEvents = mutableListOf<Event>()
        val testEventBus = object: EventBus by TestEventBus {
            override fun post(event: Event) {
                receivedEvents.add(event)
            }
        }

        val animeCachePopulator = AnimeCachePopulator(
                uri = URI("http://localhost:$port/anime/1535"),
                eventBus = testEventBus,
        )

        serverInstance.stubFor(
                get(urlPathEqualTo("/anime/1535")).willReturn(
                    aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(loadTestResource("cache_tests/populator/test-database.json"))
                )
        )

        // when
        animeCachePopulator.populate(testCache)

        // then
        val expectedAnidbEntry = expectedAnime.copy(
            sources = SortedList(URI("https://anidb.net/anime/4563")),
            relatedAnime = SortedList(
                URI("https://anidb.net/anime/8146"),
                URI("https://anidb.net/anime/8147")
            ),
        )
        assertThat((testCache.fetch(URI("https://anidb.net/anime/4563")) as PresentValue).value).isEqualTo(expectedAnidbEntry)

        val expectedAnilistEntry = expectedAnime.copy(
            sources = SortedList(URI("https://anilist.co/anime/1535")),
            relatedAnime = SortedList(URI("https://anilist.co/anime/2994")),
        )
        assertThat((testCache.fetch(URI("https://anilist.co/anime/1535")) as PresentValue).value).isEqualTo(expectedAnilistEntry)

        val expectedAnimePlanetEntry = expectedAnime.copy(
            sources = SortedList(URI("https://anime-planet.com/anime/death-note")),
            relatedAnime = SortedList(
                URI("https://anime-planet.com/anime/death-note-rewrite-1-visions-of-a-god"),
                URI("https://anime-planet.com/anime/death-note-rewrite-2-ls-successors"),
            ),
        )
        assertThat((testCache.fetch(URI("https://anime-planet.com/anime/death-note")) as PresentValue).value).isEqualTo(expectedAnimePlanetEntry)

        val expectedKitsuEntry = expectedAnime.copy(
            sources = SortedList(URI("https://kitsu.io/anime/1376")),
            relatedAnime = SortedList(URI("https://kitsu.io/anime/2707")),
        )
        assertThat((testCache.fetch(URI("https://kitsu.io/anime/1376")) as PresentValue).value).isEqualTo(expectedKitsuEntry)

        val expectedMalEntry = expectedAnime.copy(
            sources = SortedList(URI("https://myanimelist.net/anime/1535")),
            relatedAnime = SortedList(URI("https://myanimelist.net/anime/2994")),
        )
        assertThat((testCache.fetch(URI("https://myanimelist.net/anime/1535")) as PresentValue).value).isEqualTo(expectedMalEntry)

        val expectedNotifyEntry = expectedAnime.copy(
            sources = SortedList(URI("https://notify.moe/anime/0-A-5Fimg")),
            relatedAnime = SortedList(URI("https://notify.moe/anime/DBBU5Kimg")),
        )
        assertThat((testCache.fetch(URI("https://notify.moe/anime/0-A-5Fimg")) as PresentValue).value).isEqualTo(expectedNotifyEntry)

        assertThat(receivedEvents).hasSize(2)
        assertThat(receivedEvents.first()).isInstanceOf(NumberOfEntriesPerMetaDataProviderEvent::class.java)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["anidb.net"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["anilist.co"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["anime-planet.com"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["kitsu.io"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["myanimelist.net"]).isEqualTo(1)
        assertThat((receivedEvents.first() as NumberOfEntriesPerMetaDataProviderEvent).entries["notify.moe"]).isEqualTo(1)
        assertThat(receivedEvents.last()).isInstanceOf(CachePopulatorFinishedEvent::class.java)
    }
}