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
        command.execute()

        //then
        assertThat(savedEntries).containsExactly(ignoreListEntry)
    }
}