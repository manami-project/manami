package io.github.manamiproject.manami.app.lists.watchlist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class CmdAddWatchListEntryTest {

    @Test
    fun `add the watch list entry to the state`() {
        // given
        val savedEntries = mutableListOf<WatchListEntry>()
        val testState = object: State by TestState {
            override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                savedEntries.addAll(anime)
            }
        }

        val watchListEntry = WatchListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Fullmetal Alchemist: Brotherhood",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )

        val command = CmdAddWatchListEntry(
            state = testState,
            watchListEntry = watchListEntry,
        )

        // when
        command.execute()

        //then
        assertThat(savedEntries).containsExactly(watchListEntry)
    }
}