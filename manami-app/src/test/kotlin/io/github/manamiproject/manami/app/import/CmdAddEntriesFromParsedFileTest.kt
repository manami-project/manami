package io.github.manamiproject.manami.app.import

import io.github.manamiproject.manami.app.cache.Cache
import io.github.manamiproject.manami.app.cache.TestAnimeCache
import io.github.manamiproject.manami.app.import.parser.ParsedFile
import io.github.manamiproject.manami.app.models.AnimeListEntry
import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.app.models.Link
import io.github.manamiproject.manami.app.models.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.CURRENTLY
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.Duration
import io.github.manamiproject.modb.core.models.Duration.TimeUnit.MINUTES
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.net.URI

internal class CmdAddEntriesFromParsedFileTest {

    @AfterEach
    fun afterEach() {
        InternalState.clear()
    }

    @Test
    fun `create WatchListeEntry and IgnoreListEntry from cache and correctly populate cache`() {
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
                type = TV,
                location = URI("some/relative/path/beck"),
        )

        val watchListEntry1 = Anime(
            _title = "Golden Kamuy 2nd Season",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
            duration = Duration(23, MINUTES)
        ).apply {
            addSources(URI("https://myanimelist.net/anime/37989"))
        }
        val watchListEntry2 = Anime(
            _title = "Golden Kamuy 3rd Season",
            type = TV,
            episodes = 12,
            status = CURRENTLY,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/1763/108108.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
            duration = Duration(23, MINUTES)
        ).apply {
            addSources(URI("https://myanimelist.net/anime/40059"))
        }

        val ignoreListEntry1 = Anime(
            _title = "Ame-iro Cocoa",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/10/72517.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/72517t.jpg"),
            duration = Duration(2, MINUTES)
        ).apply {
            addSources(URI("https://myanimelist.net/anime/28981"))
        }
        val ignoreListEntry2 = Anime(
            _title = "Ame-iro Cocoa in Hawaii",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/3/82186.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/3/82186t.jpg"),
            duration = Duration(2, MINUTES)
        ).apply {
            addSources(URI("https://myanimelist.net/anime/33245"))
        }

        val testAnimeCache = object : Cache<URI, Anime?> by TestAnimeCache{
            override fun fetch(key: URI): Anime? {
                return when(key) {
                    watchListEntry1.sources.first() -> watchListEntry1
                    watchListEntry2.sources.first() -> watchListEntry2
                    ignoreListEntry1.sources.first() -> ignoreListEntry1
                    ignoreListEntry2.sources.first() -> ignoreListEntry2
                    else -> shouldNotBeInvoked()
                }
            }
        }

        val testState = object: State by TestState {
            private val animeList = mutableListOf<AnimeListEntry>()
            override fun animeList(): List<AnimeListEntry> = animeList
            override fun addAllAnimeListEntries(anime: Set<AnimeListEntry>) {
                animeList.addAll(anime)
            }

            private val watchList = mutableSetOf<WatchListEntry>()
            override fun watchList(): Set<WatchListEntry> = watchList
            override fun addAllWatchListEntries(anime: Set<WatchListEntry>) {
                watchList.addAll(anime)
            }

            private val ignoreList = mutableSetOf<IgnoreListEntry>()
            override fun ignoreList(): Set<IgnoreListEntry> = ignoreList
            override fun addAllIgnoreListEntries(anime: Set<IgnoreListEntry>) {
                ignoreList.addAll(anime)
            }
        }

        val cmd = CmdAddEntriesFromParsedFile(
            state = testState,
            parsedFile = ParsedFile(
                    animeListEntries = setOf(
                        animeListEntry1,
                        animeListEntry2,
                    ),
                    watchListEntries = setOf(
                        URI("https://myanimelist.net/anime/37989"),
                        URI("https://myanimelist.net/anime/40059"),
                    ),
                    ignoreListEntries = setOf(
                        URI("https://myanimelist.net/anime/28981"),
                        URI("https://myanimelist.net/anime/33245"),
                    ),
            ),
            cache = testAnimeCache,
        )

        // when
        cmd.execute()

        // then
        assertThat(testState.animeList()).containsExactlyInAnyOrder(
                animeListEntry1,
                animeListEntry2,
        )
        assertThat(testState.watchList()).containsExactlyInAnyOrder(
            WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            ),
            WatchListEntry(
                link = Link("https://myanimelist.net/anime/40059"),
                title = "Golden Kamuy 3rd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg")
            ),
        )
        assertThat(testState.ignoreList()).containsExactlyInAnyOrder(
            IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/28981"),
                title = "Ame-iro Cocoa",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/72517t.jpg")
            ),
            IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/33245"),
                title = "Ame-iro Cocoa in Hawaii",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/3/82186t.jpg")
            ),
        )
    }

    @Test
    fun `ignore WatchListEntrie and IgnoreListEntry if the cache doesn't return any Anime for it`() {
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
            type = TV,
            location = URI("some/relative/path/beck"),
        )

        val watchListEntry = Anime(
            _title = "Golden Kamuy 2nd Season",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
            duration = Duration(23, MINUTES)
        ).apply {
            addSources(URI("https://myanimelist.net/anime/37989"))
        }

        val ignoreListEntry = Anime(
            _title = "Ame-iro Cocoa",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/10/72517.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/72517t.jpg"),
            duration = Duration(2, MINUTES)
        ).apply {
            addSources(URI("https://myanimelist.net/anime/28981"))
        }

        val testAnimeCache = object : Cache<URI, Anime?> by TestAnimeCache{
            override fun fetch(key: URI): Anime? {
                return when(key) {
                    watchListEntry.sources.first() -> watchListEntry
                    ignoreListEntry.sources.first() -> ignoreListEntry
                    else -> null
                }
            }
        }

        val testState = object: State by TestState {
            private val animeList = mutableListOf<AnimeListEntry>()
            override fun animeList(): List<AnimeListEntry> = animeList
            override fun addAllAnimeListEntries(anime: Set<AnimeListEntry>) {
                animeList.addAll(anime)
            }

            private val watchList = mutableSetOf<WatchListEntry>()
            override fun watchList(): Set<WatchListEntry> = watchList
            override fun addAllWatchListEntries(anime: Set<WatchListEntry>) {
                watchList.addAll(anime)
            }

            private val ignoreList = mutableSetOf<IgnoreListEntry>()
            override fun ignoreList(): Set<IgnoreListEntry> = ignoreList
            override fun addAllIgnoreListEntries(anime: Set<IgnoreListEntry>) {
                ignoreList.addAll(anime)
            }
        }

        val cmd = CmdAddEntriesFromParsedFile(
            state = testState,
            parsedFile = ParsedFile(
                animeListEntries = setOf(
                    animeListEntry1,
                    animeListEntry2,
                ),
                watchListEntries = setOf(
                    URI("https://myanimelist.net/anime/37989"),
                    URI("https://myanimelist.net/anime/40059"),
                ),
                ignoreListEntries = setOf(
                    URI("https://myanimelist.net/anime/28981"),
                    URI("https://myanimelist.net/anime/33245"),
                ),
            ),
            cache = testAnimeCache,
        )

        // when
        cmd.execute()

        // then
        assertThat(testState.animeList()).containsExactlyInAnyOrder(
            animeListEntry1,
            animeListEntry2,
        )
        assertThat(testState.watchList()).containsExactlyInAnyOrder(
            WatchListEntry(
                link = Link("https://myanimelist.net/anime/37989"),
                title = "Golden Kamuy 2nd Season",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg")
            ),
        )
        assertThat(testState.ignoreList()).containsExactlyInAnyOrder(
            IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/28981"),
                title = "Ame-iro Cocoa",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/72517t.jpg")
            ),
        )
    }
}