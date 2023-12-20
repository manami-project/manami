package io.github.manamiproject.manami.app.inconsistencies.lists.deadentries

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class CmdFixDeadEntriesTest {

    @Test
    fun `remove current WatchListEntry`() {
        // given
        val currentWatchListEntry1 = WatchListEntry(
            link = Link("https://myanimelist.net/anime/40059"),
            title = "Golden Kamuy 3rd Season",
            thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
        )
        val currentWatchListEntry2 = WatchListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Hagane no Renkinjutsushi: Fullmetal Alchemist",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )

        val removedEntries = mutableListOf<WatchListEntry>()
        val testState = object: State by TestState {
            override fun watchList(): Set<WatchListEntry> = setOf(currentWatchListEntry1, currentWatchListEntry2)
            override fun removeWatchListEntry(entry: WatchListEntry) {
                removedEntries.add(entry)
            }
            override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
        }

        val cmdFixDeadEntries = CmdFixDeadEntries(
            state = testState,
            removeWatchList = listOf(
                currentWatchListEntry1,
                currentWatchListEntry2,
            )
        )

        // when
        val result = cmdFixDeadEntries.execute()

        // then
        assertThat(result).isTrue()
        assertThat(removedEntries).containsExactly(currentWatchListEntry1, currentWatchListEntry2)
    }

    @Test
    fun `remove current IgnoreListEntry`() {
        // given
        val currentIgnoreListEntry1 = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/40059"),
            title = "Golden Kamuy 3rd Season",
            thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
        )
        val currentIgnoreListEntry2 = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Hagane no Renkinjutsushi: Fullmetal Alchemist",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )

        val removedEntries = mutableListOf<IgnoreListEntry>()
        val testState = object: State by TestState {
            override fun ignoreList(): Set<IgnoreListEntry> = setOf(currentIgnoreListEntry1, currentIgnoreListEntry2)
            override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
                removedEntries.add(entry)
            }
            override fun watchList(): Set<WatchListEntry> = emptySet()
        }

        val cmdFixDeadEntries = CmdFixDeadEntries(
            state = testState,
            removeIgnoreList = listOf(
                currentIgnoreListEntry1,
                currentIgnoreListEntry2,
            )
        )

        // when
        val result = cmdFixDeadEntries.execute()

        // then
        assertThat(result).isTrue()
        assertThat(removedEntries).containsExactly(currentIgnoreListEntry1, currentIgnoreListEntry2)
    }
}