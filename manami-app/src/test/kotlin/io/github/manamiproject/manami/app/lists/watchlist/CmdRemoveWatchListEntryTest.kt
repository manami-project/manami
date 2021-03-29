package io.github.manamiproject.manami.app.lists.watchlist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class CmdRemoveWatchListEntryTest {

    @Test
    fun `remove watch list entry from watch list in state`() {
        // given
        val expectedEntry = WatchListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Fullmetal Alchemist: Brotherhood",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )

        var receivedEntry: WatchListEntry? = null
        val testState = object: State by TestState {
            override fun watchList(): Set<WatchListEntry> = setOf(expectedEntry)
            override fun removeWatchListEntry(entry: WatchListEntry) {
                receivedEntry = entry
            }
        }

        val command = CmdRemoveWatchListEntry(
            state = testState,
            watchListEntry = expectedEntry,
        )

        // when
        val result = command.execute()

        // then
        assertThat(result).isTrue()
        assertThat(receivedEntry).isEqualTo(expectedEntry)
    }

    @Test
    fun `don't do anything if the list does not contain the entry`() {
        // given
        var receivedEntry: WatchListEntry? = null
        val testState = object: State by TestState {
            override fun watchList(): Set<WatchListEntry> = emptySet()
            override fun removeWatchListEntry(entry: WatchListEntry) {
                receivedEntry = entry
            }
        }

        val expectedEntry = WatchListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Fullmetal Alchemist: Brotherhood",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )

        val command = CmdRemoveWatchListEntry(
            state = testState,
            watchListEntry = expectedEntry,
        )

        // when
        val result = command.execute()

        // then
        assertThat(result).isFalse()
        assertThat(receivedEntry).isNull()
    }
}