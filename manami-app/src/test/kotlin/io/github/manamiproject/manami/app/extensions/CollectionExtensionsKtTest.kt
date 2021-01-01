package io.github.manamiproject.manami.app.extensions

import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.Link
import io.github.manamiproject.manami.app.models.WatchListEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI

internal class CollectionExtensionsKtTest {

    @Nested
    inner class CastToSetTests {

        @Test
        fun `successfully cast collection to a set of a specifc type`() {
            // given
            val entry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
            )
            val entry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
            )
            val entry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            val list: List<*> = listOf(
                entry1,
                entry2,
                entry3,
            )

            // when
            val result: Set<WatchListEntry> = list.castToSet()

            // then
            assertThat(result).containsExactly(entry1, entry2, entry3)
        }

        @Test
        fun `throws exception if not all entries are of the same type`() {
            // given
            val entry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
            )
            val entry2 = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
            )
            val entry3 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )

            val list: List<*> = listOf(
                entry1,
                entry2,
                entry3,
            )

            // when
            val result = assertThrows<IllegalArgumentException> {
                list.castToSet<WatchListEntry>()
            }

            // then
            assertThat(result).hasMessage("Not all items are of type [class io.github.manamiproject.manami.app.models.WatchListEntry]")
        }
    }
}