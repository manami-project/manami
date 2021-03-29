package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class CmdAddAnimeListEntryTest {

    @Test
    fun `add the anime list entry to the state`() {
        // given
        val savedEntries = mutableListOf<AnimeListEntry>()
        val testState = object: State by TestState {
            override fun animeList(): List<AnimeListEntry> = emptyList()
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                savedEntries.addAll(anime)
            }
        }

        val animeListEntry = AnimeListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            episodes = 26,
            type = TV,
            location = URI("some/relative/path/beck"),
        )

        val command = CmdAddAnimeListEntry(
            state = testState,
            animeListEntry = animeListEntry,
        )

        // when
        val result = command.execute()

        //then
        assertThat(result).isTrue()
        assertThat(savedEntries).containsExactly(animeListEntry)
    }

    @Test
    fun `don't add entry if it already exists in list`() {
        // given
        val animeListEntry = AnimeListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            episodes = 26,
            type = TV,
            location = URI("some/relative/path/beck"),
        )

        val savedEntries = mutableListOf<AnimeListEntry>()
        val testState = object: State by TestState {
            override fun animeList(): List<AnimeListEntry> = listOf(animeListEntry)
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                savedEntries.addAll(anime)
            }
        }

        val command = CmdAddAnimeListEntry(
            state = testState,
            animeListEntry = animeListEntry,
        )

        // when
        val result = command.execute()

        //then
        assertThat(result).isFalse()
        assertThat(savedEntries).isEmpty()
    }
}