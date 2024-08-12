package io.github.manamiproject.manami.app.migration

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.cache.TestAnimeCache
import io.github.manamiproject.manami.app.commands.TestCommandHistory
import io.github.manamiproject.manami.app.events.Event
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.TestEventBus
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Type.TV
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.nio.file.Paths

internal class DefaultMetaDataMigrationHandlerTest {

    @Nested
    inner class CheckMigrationTests {

        @Test
        fun `throws exception if hostname of current entry ist not provided`() {
            // given
            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(KitsuConfig.hostname())
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = TestEventBus,
                commandHistory = TestCommandHistory,
                state = TestState,
            )

            // when
            val result = assertThrows<IllegalArgumentException> {
                defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())
            }

            // then
            assertThat(result).hasMessage("MetaDataProvider [myanimelist.net] is not supported.")
        }

        @Test
        fun `throws exception if hostname of new entry ist not provided`() {
            // given
            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname())
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = TestEventBus,
                commandHistory = TestCommandHistory,
                state = TestState,
            )

            // when
            val result = assertThrows<IllegalArgumentException> {
                defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())
            }

            // then
            assertThat(result).hasMessage("MetaDataProvider [kitsu.app] is not supported.")
        }

        @Test
        fun `any AnimeListEntry without a link is being ignored`() {
            // given
            val catchedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    catchedEvents.add(event)
                }
            }

            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
            }

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = listOf(
                    AnimeListEntry(
                        link = NoLink,
                        title = "Beck",
                        thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                        episodes = 26,
                        type = TV,
                        location = Paths.get("."),
                    ),
                )
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = testEventBus,
                commandHistory = TestCommandHistory,
                state = testState,
            )

            // when
            defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

            // then
            assertThat(catchedEvents).hasSize(1)
            assertThat(catchedEvents).containsExactly(
                MetaDataMigrationResultEvent(
                    animeListEntriesWithoutMapping = emptyList(),
                    animeListEntiresMultipleMappings = emptyMap(),
                    animeListMappings = emptyMap(),
                    watchListEntriesWithoutMapping = emptyList(),
                    watchListEntiresMultipleMappings = emptyMap(),
                    watchListMappings = emptyMap(),
                    ignoreListEntriesWithoutMapping = emptyList(),
                    ignoreListEntiresMultipleMappings = emptyMap(),
                    ignoreListMappings = emptyMap(),
                )
            )
        }

        @Test
        fun `AnimeListEntry without mapping`() {
            // given
            val catchedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    catchedEvents.add(event)
                }
            }

            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = emptySet()
            }

            val currentAnimeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = Paths.get("."),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = listOf(
                    currentAnimeListEntry,
                )
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = testEventBus,
                commandHistory = TestCommandHistory,
                state = testState,
            )

            // when
            defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

            // then
            assertThat(catchedEvents).hasSize(2)
            assertThat(catchedEvents).containsExactly(
                MetaDataMigrationProgressEvent(
                    finishedTasks = 1,
                    numberOfTasks = 1,
                ),
                MetaDataMigrationResultEvent(
                    animeListEntriesWithoutMapping = listOf(currentAnimeListEntry),
                    animeListEntiresMultipleMappings = emptyMap(),
                    animeListMappings = emptyMap(),
                    watchListEntriesWithoutMapping = emptyList(),
                    watchListEntiresMultipleMappings = emptyMap(),
                    watchListMappings = emptyMap(),
                    ignoreListEntriesWithoutMapping = emptyList(),
                    ignoreListEntiresMultipleMappings = emptyMap(),
                    ignoreListMappings = emptyMap(),
                )
            )
        }

        @Test
        fun `AnimeListEntry having one on one mapping`() {
            // given
            val catchedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    catchedEvents.add(event)
                }
            }

            val kitsuLink = KitsuConfig.buildAnimeLink("38")

            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                    kitsuLink
                )

                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        _title = "Beck",
                        sources = hashSetOf(kitsuLink),
                    )
                )
            }

            val currentAnimeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = Paths.get("."),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = listOf(
                    currentAnimeListEntry
                )
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = testEventBus,
                commandHistory = TestCommandHistory,
                state = testState,
            )

            // when
            defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

            // then
            assertThat(catchedEvents).hasSize(2)
            assertThat(catchedEvents).containsExactly(
                MetaDataMigrationProgressEvent(
                    finishedTasks = 1,
                    numberOfTasks = 1,
                ),
                MetaDataMigrationResultEvent(
                    animeListEntriesWithoutMapping = emptyList(),
                    animeListEntiresMultipleMappings = emptyMap(),
                    animeListMappings = mapOf(
                        currentAnimeListEntry to Link(kitsuLink)
                    ),
                    watchListEntriesWithoutMapping = emptyList(),
                    watchListEntiresMultipleMappings = emptyMap(),
                    watchListMappings = emptyMap(),
                    ignoreListEntriesWithoutMapping = emptyList(),
                    ignoreListEntiresMultipleMappings = emptyMap(),
                    ignoreListMappings = emptyMap(),
                )
            )
        }

        @Test
        fun `AnimeListEntry having multiple mappings`() {
            // given
            val catchedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    catchedEvents.add(event)
                }
            }

            val firstKitsuLink = KitsuConfig.buildAnimeLink("38")
            val secondKitsuLink = KitsuConfig.buildAnimeLink("99999999")

            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                    firstKitsuLink,
                    secondKitsuLink,
                )

                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        _title = "Beck",
                        sources = hashSetOf(firstKitsuLink, secondKitsuLink),
                    ),
                )
            }

            val currentAnimeListEntry = AnimeListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
                episodes = 26,
                type = TV,
                location = Paths.get("."),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = listOf(
                    currentAnimeListEntry
                )
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = testEventBus,
                commandHistory = TestCommandHistory,
                state = testState,
            )

            // when
            defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

            // then
            assertThat(catchedEvents).hasSize(2)
            assertThat(catchedEvents).containsExactly(
                MetaDataMigrationProgressEvent(
                    finishedTasks = 1,
                    numberOfTasks = 1,
                ),
                MetaDataMigrationResultEvent(
                    animeListEntriesWithoutMapping = emptyList(),
                    animeListEntiresMultipleMappings = mapOf(
                        currentAnimeListEntry to setOf(Link(firstKitsuLink), Link(secondKitsuLink))
                    ),
                    animeListMappings = emptyMap(),
                    watchListEntriesWithoutMapping = emptyList(),
                    watchListEntiresMultipleMappings = emptyMap(),
                    watchListMappings = emptyMap(),
                    ignoreListEntriesWithoutMapping = emptyList(),
                    ignoreListEntiresMultipleMappings = emptyMap(),
                    ignoreListMappings = emptyMap(),
                )
            )
        }

        @Test
        fun `WatchListEntry without mapping`() {
            // given
            val catchedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    catchedEvents.add(event)
                }
            }

            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = emptySet()
            }

            val currentWatchListEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = listOf()
                override fun watchList(): Set<WatchListEntry> = setOf(
                    currentWatchListEntry,
                )
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = testEventBus,
                commandHistory = TestCommandHistory,
                state = testState,
            )

            // when
            defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

            // then
            assertThat(catchedEvents).hasSize(2)
            assertThat(catchedEvents).containsExactly(
                MetaDataMigrationProgressEvent(
                    finishedTasks = 1,
                    numberOfTasks = 1,
                ),
                MetaDataMigrationResultEvent(
                    animeListEntriesWithoutMapping = emptyList(),
                    animeListEntiresMultipleMappings = emptyMap(),
                    animeListMappings = emptyMap(),
                    watchListEntriesWithoutMapping = listOf(currentWatchListEntry),
                    watchListEntiresMultipleMappings = emptyMap(),
                    watchListMappings = emptyMap(),
                    ignoreListEntriesWithoutMapping = emptyList(),
                    ignoreListEntiresMultipleMappings = emptyMap(),
                    ignoreListMappings = emptyMap(),
                )
            )
        }

        @Test
        fun `WatchListEntry having one on one mapping`() {
            // given
            val catchedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    catchedEvents.add(event)
                }
            }

            val kitsuLink = KitsuConfig.buildAnimeLink("38")

            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                    kitsuLink
                )

                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        _title = "Beck",
                        sources = hashSetOf(kitsuLink),
                    )
                )
            }

            val currentWatchListEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun watchList(): Set<WatchListEntry> = setOf(currentWatchListEntry)
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = testEventBus,
                commandHistory = TestCommandHistory,
                state = testState,
            )

            // when
            defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

            // then
            assertThat(catchedEvents).hasSize(2)
            assertThat(catchedEvents).containsExactly(
                MetaDataMigrationProgressEvent(
                    finishedTasks = 1,
                    numberOfTasks = 1,
                ),
                MetaDataMigrationResultEvent(
                    animeListEntriesWithoutMapping = emptyList(),
                    animeListEntiresMultipleMappings = emptyMap(),
                    animeListMappings = emptyMap(),
                    watchListEntriesWithoutMapping = emptyList(),
                    watchListEntiresMultipleMappings = emptyMap(),
                    watchListMappings = mapOf(
                        currentWatchListEntry to Link(kitsuLink)
                    ),
                    ignoreListEntriesWithoutMapping = emptyList(),
                    ignoreListEntiresMultipleMappings = emptyMap(),
                    ignoreListMappings = emptyMap(),
                )
            )
        }

        @Test
        fun `WatchListEntry having multiple mappings`() {
            // given
            val catchedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    catchedEvents.add(event)
                }
            }

            val firstKitsuLink = KitsuConfig.buildAnimeLink("38")
            val secondKitsuLink = KitsuConfig.buildAnimeLink("99999999")

            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                    firstKitsuLink,
                    secondKitsuLink,
                )

                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        _title = "Beck",
                        sources = hashSetOf(firstKitsuLink, secondKitsuLink),
                    ),
                )
            }

            val currentWatchListEntry = WatchListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun watchList(): Set<WatchListEntry> = setOf(
                    currentWatchListEntry,
                )
                override fun ignoreList(): Set<IgnoreListEntry> = emptySet()
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = testEventBus,
                commandHistory = TestCommandHistory,
                state = testState,
            )

            // when
            defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

            // then
            assertThat(catchedEvents).hasSize(2)
            assertThat(catchedEvents).containsExactly(
                MetaDataMigrationProgressEvent(
                    finishedTasks = 1,
                    numberOfTasks = 1,
                ),
                MetaDataMigrationResultEvent(
                    animeListEntriesWithoutMapping = emptyList(),
                    animeListEntiresMultipleMappings = emptyMap(),
                    animeListMappings = emptyMap(),
                    watchListEntriesWithoutMapping = emptyList(),
                    watchListEntiresMultipleMappings = mapOf(
                        currentWatchListEntry to setOf(Link(firstKitsuLink), Link(secondKitsuLink))
                    ),
                    watchListMappings = emptyMap(),
                    ignoreListEntriesWithoutMapping = emptyList(),
                    ignoreListEntiresMultipleMappings = emptyMap(),
                    ignoreListMappings = emptyMap(),
                )
            )
        }

        @Test
        fun `IgnoreListEntry without mapping`() {
            // given
            val catchedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    catchedEvents.add(event)
                }
            }

            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = emptySet()
            }

            val currentIgnoreListEntry = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = listOf()
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                    currentIgnoreListEntry,
                )
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = testEventBus,
                commandHistory = TestCommandHistory,
                state = testState,
            )

            // when
            defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

            // then
            assertThat(catchedEvents).hasSize(2)
            assertThat(catchedEvents).containsExactly(
                MetaDataMigrationProgressEvent(
                    finishedTasks = 1,
                    numberOfTasks = 1,
                ),
                MetaDataMigrationResultEvent(
                    animeListEntriesWithoutMapping = emptyList(),
                    animeListEntiresMultipleMappings = emptyMap(),
                    animeListMappings = emptyMap(),
                    watchListEntriesWithoutMapping = emptyList(),
                    watchListEntiresMultipleMappings = emptyMap(),
                    watchListMappings = emptyMap(),
                    ignoreListEntriesWithoutMapping = listOf(currentIgnoreListEntry),
                    ignoreListEntiresMultipleMappings = emptyMap(),
                    ignoreListMappings = emptyMap(),
                )
            )
        }

        @Test
        fun `IgnoreListEntry having one on one mapping`() {
            // given
            val catchedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    catchedEvents.add(event)
                }
            }

            val kitsuLink = KitsuConfig.buildAnimeLink("38")

            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                    kitsuLink
                )

                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        _title = "Beck",
                        sources = hashSetOf(kitsuLink),
                    ),
                )
            }

            val currentIgnoreListEntry = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                    currentIgnoreListEntry,
                )
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = testEventBus,
                commandHistory = TestCommandHistory,
                state = testState,
            )

            // when
            defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

            // then
            assertThat(catchedEvents).hasSize(2)
            assertThat(catchedEvents).containsExactly(
                MetaDataMigrationProgressEvent(
                    finishedTasks = 1,
                    numberOfTasks = 1,
                ),
                MetaDataMigrationResultEvent(
                    animeListEntriesWithoutMapping = emptyList(),
                    animeListEntiresMultipleMappings = emptyMap(),
                    animeListMappings = emptyMap(),
                    watchListEntriesWithoutMapping = emptyList(),
                    watchListEntiresMultipleMappings = emptyMap(),
                    watchListMappings = emptyMap(),
                    ignoreListEntriesWithoutMapping = emptyList(),
                    ignoreListEntiresMultipleMappings = emptyMap(),
                    ignoreListMappings = mapOf(
                        currentIgnoreListEntry to Link(kitsuLink)
                    ),
                )
            )
        }

        @Test
        fun `IgnoreListEntry having multiple mappings`() {
            // given
            val catchedEvents = mutableListOf<Event>()
            val testEventBus = object : EventBus by TestEventBus {
                override fun post(event: Event) {
                    catchedEvents.add(event)
                }
            }

            val firstKitsuLink = KitsuConfig.buildAnimeLink("38")
            val secondKitsuLink = KitsuConfig.buildAnimeLink("99999999")

            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                    firstKitsuLink,
                    secondKitsuLink,
                )

                override fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                    Anime(
                        _title = "Beck",
                        sources = hashSetOf(firstKitsuLink, secondKitsuLink),
                    ),
                )
            }

            val currentIgnoreListEntry = IgnoreListEntry(
                link = Link("https://myanimelist.net/anime/57"),
                title = "Beck",
                thumbnail = URI("https://cdn.myanimelist.net/images/anime/11/11636t.jpg"),
            )

            val testState = object: State by TestState {
                override fun animeList(): List<AnimeListEntry> = emptyList()
                override fun watchList(): Set<WatchListEntry> = emptySet()
                override fun ignoreList(): Set<IgnoreListEntry> = setOf(
                    currentIgnoreListEntry,
                )
            }

            val defaultMetaDataMigrationHandler = DefaultMetaDataMigrationHandler(
                cache = testCache,
                eventBus = testEventBus,
                commandHistory = TestCommandHistory,
                state = testState,
            )

            // when
            defaultMetaDataMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

            // then
            assertThat(catchedEvents).hasSize(2)
            assertThat(catchedEvents).containsExactly(
                MetaDataMigrationProgressEvent(
                    finishedTasks = 1,
                    numberOfTasks = 1,
                ),
                MetaDataMigrationResultEvent(
                    animeListEntriesWithoutMapping = emptyList(),
                    animeListEntiresMultipleMappings = emptyMap(),
                    animeListMappings = emptyMap(),
                    watchListEntriesWithoutMapping = emptyList(),
                    watchListEntiresMultipleMappings = emptyMap(),
                    watchListMappings = emptyMap(),
                    ignoreListEntriesWithoutMapping = emptyList(),
                    ignoreListEntiresMultipleMappings = mapOf(
                        currentIgnoreListEntry to setOf(Link(firstKitsuLink), Link(secondKitsuLink))
                    ),
                    ignoreListMappings = emptyMap(),
                )
            )
        }
    }
}