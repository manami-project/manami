package io.github.manamiproject.manami.app.inconsistencies.lists.metadata

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class CmdFixMetaDataTest {

    @Test
    fun `remove current WatchListEntry and replace it with the new instances`() {
        // given
        val currentWatchListEntry1 = WatchListEntry(
            link = Link("https://myanimelist.net/anime/40059"),
            title = "Golden Kamuy 3rd Season",
            thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
        )
        val newWatchListEntry1 = WatchListEntry(
            link = Link("https://myanimelist.net/anime/40059"),
            title = "Golden Kamuy 3rd Season",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
        )

        val currentWatchListEntry2 = WatchListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Hagane no Renkinjutsushi: Fullmetal Alchemist",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )
        val newWatchListEntry2 = WatchListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Fullmetal Alchemist: Brotherhood",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )

        val removedEntries = mutableListOf<WatchListEntry>()
        val addedEntries = mutableListOf<WatchListEntry>()
        val testState = object: State by TestState {
            override fun watchList(): Set<WatchListEntry> = setOf(currentWatchListEntry1, currentWatchListEntry2)
            override fun removeWatchListEntry(entry: WatchListEntry) {
                removedEntries.add(entry)
            }
            override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                addedEntries.addAll(anime)
            }
            override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
        }

        val cmdFixMetaData = CmdFixMetaData(
            state = testState,
            diffWatchList = listOf(
                MetaDataDiff(currentEntry = currentWatchListEntry1, newEntry = newWatchListEntry1),
                MetaDataDiff(currentEntry = currentWatchListEntry2, newEntry = newWatchListEntry2),
            )
        )

        // when
        val result = cmdFixMetaData.execute()

        // then
        assertThat(result).isTrue()
        assertThat(removedEntries).containsExactly(currentWatchListEntry1, currentWatchListEntry2)
        assertThat(addedEntries).containsExactly(newWatchListEntry1, newWatchListEntry2)
    }

    @Test
    fun `remove current IgnoreListEntry and replace it with the new instances`() {
        // given
        val currentIgnoreListEntry1 = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/40059"),
            title = "Golden Kamuy 3rd Season",
            thumbnail = URI("https://cdn.myanimelist.net/images/qm_50.gif"),
        )
        val newIgnoreListEntry1 = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/40059"),
            title = "Golden Kamuy 3rd Season",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
        )

        val currentIgnoreListEntry2 = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Hagane no Renkinjutsushi: Fullmetal Alchemist",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )
        val newIgnoreListEntry2 = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Fullmetal Alchemist: Brotherhood",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )

        val removedEntries = mutableListOf<IgnoreListEntry>()
        val addedEntries = mutableListOf<IgnoreListEntry>()
        val testState = object: State by TestState {
            override fun ignoreList(): Set<IgnoreListEntry> = setOf(currentIgnoreListEntry1, currentIgnoreListEntry2)
            override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
                removedEntries.add(entry)
            }
            override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                addedEntries.addAll(anime)
            }
            override fun watchList(): Set<WatchListEntry> = emptySet()
        }

        val cmdFixMetaData = CmdFixMetaData(
            state = testState,
            diffIgnoreList = listOf(
                MetaDataDiff(currentEntry = currentIgnoreListEntry1, newEntry = newIgnoreListEntry1),
                MetaDataDiff(currentEntry = currentIgnoreListEntry2, newEntry = newIgnoreListEntry2),
            )
        )

        // when
        val result = cmdFixMetaData.execute()

        // then
        assertThat(result).isTrue()
        assertThat(removedEntries).containsExactly(currentIgnoreListEntry1, currentIgnoreListEntry2)
        assertThat(addedEntries).containsExactly(newIgnoreListEntry1, newIgnoreListEntry2)
    }
}