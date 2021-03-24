package io.github.manamiproject.manami.app.lists.ignorelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class CmdRemoveIgnoreListEntryTest {

    @Test
    fun `remove ignore list entry from watch list in state`() {
        // given
        var receivedEntry: IgnoreListEntry? = null
        val testState = object: State by TestState {
            override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
                receivedEntry = entry
            }
        }

        val expectedEntry = IgnoreListEntry(
            link = Link("https://myanimelist.net/anime/5114"),
            title = "Fullmetal Alchemist: Brotherhood",
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
        )

        val command = CmdRemoveIgnoreListEntry(
            state = testState,
            ignoreListEntry = expectedEntry,
        )

        // when
        command.execute()

        // then
        assertThat(receivedEntry).isEqualTo(expectedEntry)
    }
}