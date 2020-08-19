package io.github.manamiproject.manami.app.cache.populator

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.TestCacheLoader
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
import java.net.URL

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
                        _year = 2006
                ),
                picture = URL("https://cdn.myanimelist.net/images/anime/9/9453.jpg"),
                thumbnail = URL("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
        ).apply {
            addSources(listOf(
                    URL("https://anidb.net/anime/4563"),
                    URL("https://anilist.co/anime/1535"),
                    URL("https://anime-planet.com/anime/death-note"),
                    URL("https://kitsu.io/anime/1376"),
                    URL("https://myanimelist.net/anime/1535"),
                    URL("https://notify.moe/anime/0-A-5Fimg")
            ))
            addSynonyms(listOf(
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
                    "데스노트"
            ))
            addRelations(listOf(
                    URL("https://anidb.net/anime/8146"),
                    URL("https://anidb.net/anime/8147"),
                    URL("https://anilist.co/anime/2994"),
                    URL("https://anime-planet.com/anime/death-note-rewrite-1-visions-of-a-god"),
                    URL("https://anime-planet.com/anime/death-note-rewrite-2-ls-successors"),
                    URL("https://kitsu.io/anime/2707"),
                    URL("https://myanimelist.net/anime/2994"),
                    URL("https://notify.moe/anime/DBBU5Kimg")
            ))
            addTags(listOf(
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
                    "work"
            ))
        }

        val testCache = AnimeCache(listOf(TestCacheLoader))
        val animeCachePopulator = AnimeCachePopulator(
                url = URL("http://localhost:$port/anime/1535")
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
        assertThat(testCache.fetch(URL("https://anidb.net/anime/4563"))).isEqualTo(expectedAnime)
        assertThat(testCache.fetch(URL("https://anilist.co/anime/1535"))).isEqualTo(expectedAnime)
        assertThat(testCache.fetch(URL("https://anime-planet.com/anime/death-note"))).isEqualTo(expectedAnime)
        assertThat(testCache.fetch(URL("https://kitsu.io/anime/1376"))).isEqualTo(expectedAnime)
        assertThat(testCache.fetch(URL("https://myanimelist.net/anime/1535"))).isEqualTo(expectedAnime)
        assertThat(testCache.fetch(URL("https://notify.moe/anime/0-A-5Fimg"))).isEqualTo(expectedAnime)
    }
}