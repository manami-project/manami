package io.github.manamiproject.manami.app.file

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.extensions.readFile
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.test.tempDirectory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.URI

internal class DefaultFileWriterTest {

    @Test
    fun `ensure that the structure of the written file is correct`() {
        tempDirectory {
            // given
            val animeListEntry1 = AnimeListEntry(
                title = "H2O: Footprints in the Sand",
                episodes = 4,
                type = Anime.Type.Special,
                location = URI("some/relative/path/h2o_-_footprints_in_the_sand_special"),
            )
            val animeListEntry2 = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                episodes = 26,
                type = Anime.Type.TV,
                location = URI("some/relative/path/beck"),
            )
            val watchListEntry1 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/5114"),
                title = "Fullmetal Alchemist: Brotherhood",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"),
            )
            val watchListEntry2 = WatchListEntry(
                link = Link("https://myanimelist.net/anime/1535"),
                title = "Death Note",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/9/9453t.jpg")
            )
            val ignoreListEntry1 = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            )
            val ignoreListEntry2 = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = listOf(animeListEntry1, animeListEntry2)
                override fun watchList(): Set<WatchListEntry> = setOf(watchListEntry1, watchListEntry2)
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(ignoreListEntry1, ignoreListEntry2)
            }

            val fileWriter = DefaultFileWriter(state = testState)

            // when
            fileWriter.writeTo(tempDir.resolve("test.xml"))

            // then
            val content = tempDir.resolve("test.xml").readFile()
            assertThat(content).isEqualTo("""
                <?xml version="1.1" encoding="UTF-8"?>
                <!DOCTYPE manami SYSTEM "manami_3.0.0.dtd">
                <manami version="3.0.0">
                  <animeList>
                    <animeListEntry link="https://myanimelist.net/anime/57" title="Beck" type="TV" episodes="26" location="some/relative/path/beck"/>
                    <animeListEntry link="" title="H2O: Footprints in the Sand" type="Special" episodes="4" location="some/relative/path/h2o_-_footprints_in_the_sand_special"/>
                  </animeList>
                  <watchList>
                    <watchListEntry link="https://myanimelist.net/anime/1535" title="Death Note" thumbnail="https://cdn.myanimelist.net/images/anime/9/9453t.jpg"/>
                    <watchListEntry link="https://myanimelist.net/anime/5114" title="Fullmetal Alchemist: Brotherhood" thumbnail="https://cdn.myanimelist.net/images/anime/1223/96541t.jpg"/>
                  </watchList>
                  <ignoreList>
                    <ignoreListEntry link="https://myanimelist.net/anime/37989" title="Golden Kamuy 2nd Season" thumbnail="https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"/>
                    <ignoreListEntry link="https://myanimelist.net/anime/40059" title="Golden Kamuy 3rd Season" thumbnail="https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"/>
                  </ignoreList>
                </manami>
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
            fileWriter.writeTo(tempDir.resolve("test.xml"))

            // then
            val content = tempDir.resolve("test.xml").readFile()
            assertThat(content).isEqualTo("""
                <?xml version="1.1" encoding="UTF-8"?>
                <!DOCTYPE manami SYSTEM "manami_3.0.0.dtd">
                <manami version="3.0.0">
                  <animeList>
                  </animeList>
                  <watchList>
                  </watchList>
                  <ignoreList>
                  </ignoreList>
                </manami>
            """.trimIndent())
        }
    }

    @Test
    fun `writes dtd file along with the actual file`() {
        tempDirectory {
            // given
            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val fileWriter = DefaultFileWriter(state = testState)

            // when
            fileWriter.writeTo(tempDir.resolve("test.xml"))

            // then
            assertThat(tempDir.resolve("manami_3.0.0.dtd")).exists()
        }
    }

    @Test
    fun `dtd content for version 3 0 0`() {
        tempDirectory {
            // given

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val fileWriter = DefaultFileWriter(state = testState)

            // when
            fileWriter.writeTo(tempDir.resolve("test.xml"))

            // then
            assertThat(tempDir.resolve("manami_3.0.0.dtd").readFile()).isEqualTo(
                """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <!ELEMENT manami (animeList, watchList, ignoreList)>
                    <!ATTLIST manami version CDATA #REQUIRED>
        
                    <!ELEMENT animeList (animeListEntry*)>
                    <!ELEMENT animeListEntry EMPTY>
                    <!ATTLIST animeListEntry episodes CDATA #REQUIRED
                        link CDATA #IMPLIED
                        title CDATA #REQUIRED
                        type CDATA #REQUIRED
                        episodes CDATA #REQUIRED
                        location CDATA #REQUIRED
                    >
        
                    <!ELEMENT watchList (watchListEntry*)>
                    <!ELEMENT watchListEntry EMPTY>
                    <!ATTLIST watchListEntry episodes CDATA #REQUIRED
                        link CDATA #REQUIRED
                        title CDATA #REQUIRED
                        thumbnail CDATA #REQUIRED
                    >
        
                    <!ELEMENT ignoreList (ignoreListEntry*)>
                    <!ELEMENT ignoreListEntry EMPTY>
                    <!ATTLIST ignoreListEntry episodes CDATA #REQUIRED
                        link CDATA #REQUIRED
                        title CDATA #REQUIRED
                        thumbnail CDATA #REQUIRED
                    >
                """.trimIndent()
            )
        }
    }
}