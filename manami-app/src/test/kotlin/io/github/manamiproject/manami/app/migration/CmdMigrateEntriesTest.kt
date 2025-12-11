package io.github.manamiproject.manami.app.migration

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import org.assertj.core.api.Assertions.assertThat
import java.net.URI
import java.nio.file.Paths
import kotlin.test.Test

internal class CmdMigrateEntriesTest {

    @Test
    fun `adds the new AnimeListEntry and removes the old entry afterwards`() {
        // given
        val addedEntries = mutableListOf<AnimeListEntry>()
        val removedEntries = mutableListOf<AnimeListEntry>()

        val testState = object : State by TestState {
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                addedEntries.addAll(anime)
            }
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

        val cmd = CmdMigrateEntries(
            state = testState,
            animeListMappings = mapOf(currentAnimeListEntry to Link("https://anisearch.com/anime/1679")),
            watchListMappings = emptyMap(),
            ignoreListMappings = emptyMap(),
        )

        // when
        cmd.execute()

        // then
        assertThat(addedEntries).containsExactly(currentAnimeListEntry.copy(link = Link("https://anisearch.com/anime/1679")))
        assertThat(removedEntries).containsExactly(currentAnimeListEntry)
    }

    @Test
    fun `adds the new WatchListEntry and removes the old entry afterwards`() {
        // given
        val addedEntries = mutableListOf<WatchListEntry>()
        val removedEntries = mutableListOf<WatchListEntry>()

        val testState = object : State by TestState {
            override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                addedEntries.addAll(anime)
            }

            override fun removeWatchListEntry(entry: WatchListEntry) {
                removedEntries.add(entry)
            }
        }

        val currentWatchListEntry = WatchListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
        )

        val cmd = CmdMigrateEntries(
            state = testState,
            animeListMappings = emptyMap(),
            watchListMappings = mapOf(currentWatchListEntry to Link("https://anisearch.com/anime/1679")),
            ignoreListMappings = emptyMap(),
        )

        // when
        cmd.execute()

        // then
        assertThat(addedEntries).containsExactly(currentWatchListEntry.copy(link = Link("https://anisearch.com/anime/1679")))
        assertThat(removedEntries).containsExactly(currentWatchListEntry)
    }

    @Test
    fun `adds the new IgnoreListEntry and removes the old entry afterwards`() {
        // given
        val addedEntries = mutableListOf<IgnoreListEntry>()
        val removedEntries = mutableListOf<IgnoreListEntry>()

        val testState = object : State by TestState {
            override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                addedEntries.addAll(anime)
            }

            override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
                removedEntries.add(entry)
            }
        }

        val currentIgnoreListEntry = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/57"),
            title = "Beck",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
        )

        val cmd = CmdMigrateEntries(
            state = testState,
            animeListMappings = emptyMap(),
            watchListMappings = emptyMap(),
            ignoreListMappings = mapOf(currentIgnoreListEntry to Link("https://anisearch.com/anime/1679")),
        )

        // when
        cmd.execute()

        // then
        assertThat(addedEntries).containsExactly(currentIgnoreListEntry.copy(link = Link("https://anisearch.com/anime/1679")))
        assertThat(removedEntries).containsExactly(currentIgnoreListEntry)
    }
}