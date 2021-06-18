package io.github.manamiproject.manami.app.import

import io.github.manamiproject.manami.app.cache.*
import io.github.manamiproject.manami.app.cache.TestAnimeCache
import io.github.manamiproject.manami.app.import.parser.ParsedFile
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.collections.SortedList
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.ONGOING
import io.github.manamiproject.modb.core.models.Anime.Status.FINISHED
import io.github.manamiproject.modb.core.models.Anime.Type.SPECIAL
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.core.models.AnimeSeason
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.FALL
import io.github.manamiproject.modb.core.models.AnimeSeason.Season.WINTER
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
    fun `create AnimeListEntry, WatchListEntry and IgnoreListEntry from cache and correctly populate cache - AnimeListEntries without link will use default thumbnail`() {
        // given
        val animeListEntry1 = Anime(
            sources = SortedList(
                URI("https://myanimelist.net/anime/57"),
            ),
            _title = "Beck",
            type = TV,
            episodes = 26,
            status = FINISHED,
            animeSeason = AnimeSeason(
                season = FALL,
                year = 2004,
            ),
            picture = URI("https://cdn.myanimelist.net/images/anime/11/11636.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
        )
        val animeListEntry2 = Anime(
            sources = SortedList(
                URI("https://myanimelist.net/anime/3299"),
            ),
            _title = "H2O: Footprints in the Sand",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(
                season = WINTER,
                year = 2008,
            ),
            picture = URI("https://cdn.myanimelist.net/images/anime/2/5962.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/2/5962t.jpg"),
        )

        val watchListEntry1 = Anime(
            sources = SortedList(URI("https://myanimelist.net/anime/37989")),
            _title = "Golden Kamuy 2nd Season",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
            duration = Duration(23, MINUTES)
        )
        val watchListEntry2 = Anime(
            sources = SortedList(URI("https://myanimelist.net/anime/40059")),
            _title = "Golden Kamuy 3rd Season",
            type = TV,
            episodes = 12,
            status = ONGOING,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/1763/108108.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1763/108108t.jpg"),
            duration = Duration(23, MINUTES)
        )

        val ignoreListEntry1 = Anime(
            sources = SortedList(URI("https://myanimelist.net/anime/28981")),
            _title = "Ame-iro Cocoa",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/10/72517.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/72517t.jpg"),
            duration = Duration(2, MINUTES)
        )
        val ignoreListEntry2 = Anime(
            sources = SortedList(URI("https://myanimelist.net/anime/33245")),
            _title = "Ame-iro Cocoa in Hawaii",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/3/82186.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/3/82186t.jpg"),
            duration = Duration(2, MINUTES)
        )

        val testAnimeCache = object : Cache<URI, CacheEntry<Anime>> by TestAnimeCache{
            override fun fetch(key: URI): CacheEntry<Anime> {
                return when(key) {
                    animeListEntry1.sources.first() -> PresentValue(animeListEntry1)
                    animeListEntry2.sources.first() -> PresentValue(animeListEntry2)
                    watchListEntry1.sources.first() -> PresentValue(watchListEntry1)
                    watchListEntry2.sources.first() -> PresentValue(watchListEntry2)
                    ignoreListEntry1.sources.first() -> PresentValue(ignoreListEntry1)
                    ignoreListEntry2.sources.first() -> PresentValue(ignoreListEntry2)
                    else -> shouldNotBeInvoked()
                }
            }
        }

        val testState = object: State by TestState {
            private val animeList = mutableListOf<AnimeListEntry>()
            override fun animeList(): List<AnimeListEntry> = animeList
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                animeList.addAll(anime)
            }

            private val watchList = mutableSetOf<WatchListEntry>()
            override fun watchList(): Set<WatchListEntry> = watchList
            override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                watchList.addAll(anime)
            }

            private val ignoreList = mutableSetOf<IgnoreListEntry>()
            override fun ignoreList(): Set<IgnoreListEntry> = ignoreList
            override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                ignoreList.addAll(anime)
            }
        }

        val cmd = CmdAddEntriesFromParsedFile(
            state = testState,
            parsedFile = ParsedFile(
                    animeListEntries = setOf(
                        AnimeListEntry(
                            title = "H2O: Footprints in the Sand",
                            episodes = 4,
                            type = SPECIAL,
                            location = URI("some/relative/path/h2o_-_footprints_in_the_sand_special"),
                        ),
                        AnimeListEntry(
                            link = Link("https://myanimelist.net/anime/57"),
                            title = "Beck",
                            episodes = 26,
                            type = TV,
                            location = URI("some/relative/path/beck"),
                        ),
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
            AnimeListEntry(
                title = "H2O: Footprints in the Sand",
                episodes = 4,
                type = SPECIAL,
                location = URI("some/relative/path/h2o_-_footprints_in_the_sand_special"),
            ),
            AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = URI("some/relative/path/beck"),
            ),
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
    fun `ignore WatchListEntry and IgnoreListEntry if the cache doesn't return any Anime for it and use default thumbnail for AnimeListEntry`() {
        // given
        val animeListEntry = Anime(
            sources = SortedList(
                URI("https://myanimelist.net/anime/57"),
            ),
            _title = "Beck",
            type = TV,
            episodes = 26,
            status = FINISHED,
            animeSeason = AnimeSeason(
                season = FALL,
                year = 2004,
            ),
            picture = URI("https://cdn.myanimelist.net/images/anime/11/11636.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
        )

        val watchListEntry = Anime(
            sources = SortedList(URI("https://myanimelist.net/anime/37989")),
            _title = "Golden Kamuy 2nd Season",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/1180/95018.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/1180/95018t.jpg"),
            duration = Duration(23, MINUTES)
        )

        val ignoreListEntry = Anime(
            sources = SortedList(URI("https://myanimelist.net/anime/28981")),
            _title = "Ame-iro Cocoa",
            type = TV,
            episodes = 12,
            status = FINISHED,
            animeSeason = AnimeSeason(),
            picture = URI("https://cdn.myanimelist.net/images/anime/10/72517.jpg"),
            thumbnail = URI("https://cdn.myanimelist.net/images/anime/10/72517t.jpg"),
            duration = Duration(2, MINUTES)
        )

        val testAnimeCache = object : Cache<URI, CacheEntry<Anime>> by TestAnimeCache{
            override fun fetch(key: URI): CacheEntry<Anime> {
                return when(key) {
                    animeListEntry.sources.first() -> PresentValue(animeListEntry)
                    watchListEntry.sources.first() -> PresentValue(watchListEntry)
                    ignoreListEntry.sources.first() -> PresentValue(ignoreListEntry)
                    else -> Empty()
                }
            }
        }

        val testState = object: State by TestState {
            private val animeList = mutableListOf<AnimeListEntry>()
            override fun animeList(): List<AnimeListEntry> = animeList
            override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                animeList.addAll(anime)
            }

            private val watchList = mutableSetOf<WatchListEntry>()
            override fun watchList(): Set<WatchListEntry> = watchList
            override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                watchList.addAll(anime)
            }

            private val ignoreList = mutableSetOf<IgnoreListEntry>()
            override fun ignoreList(): Set<IgnoreListEntry> = ignoreList
            override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                ignoreList.addAll(anime)
            }
        }

        val cmd = CmdAddEntriesFromParsedFile(
            state = testState,
            parsedFile = ParsedFile(
                animeListEntries = setOf(
                    AnimeListEntry(
                        link = Link(URI("https://myanimelist.net/anime/3299")),
                        title = "H2O: Footprints in the Sand",
                        episodes = 4,
                        type = SPECIAL,
                        location = URI("some/relative/path/h2o_-_footprints_in_the_sand_special"),
                    ),
                    AnimeListEntry(
                        link = Link("https://myanimelist.net/anime/57"),
                        title = "Beck",
                        episodes = 26,
                        type = TV,
                        location = URI("some/relative/path/beck"),
                    ),
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
            AnimeListEntry(
                link = Link(URI("https://myanimelist.net/anime/3299")),
                title = "H2O: Footprints in the Sand",
                episodes = 4,
                type = SPECIAL,
                location = URI("some/relative/path/h2o_-_footprints_in_the_sand_special"),
            ),
            AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = URI("some/relative/path/beck"),
            )
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