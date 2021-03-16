package io.github.manamiproject.manami.app.lists

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class LinkEntryKtTest {

    @Test
    fun `toString of NoLink returns an empty string`() {
        // when
        val result = NoLink.toString()

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `toString of LinkEntry returns the URL as string`() {
        // given
        val link = Link("https://myanimelist.net/anime/1535")

        // when
        val result = link.toString()

        // then
        assertThat(result).isEqualTo("https://myanimelist.net/anime/1535")
    }
}