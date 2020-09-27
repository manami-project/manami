package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.commands.TestCommandHistory
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CmdNewListTest {

    @Test
    fun `clears command history, closes file and clears internal state`() {
        // given
        var hasCloseFileBeenCalled = false
        var hasClearStateBeenCalled = false
        var hasClearHistoryBeenCalled = false

        val testState = object: State by TestState {
            override fun closeFile() { hasCloseFileBeenCalled = true }
            override fun clear() { hasClearStateBeenCalled = true }
        }

        val testCommandHistory = object: CommandHistory by TestCommandHistory {
            override fun clear() { hasClearHistoryBeenCalled = true }
        }

        val command = CmdNewList(testState, testCommandHistory)

        // when
        command.execute()

        // then
        assertThat(hasCloseFileBeenCalled).isTrue()
        assertThat(hasClearStateBeenCalled).isTrue()
        assertThat(hasClearHistoryBeenCalled).isTrue()
    }
}