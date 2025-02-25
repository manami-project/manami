package io.github.manamiproject.manami.app.lists.watchlist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeSeason
import io.github.manamiproject.modb.core.anime.AnimeSeason.Season.*
import io.github.manamiproject.modb.core.anime.AnimeStatus
import io.github.manamiproject.modb.core.anime.AnimeStatus.*
import io.github.manamiproject.modb.core.anime.AnimeType
import io.github.manamiproject.modb.core.anime.AnimeType.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class WatchListEntryTest {

    @Test
    fun `correctly convert Anime to WatchListEntry`() {
        // given
        val anime = Anime(
            title = "Death Note",
            type = TV,
            episodes = 37,
            status = FINISHED,
            animeSeason = AnimeSeason(
                season = FALL,
                year = 2006,
            ),
            picture = URI("https://cdn.myanimelist.net/images/anime/9/9453.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg"),
            sources = hashSetOf(
                URI("https://anidb.net/anime/4563"),
                URI("https://anilist.co/anime/1535"),
                URI("https://anime-planet.com/anime/death-note"),
                URI("https://kitsu.app/anime/1376"),
                URI("https://myanimelist.net/anime/1535"),
                URI("https://notify.moe/anime/0-A-5Fimg"),
            ),
            synonyms = hashSetOf(
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
            ),
            tags = hashSetOf(
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
            ),
        )

        // when
        val result = WatchListEntry(anime)

        // then
        assertThat(result.title).isEqualTo(anime.title)
        assertThat(result.link.uri).isEqualTo(anime.sources.first())
        assertThat(result.thumbnail).isEqualTo(anime.thumbnail)
    }

    @Test
    fun `correctly convert AnimeEntry to WatchListEntry`() {
        // given
        val ignoreListEntry = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/28981"),
            title = "Ame-iro Cocoa",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1957/111714t.jpg"),
        )

        // when
        val result = WatchListEntry(ignoreListEntry)

        // then
        assertThat(result.title).isEqualTo(ignoreListEntry.title)
        assertThat(result.link).isEqualTo(ignoreListEntry.link)
        assertThat(result.thumbnail).isEqualTo(ignoreListEntry.thumbnail)
    }
}