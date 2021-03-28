package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.models.Anime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class CmdAddAnimeListEntryTest {

    @Test
    fun `add the anime list entry to the state`() {
        // given
        val savedEntries = mutableListOf<AnimeListEntry>()
        val testState = object: State by TestState {
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                savedEntries.addAll(anime)
            }
        }

        val animeListEntry = AnimeListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            episodes = 26,
            type = Anime.Type.TV,
            location = URI("some/relative/path/beck"),
        )

        val command = CmdAddAnimeListEntry(
            state = testState,
            animeListEntry = animeListEntry,
        )

        // when
        command.execute()

        //then
        assertThat(savedEntries).containsExactly(animeListEntry)
    }
}