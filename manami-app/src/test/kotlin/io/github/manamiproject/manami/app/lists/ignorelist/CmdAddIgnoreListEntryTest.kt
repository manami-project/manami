package io.github.manamiproject.manami.app.lists.ignorelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class CmdAddIgnoreListEntryTest {

    @Test
    fun `add the ignore list entry to the state`() {
        // given
        val savedEntries = mutableListOf<IgnoreListEntry>()
        val testState = object: State by TestState {
            override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                savedEntries.addAll(anime)
            }
        }

        val ignoreListEntry = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Fullmetal Alchemist: Brotherhood",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )

        val command = CmdAddIgnoreListEntry(
            state = testState,
            ignoreListEntry = ignoreListEntry,
        )

        // when
        val result = command.execute()

        //then
        assertThat(result).isTrue()
        assertThat(savedEntries).containsExactly(ignoreListEntry)
    }

    @Test
    fun `don't add entry if it already exists in list`() {
        // given
        val ignoreListEntry = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Fullmetal Alchemist: Brotherhood",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )

        val savedEntries = mutableListOf<IgnoreListEntry>()
        val testState = object: State by TestState {
            override fun ignoreList(): Set<IgnoreListEntry> = setOf(ignoreListEntry)
            override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                savedEntries.addAll(anime)
            }
        }

        val command = CmdAddIgnoreListEntry(
            state = testState,
            ignoreListEntry = ignoreListEntry,
        )

        // when
        val result = command.execute()

        //then
        assertThat(result).isFalse()
        assertThat(savedEntries).isEmpty()
    }
}