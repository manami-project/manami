package io.github.manamiproject.manami.app.lists.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.models.Anime
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class CmdRemoveAnimeListEntryTest {

    @Test
    fun `remove anime list entry from anime list in state`() {
        // given
        var receivedEntry: AnimeListEntry? = null
        val testState = object: State by TestState {
            override fun removeAnimeListEntry(entry: AnimeListEntry) {
                receivedEntry = entry
            }
        }

        val expectedEntry = AnimeListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            episodes = 26,
            type = Anime.Type.TV,
            location = URI("some/relative/path/beck"),
        )

        val command = CmdRemoveAnimeListEntry(
            state = testState,
            animeListEntry = expectedEntry,
        )

        // when
        command.execute()

        // then
        assertThat(receivedEntry).isEqualTo(expectedEntry)
    }
}