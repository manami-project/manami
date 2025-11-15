package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.versioning.SemanticVersion
import io.github.manamiproject.manami.app.versioning.TestVersionProvider
import io.github.manamiproject.manami.app.versioning.VersionProvider
import io.github.manamiproject.modb.core.anime.AnimeType.SPECIAL
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.io.path.Path

internal class DefaultFileWriterTest {

    @Test
    fun `ensure that the structure of the written file is correct`() {
        tempDirectory {
            // given
            val animeListEntry1 = AnimeListEntry(
                title = "H2O: Footprints in the Sand",
                episodes = 4,
                type = SPECIAL,
                location = Path("some/relative/path/h2o_-_footprints_in_the_sand_special"),
            )
            val animeListEntry2 = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/248"),
                title = "Ichigo 100%",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/5/20036.jpg"),
                episodes = 12,
                type = TV,
                location = Path("some/relative/path/ichigo_100%"),
            )
            val animeListEntry3 = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/11235"),
                title = "Amagami SS+ Plus",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/13/33359.jpg"),
                episodes = 13,
                type = TV,
                location = Path("some/relative/path/amagami_ss+_plus"),
            )
            val watchListEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541.jpg"),
            )
            val watchListEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453.jpg"),
            )
            val ignoreListEntry1 = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
            )
            val ignoreListEntry2 = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108.jpg"),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = listOf(animeListEntry1, animeListEntry2, animeListEntry3)
                override fun watchList(): Set<WatchListEntry> = setOf(watchListEntry1, watchListEntry2)
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(ignoreListEntry1, ignoreListEntry2)
            }

            val fileWriter = DefaultFileWriter(state = testState)

            // when
            fileWriter.writeTo(tempDir.resolve("test.json"))

            // then
            val content = tempDir.resolve("test.json").readFile()
            assertThat(content).isEqualTo("""
                  {"version":"3.0.0","animeListEntries":[{"link":"https://myanimelist.net/anime/11235","title":"Amagami SS+ Plus","thumbnail":"https://cdn.myanimelist.net/images/anime/13/33359.jpg","episodes":"13","type":"TV","location":"some/relative/path/amagami_ss+_plus"},{"title":"H2O: Footprints in the Sand","thumbnail":"https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png","episodes":"4","type":"SPECIAL","location":"some/relative/path/h2o_-_footprints_in_the_sand_special"},{"link":"https://myanimelist.net/anime/248","title":"Ichigo 100%","thumbnail":"https://cdn.myanimelist.net/images/anime/5/20036.jpg","episodes":"12","type":"TV","location":"some/relative/path/ichigo_100%"}],"watchListEntries":["https://myanimelist.net/anime/1535","https://myanimelist.net/anime/5114"],"ignoreListEntries":["https://myanimelist.net/anime/37989","https://myanimelist.net/anime/40059"]}
            """.trimIndent())
        }
    }

    @Test
    fun `ensure that the structure of the written file is correct for completely empty lists`() {
        tempDirectory {
            // given
            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val fileWriter = DefaultFileWriter(state = testState)

            // when
            fileWriter.writeTo(tempDir.resolve("test.json"))

            // then
            val content = tempDir.resolve("test.json").readFile()
            assertThat(content).isEqualTo("""
                {"version":"3.0.0","animeListEntries":[],"watchListEntries":[],"ignoreListEntries":[]}
            """.trimIndent())
        }
    }

    @Test
    fun `dynamically fetch version from build file and use it in dtd file name and in xml attribute`() {
        tempDirectory {
            // given
            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val testVersionProvider = object: VersionProvider by TestVersionProvider {
                override suspend fun version(): SemanticVersion = SemanticVersion("3.1.0")
            }

            val fileWriter = DefaultFileWriter(
                state = testState,
                versionProvider = testVersionProvider,
            )

            // when
            fileWriter.writeTo(tempDir.resolve("test.json"))

            // then
            val content = tempDir.resolve("test.json").readFile()
            assertThat(content).isEqualTo("""
                {"version":"3.1.0","animeListEntries":[],"watchListEntries":[],"ignoreListEntries":[]}
            """.trimIndent())
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultFileWriter.instance

            // when
            val result = DefaultFileWriter.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultFileWriter::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}