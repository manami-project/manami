package io.github.manamiproject.manami.app.migration

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.CacheEntry
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.cache.TestAnimeCache
import io.github.manamiproject.manami.app.commands.ReversibleCommand
import io.github.manamiproject.manami.app.commands.TestCommandHistory
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.MetaDataProviderMigrationState
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.manami.app.state.TestState
import io.github.manamiproject.manami.app.state.snapshot.Snapshot
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.anime.AnimeType.TV
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.kitsu.KitsuConfig
import io.github.manamiproject.modb.myanimelist.MyanimelistConfig
import io.github.manamiproject.modb.test.shouldNotBeInvoked
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.net.URI
import java.nio.file.Paths
import kotlin.test.AfterTest
import kotlin.test.Test

internal class DefaultMetaDataProviderMigrationHandlerTest {

    @AfterTest
    fun afterTest() {
        CoroutinesFlowEventBus.clear()
    }

    @Nested
    inner class CheckMigrationTests {

        @Test
        fun `throws exception if hostname of current entry ist not provided`() {
            // given
            val testCache = object: AnimeCache by TestAnimeCache {
                override val availableMetaDataProvider: Set<Hostname>
                    get() = setOf(KitsuConfig.hostname())
            }

            val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                cache = testCache,
                eventBus = CoroutinesFlowEventBus,
                commandHistory = TestCommandHistory,
                state = TestState,
            )

            // when
            val result = assertThrows<IllegalArgumentException> {
                runBlocking { defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname()) }
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

            val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                cache = testCache,
                eventBus = CoroutinesFlowEventBus,
                commandHistory = TestCommandHistory,
                state = TestState,
            )

            // when
            val result = assertThrows<IllegalArgumentException> {
                runBlocking { defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname()) }
            }

            // then
            assertThat(result).hasMessage("MetaDataProvider [kitsu.app] is not supported.")
        }

        @Test
        fun `AnimeListEntry without mapping`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

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

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = listOf(currentAnimeListEntry),
                        animeListEntriesMultipleMappings = emptyMap(),
                        animeListMappings = emptyMap(),
                        watchListEntriesWithoutMapping = emptyList(),
                        watchListEntriesMultipleMappings = emptyMap(),
                        watchListMappings = emptyMap(),
                        ignoreListEntriesWithoutMapping = emptyList(),
                        ignoreListEntriesMultipleMappings = emptyMap(),
                        ignoreListMappings = emptyMap(),
                    )
                )
            }
        }

        @Test
        fun `AnimeListEntry having one on one mapping`() {
            runBlocking {

                // given
                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

                val kitsuLink = KitsuConfig.buildAnimeLink("38")

                val testCache = object: AnimeCache by TestAnimeCache {
                    override val availableMetaDataProvider: Set<Hostname>
                        get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                    override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                        kitsuLink
                    )

                    override suspend fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                        Anime(
                            title = "Beck",
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

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = emptyList(),
                        animeListEntriesMultipleMappings = emptyMap(),
                        animeListMappings = mapOf(
                            currentAnimeListEntry to Link(kitsuLink)
                        ),
                        watchListEntriesWithoutMapping = emptyList(),
                        watchListEntriesMultipleMappings = emptyMap(),
                        watchListMappings = emptyMap(),
                        ignoreListEntriesWithoutMapping = emptyList(),
                        ignoreListEntriesMultipleMappings = emptyMap(),
                        ignoreListMappings = emptyMap(),
                    )
                )
            }
        }

        @Test
        fun `AnimeListEntry having multiple mappings`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

                val firstKitsuLink = KitsuConfig.buildAnimeLink("38")
                val secondKitsuLink = KitsuConfig.buildAnimeLink("99999999")

                val testCache = object: AnimeCache by TestAnimeCache {
                    override val availableMetaDataProvider: Set<Hostname>
                        get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                    override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                        firstKitsuLink,
                        secondKitsuLink,
                    )

                    override suspend fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                        Anime(
                            title = "Beck",
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

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = emptyList(),
                        animeListEntriesMultipleMappings = mapOf(
                            currentAnimeListEntry to setOf(Link(firstKitsuLink), Link(secondKitsuLink))
                        ),
                        animeListMappings = emptyMap(),
                        watchListEntriesWithoutMapping = emptyList(),
                        watchListEntriesMultipleMappings = emptyMap(),
                        watchListMappings = emptyMap(),
                        ignoreListEntriesWithoutMapping = emptyList(),
                        ignoreListEntriesMultipleMappings = emptyMap(),
                        ignoreListMappings = emptyMap(),
                    )
                )
            }
        }

        @Test
        fun `WatchListEntry without mapping`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

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

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = emptyList(),
                        animeListEntriesMultipleMappings = emptyMap(),
                        animeListMappings = emptyMap(),
                        watchListEntriesWithoutMapping = listOf(currentWatchListEntry),
                        watchListEntriesMultipleMappings = emptyMap(),
                        watchListMappings = emptyMap(),
                        ignoreListEntriesWithoutMapping = emptyList(),
                        ignoreListEntriesMultipleMappings = emptyMap(),
                        ignoreListMappings = emptyMap(),
                    )
                )
            }
        }

        @Test
        fun `WatchListEntry having one on one mapping`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

                val kitsuLink = KitsuConfig.buildAnimeLink("38")

                val testCache = object: AnimeCache by TestAnimeCache {
                    override val availableMetaDataProvider: Set<Hostname>
                        get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                    override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                        kitsuLink
                    )

                    override suspend fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                        Anime(
                            title = "Beck",
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

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = emptyList(),
                        animeListEntriesMultipleMappings = emptyMap(),
                        animeListMappings = emptyMap(),
                        watchListEntriesWithoutMapping = emptyList(),
                        watchListEntriesMultipleMappings = emptyMap(),
                        watchListMappings = mapOf(
                            currentWatchListEntry to Link(kitsuLink)
                        ),
                        ignoreListEntriesWithoutMapping = emptyList(),
                        ignoreListEntriesMultipleMappings = emptyMap(),
                        ignoreListMappings = emptyMap(),
                    )
                )
            }
        }

        @Test
        fun `WatchListEntry having multiple mappings`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

                val firstKitsuLink = KitsuConfig.buildAnimeLink("38")
                val secondKitsuLink = KitsuConfig.buildAnimeLink("99999999")

                val testCache = object: AnimeCache by TestAnimeCache {
                    override val availableMetaDataProvider: Set<Hostname>
                        get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                    override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                        firstKitsuLink,
                        secondKitsuLink,
                    )

                    override suspend fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                        Anime(
                            title = "Beck",
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

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = emptyList(),
                        animeListEntriesMultipleMappings = emptyMap(),
                        animeListMappings = emptyMap(),
                        watchListEntriesWithoutMapping = emptyList(),
                        watchListEntriesMultipleMappings = mapOf(
                            currentWatchListEntry to setOf(Link(firstKitsuLink), Link(secondKitsuLink))
                        ),
                        watchListMappings = emptyMap(),
                        ignoreListEntriesWithoutMapping = emptyList(),
                        ignoreListEntriesMultipleMappings = emptyMap(),
                        ignoreListMappings = emptyMap(),
                    )
                )
            }
        }

        @Test
        fun `IgnoreListEntry without mapping`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

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

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = emptyList(),
                        animeListEntriesMultipleMappings = emptyMap(),
                        animeListMappings = emptyMap(),
                        watchListEntriesWithoutMapping = emptyList(),
                        watchListEntriesMultipleMappings = emptyMap(),
                        watchListMappings = emptyMap(),
                        ignoreListEntriesWithoutMapping = listOf(currentIgnoreListEntry),
                        ignoreListEntriesMultipleMappings = emptyMap(),
                        ignoreListMappings = emptyMap(),
                    )
                )
            }
        }

        @Test
        fun `IgnoreListEntry having one on one mapping`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

                val kitsuLink = KitsuConfig.buildAnimeLink("38")

                val testCache = object: AnimeCache by TestAnimeCache {
                    override val availableMetaDataProvider: Set<Hostname>
                        get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                    override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                        kitsuLink
                    )

                    override suspend fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                        Anime(
                            title = "Beck",
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

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = emptyList(),
                        animeListEntriesMultipleMappings = emptyMap(),
                        animeListMappings = emptyMap(),
                        watchListEntriesWithoutMapping = emptyList(),
                        watchListEntriesMultipleMappings = emptyMap(),
                        watchListMappings = emptyMap(),
                        ignoreListEntriesWithoutMapping = emptyList(),
                        ignoreListEntriesMultipleMappings = emptyMap(),
                        ignoreListMappings = mapOf(
                            currentIgnoreListEntry to Link(kitsuLink)
                        ),
                    )
                )
            }
        }

        @Test
        fun `IgnoreListEntry having multiple mappings`() {
            runBlocking {
                // given
                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

                val firstKitsuLink = KitsuConfig.buildAnimeLink("38")
                val secondKitsuLink = KitsuConfig.buildAnimeLink("99999999")

                val testCache = object: AnimeCache by TestAnimeCache {
                    override val availableMetaDataProvider: Set<Hostname>
                        get() = setOf(MyanimelistConfig.hostname(), KitsuConfig.hostname())
                    override fun mapToMetaDataProvider(uri: URI, metaDataProvider: Hostname): Set<URI> = setOf(
                        firstKitsuLink,
                        secondKitsuLink,
                    )

                    override suspend fun fetch(key: URI): CacheEntry<Anime> = PresentValue(
                        Anime(
                            title = "Beck",
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

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = testCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = TestCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.checkMigration(MyanimelistConfig.hostname(), KitsuConfig.hostname())

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = emptyList(),
                        animeListEntriesMultipleMappings = emptyMap(),
                        animeListMappings = emptyMap(),
                        watchListEntriesWithoutMapping = emptyList(),
                        watchListEntriesMultipleMappings = emptyMap(),
                        watchListMappings = emptyMap(),
                        ignoreListEntriesWithoutMapping = emptyList(),
                        ignoreListEntriesMultipleMappings = mapOf(
                            currentIgnoreListEntry to setOf(Link(firstKitsuLink), Link(secondKitsuLink))
                        ),
                        ignoreListMappings = emptyMap(),
                    )
                )
            }
        }
    }

    @Nested
    inner class MigrateTests {

        @Test
        fun `correctly migrate entries`() {
            runBlocking {
                // given
                val animeListEntryWithMapping = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/1"),
                    title = "1",
                    episodes = 26,
                    type = TV,
                    location = Paths.get("."),
                )
                val animeListEntryWithoutMapping = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/2"),
                    title = "2",
                    episodes = 26,
                    type = TV,
                    location = Paths.get("."),
                )
                val animeListEntryWithMultipleMapping = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/3"),
                    title = "3",
                    episodes = 26,
                    type = TV,
                    location = Paths.get("."),
                )
                val watchListEntryWithMapping = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/4"),
                    title = "4",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )
                val watchListEntryWithoutMapping = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/5"),
                    title = "5",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )
                val watchListEntryWithMultipleMapping = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/6"),
                    title = "6",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )
                val ignoreListEntryWithMapping = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/7"),
                    title = "7",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )
                val ignoreListEntryWithoutMapping = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/8"),
                    title = "8",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )
                val ignoreListEntryWithMultipleMapping = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/9"),
                    title = "9",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )

                CoroutinesFlowEventBus.metaDataProviderMigrationState.update {
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = listOf(animeListEntryWithoutMapping),
                        animeListEntriesMultipleMappings = mapOf(animeListEntryWithMultipleMapping to setOf(Link("https://example.org/anime/3"), Link("https://othersite.com/anime/3"))),
                        animeListMappings = mapOf(animeListEntryWithMapping to Link("https://example.org/anime/1")),
                        watchListEntriesWithoutMapping = listOf(watchListEntryWithoutMapping),
                        watchListEntriesMultipleMappings = mapOf(watchListEntryWithMultipleMapping to setOf(Link("https://example.org/anime/5"), Link("https://othersite.com/anime/5"))),
                        watchListMappings = mapOf(watchListEntryWithMapping to Link("https://example.org/anime/4")),
                        ignoreListEntriesWithoutMapping = listOf(ignoreListEntryWithoutMapping),
                        ignoreListEntriesMultipleMappings = mapOf(ignoreListEntryWithMultipleMapping to setOf(Link("https://example.org/anime/9"), Link("https://othersite.com/anime/9"))),
                        ignoreListMappings = mapOf(ignoreListEntryWithMapping to Link("https://example.org/anime/7")),
                    )
                }

                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

                var addAllAnimeListEntriesInvoked = false
                var removeAnimeListEntryInvoked = false
                var addAllWatchListEntriesInvoked = false
                var removeWatchListEntryInvoked = false
                var addAllIgnoreListEntriesInvoked = false
                var removeIgnoreListEntryInvoked = false
                val testState = object: State by TestState {
                    override fun addAllAnimeListEntries(anime: Collection<AnimeListEntry>) {
                        addAllAnimeListEntriesInvoked = true
                    }
                    override fun removeAnimeListEntry(entry: AnimeListEntry) {
                        removeAnimeListEntryInvoked = true
                    }
                    override fun addAllWatchListEntries(anime: Collection<WatchListEntry>) {
                        addAllWatchListEntriesInvoked = true
                    }
                    override fun removeWatchListEntry(entry: WatchListEntry) {
                        removeWatchListEntryInvoked = true
                    }
                    override fun addAllIgnoreListEntries(anime: Collection<IgnoreListEntry>) {
                        addAllIgnoreListEntriesInvoked = true
                    }
                    override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
                        removeIgnoreListEntryInvoked = true
                    }
                    override fun createSnapshot(): Snapshot = object: Snapshot {
                        override fun animeList(): List<AnimeListEntry> = shouldNotBeInvoked()
                        override fun watchList(): Set<WatchListEntry> = shouldNotBeInvoked()
                        override fun ignoreList(): Set<IgnoreListEntry> = shouldNotBeInvoked()
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) {}
                }

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = TestAnimeCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = testCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.migrate(
                    animeListMappings = mapOf(animeListEntryWithMapping to Link("https://example.org/anime/1")),
                    watchListMappings = mapOf(watchListEntryWithMapping to Link("https://example.org/anime/4")),
                    ignoreListMappings = mapOf(ignoreListEntryWithMapping to Link("https://example.org/anime/7")),
                )

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assert(addAllAnimeListEntriesInvoked)
                assert(removeAnimeListEntryInvoked)
                assert(addAllWatchListEntriesInvoked)
                assert(removeWatchListEntryInvoked)
                assert(addAllIgnoreListEntriesInvoked)
                assert(removeIgnoreListEntryInvoked)
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = listOf(animeListEntryWithoutMapping),
                        animeListEntriesMultipleMappings = emptyMap(),
                        animeListMappings = emptyMap(),
                        watchListEntriesWithoutMapping = listOf(watchListEntryWithoutMapping),
                        watchListEntriesMultipleMappings = emptyMap(),
                        watchListMappings = emptyMap(),
                        ignoreListEntriesWithoutMapping = listOf(ignoreListEntryWithoutMapping),
                        ignoreListEntriesMultipleMappings = emptyMap(),
                        ignoreListMappings = emptyMap(),
                    )
                )
            }
        }
    }

    @Nested
    inner class RemoveUnmappedTests {

        @Test
        fun `correctly remove unmapped entries`() {
            runBlocking {
                // given
                val animeListEntryWithMapping = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/1"),
                    title = "1",
                    episodes = 26,
                    type = TV,
                    location = Paths.get("."),
                )
                val animeListEntryWithoutMapping = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/2"),
                    title = "2",
                    episodes = 26,
                    type = TV,
                    location = Paths.get("."),
                )
                val animeListEntryWithMultipleMapping = AnimeListEntry(
                    link = Link("https://myanimelist.net/anime/3"),
                    title = "3",
                    episodes = 26,
                    type = TV,
                    location = Paths.get("."),
                )
                val watchListEntryWithMapping = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/4"),
                    title = "4",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )
                val watchListEntryWithoutMapping = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/5"),
                    title = "5",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )
                val watchListEntryWithMultipleMapping = WatchListEntry(
                    link = Link("https://myanimelist.net/anime/6"),
                    title = "6",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )
                val ignoreListEntryWithMapping = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/7"),
                    title = "7",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )
                val ignoreListEntryWithoutMapping = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/8"),
                    title = "8",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )
                val ignoreListEntryWithMultipleMapping = IgnoreListEntry(
                    link = Link("https://myanimelist.net/anime/9"),
                    title = "9",
                    thumbnail = URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic.png"),
                )

                CoroutinesFlowEventBus.metaDataProviderMigrationState.update {
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = listOf(animeListEntryWithoutMapping),
                        animeListEntriesMultipleMappings = mapOf(animeListEntryWithMultipleMapping to setOf(Link("https://example.org/anime/3"), Link("https://othersite.com/anime/3"))),
                        animeListMappings = mapOf(animeListEntryWithMapping to Link("https://example.org/anime/1")),
                        watchListEntriesWithoutMapping = listOf(watchListEntryWithoutMapping),
                        watchListEntriesMultipleMappings = mapOf(watchListEntryWithMultipleMapping to setOf(Link("https://example.org/anime/5"), Link("https://othersite.com/anime/5"))),
                        watchListMappings = mapOf(watchListEntryWithMapping to Link("https://example.org/anime/4")),
                        ignoreListEntriesWithoutMapping = listOf(ignoreListEntryWithoutMapping),
                        ignoreListEntriesMultipleMappings = mapOf(ignoreListEntryWithMultipleMapping to setOf(Link("https://example.org/anime/9"), Link("https://othersite.com/anime/9"))),
                        ignoreListMappings = mapOf(ignoreListEntryWithMapping to Link("https://example.org/anime/7")),
                    )
                }

                val receivedEvents = mutableListOf<MetaDataProviderMigrationState>()
                val eventCollector = launch { CoroutinesFlowEventBus.metaDataProviderMigrationState.collect { event -> receivedEvents.add(event)} }
                delay(100)

                var removeAnimeListEntryInvoked = false
                var removeWatchListEntryInvoked = false
                var removeIgnoreListEntryInvoked = false
                val testState = object: State by TestState {
                    override fun removeAnimeListEntry(entry: AnimeListEntry) {
                        removeAnimeListEntryInvoked = true
                    }
                    override fun removeWatchListEntry(entry: WatchListEntry) {
                        removeWatchListEntryInvoked = true
                    }
                    override fun removeIgnoreListEntry(entry: IgnoreListEntry) {
                        removeIgnoreListEntryInvoked = true
                    }
                    override fun createSnapshot(): Snapshot = object: Snapshot {
                        override fun animeList(): List<AnimeListEntry> = shouldNotBeInvoked()
                        override fun watchList(): Set<WatchListEntry> = shouldNotBeInvoked()
                        override fun ignoreList(): Set<IgnoreListEntry> = shouldNotBeInvoked()
                    }
                }

                val testCommandHistory = object: CommandHistory by TestCommandHistory {
                    override fun push(command: ReversibleCommand) {}
                }

                val defaultMetaDataProviderMigrationHandler = DefaultMetaDataProviderMigrationHandler(
                    cache = TestAnimeCache,
                    eventBus = CoroutinesFlowEventBus,
                    commandHistory = testCommandHistory,
                    state = testState,
                )

                // when
                defaultMetaDataProviderMigrationHandler.removeUnmapped(
                    animeListEntriesWithoutMapping = listOf(animeListEntryWithoutMapping),
                    watchListEntriesWithoutMapping = listOf(watchListEntryWithoutMapping),
                    ignoreListEntriesWithoutMapping = listOf(ignoreListEntryWithoutMapping),
                )

                // then
                delay(100)
                eventCollector.cancelAndJoin()
                assert(removeAnimeListEntryInvoked)
                assert(removeWatchListEntryInvoked)
                assert(removeIgnoreListEntryInvoked)
                assertThat(receivedEvents).hasSize(3) // initial, start, result
                assertThat(receivedEvents.last()).isEqualTo(
                    MetaDataProviderMigrationState(
                        isRunning = false,
                        animeListEntriesWithoutMapping = emptyList(),
                        animeListEntriesMultipleMappings = mapOf(animeListEntryWithMultipleMapping to setOf(Link("https://example.org/anime/3"), Link("https://othersite.com/anime/3"))),
                        animeListMappings = mapOf(animeListEntryWithMapping to Link("https://example.org/anime/1")),
                        watchListEntriesWithoutMapping = emptyList(),
                        watchListEntriesMultipleMappings = mapOf(watchListEntryWithMultipleMapping to setOf(Link("https://example.org/anime/5"), Link("https://othersite.com/anime/5"))),
                        watchListMappings = mapOf(watchListEntryWithMapping to Link("https://example.org/anime/4")),
                        ignoreListEntriesWithoutMapping = emptyList(),
                        ignoreListEntriesMultipleMappings = mapOf(ignoreListEntryWithMultipleMapping to setOf(Link("https://example.org/anime/9"), Link("https://othersite.com/anime/9"))),
                        ignoreListMappings = mapOf(ignoreListEntryWithMapping to Link("https://example.org/anime/7")),
                    )
                )
            }
        }
    }

    @Nested
    inner class CompanionObjectTests {

        @Test
        fun `instance property always returns same instance`() {
            // given
            val previous = DefaultMetaDataProviderMigrationHandler.instance

            // when
            val result = DefaultMetaDataProviderMigrationHandler.instance

            // then
            assertThat(result).isExactlyInstanceOf(DefaultMetaDataProviderMigrationHandler::class.java)
            assertThat(result === previous).isTrue()
        }
    }
}