package io.github.manamiproject.manami.app.migration

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI
import java.nio.file.Paths

internal class CmdRemoveUnmappedMigrationEntriesTest {

    @Test
    fun `removes AnimeListEntries`() {
        // given
        val removedEntries = mutableListOf<AnimeListEntry>()

        val testState = object : State by TestState {
            override fun removeAnimeListEntry(entry: AnimeListEntry) {
                removedEntries.add(entry)
            }
        }

        val currentAnimeListEntry = AnimeListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            episodes = 26,
            type = TV,
            location = Paths.get("."),
        )

        val cmd = CmdRemoveUnmappedMigrationEntries(
            state = testState,
            animeListEntriesWithoutMapping = listOf(currentAnimeListEntry),
            watchListEntriesWithoutMapping = emptyList(),
            ignoreListEntriesWithoutMapping = emptyList(),
        )

        // when
        cmd.execute()

        // then
        assertThat(removedEntries).containsExactly(currentAnimeListEntry)
    }

    @Test
    fun `removes WatchListEntries`() {
        // given
        val removedEntries = mutableListOf<WatchListEntry>()

        val testState = object : State by TestState {
            override fun removeWatchListEntry(entry: WatchListEntry) {
                removedEntries.add(entry)
            }
        }

        val currentWatchListEntry = WatchListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
        )

        val cmd = CmdRemoveUnmappedMigrationEntries(
            state = testState,
            animeListEntriesWithoutMapping = emptyList(),
            watchListEntriesWithoutMapping = listOf(currentWatchListEntry),
            ignoreListEntriesWithoutMapping = emptyList(),
        )

        // when
        cmd.execute()

        // then
        assertThat(removedEntries).containsExactly(currentWatchListEntry)
    }

    @Test
    fun `removes IgnoreListEntries`() {
        // given
        val removedEntries = mutableListOf<IgnoreListEntry>()

        val testState = object : State by TestState {
            override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
                removedEntries.add(entry)
            }
        }

        val currentIgnoreListEntry = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
        )

        val cmd = CmdRemoveUnmappedMigrationEntries(
            state = testState,
            animeListEntriesWithoutMapping = emptyList(),
            watchListEntriesWithoutMapping = emptyList(),
            ignoreListEntriesWithoutMapping = listOf(currentIgnoreListEntry),
        )

        // when
        cmd.execute()

        // then
        assertThat(removedEntries).containsExactly(currentIgnoreListEntry)
    }
}