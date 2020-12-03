package io.github.manamiproject.manami.app.import

import io.github.manamiproject.manami.app.import.parser.ParsedFile
import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.Link
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.modb.core.models.Anime
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.URL

internal class CmdAddEntriesFromParsedFileTest {

    @AfterEach
    fun afterEach() {
        InternalState.clear()
    }

    @Test
    fun `correctly sets the state`() {
        // given
        val animeListEntry1 = AnimeListEntry(
                title = "H2O: Footprints in the Sand",
                episodes = 4,
                type = Anime.Type.Special,
                location = "some/relative/path/h2o_-_footprints_in_the_sand_special",
        )
        val animeListEntry2 = AnimeListEntry(
                link = Link(URI("https://myanimelist.net/anime/57")),
                title = "Beck",
                episodes = 26,
                type = Anime.Type.TV,
                location = "some/relative/path/beck",
        )

        val watchListEntry1 = URL("https://myanimelist.net/anime/37989")
        val watchListEntry2 = URL("https://myanimelist.net/anime/40059")

        val ignoreListEntry1 = URL("https://myanimelist.net/anime/28981")
        val ignoreListEntry2 = URL("https://myanimelist.net/anime/33245")
        val ignoreListEntry3 = URL("https://myanimelist.net/anime/35923")
        val ignoreListEntry4 = URL("https://myanimelist.net/anime/31139")
        val ignoreListEntry5 = URL("https://myanimelist.net/anime/37747")


        val cmd = CmdAddEntriesFromParsedFile(
                parsedFile = ParsedFile(
                        animeListEntries = setOf(
                                animeListEntry1,
                                animeListEntry2,
                        ),
                        watchListEntries = setOf(
                                watchListEntry1,
                                watchListEntry2,
                        ),
                        ignoreListEntries = setOf(
                                ignoreListEntry1,
                                ignoreListEntry2,
                                ignoreListEntry3,
                                ignoreListEntry4,
                                ignoreListEntry5,
                        ),
                )
        )

        // when
        cmd.execute()

        // then
        assertThat(InternalState.animeList()).containsExactlyInAnyOrder(
                animeListEntry1,
                animeListEntry2,
        )
        assertThat(InternalState.watchList()).containsExactlyInAnyOrder(
                watchListEntry1,
                watchListEntry2,
        )
        assertThat(InternalState.ignoreList()).containsExactlyInAnyOrder(
                ignoreListEntry1,
                ignoreListEntry2,
                ignoreListEntry3,
                ignoreListEntry4,
                ignoreListEntry5,
        )
    }
}