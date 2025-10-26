package io.github.manamiproject.manami.app.migration

import io.github.manamiproject.manami.app.cache.AnimeCache
import io.github.manamiproject.manami.app.cache.DefaultAnimeCache
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.commands.GenericReversibleCommand
import io.github.manamiproject.manami.app.commands.history.CommandHistory
import io.github.manamiproject.manami.app.commands.history.DefaultCommandHistory
import io.github.manamiproject.manami.app.events.CoroutinesFlowEventBus
import io.github.manamiproject.manami.app.events.EventBus
import io.github.manamiproject.manami.app.events.MetaDataProviderMigrationState
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.app.state.InternalState
import io.github.manamiproject.manami.app.state.State
import io.github.manamiproject.modb.core.anime.Anime
import io.github.manamiproject.modb.core.config.Hostname
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.yield

internal class DefaultMetaDataMigrationHandler(
    private val cache: AnimeCache = DefaultAnimeCache.instance,
    private val eventBus: EventBus = CoroutinesFlowEventBus,
    private val commandHistory: CommandHistory = DefaultCommandHistory,
    private val state: State = InternalState,
) : MetaDataMigrationHandler {

    override suspend fun checkMigration(metaDataProviderFrom: Hostname, metaDataProviderTo: Hostname) {
        require(cache.availableMetaDataProvider.contains(metaDataProviderFrom)) { "MetaDataProvider [$metaDataProviderFrom] is not supported." }
        require(cache.availableMetaDataProvider.contains(metaDataProviderTo)) { "MetaDataProvider [$metaDataProviderTo] is not supported." }

        eventBus.metaDataProviderMigrationState.update { MetaDataProviderMigrationState(isRunning = true); }
        yield()

        val animeList = state.animeList().filter { it.link is Link }.filter { it.link.asLink().uri.host == metaDataProviderFrom }
        val watchList = state.watchList().filter { it.link.asLink().uri.host == metaDataProviderFrom }
        val ignoreList = state.ignoreList().filter { it.link.asLink().uri.host == metaDataProviderFrom }

        val animeListEntriesWithoutMapping = mutableListOf<AnimeListEntry>()
        val animeListEntriesMultipleMappings = mutableMapOf<AnimeListEntry, Set<Link>>()
        val animeListMappings = mutableMapOf<AnimeListEntry, Link>()

        animeList.forEach { animeListEntry ->
            val newMetaDataProviderLinks = cache.mapToMetaDataProvider(animeListEntry.link.asLink().uri, metaDataProviderTo)

            when {
                newMetaDataProviderLinks.isEmpty() -> animeListEntriesWithoutMapping.add(animeListEntry)
                newMetaDataProviderLinks.size > 1 -> {
                    val cacheEntries = newMetaDataProviderLinks.map { cache.fetch(it) as PresentValue<Anime>}.map { it.value }.flatMap { it.sources }.map { Link(it) }
                    animeListEntriesMultipleMappings[animeListEntry] = cacheEntries.toSet()
                }
                else -> {
                    val cacheEntry = cache.fetch(newMetaDataProviderLinks.first()) as PresentValue<Anime>
                    animeListMappings[animeListEntry] = Link(cacheEntry.value.sources.first())
                }
            }
        }

        val watchListEntriesWithoutMapping = mutableListOf<WatchListEntry>()
        val watchListEntriesMultipleMappings = mutableMapOf<WatchListEntry, Set<Link>>()
        val watchListMappings = mutableMapOf<WatchListEntry, Link>()

        watchList.forEach { watchListEntry ->
            val newMetaDataProviderLinks = cache.mapToMetaDataProvider(watchListEntry.link.asLink().uri, metaDataProviderTo)

            when {
                newMetaDataProviderLinks.isEmpty() -> watchListEntriesWithoutMapping.add(watchListEntry)
                newMetaDataProviderLinks.size > 1 -> {
                    val cacheEntries = newMetaDataProviderLinks.map { cache.fetch(it) as PresentValue<Anime>}.map { it.value }.flatMap { it.sources }.map { Link(it) }
                    watchListEntriesMultipleMappings[watchListEntry] = cacheEntries.toSet()
                }
                else -> {
                    val cacheEntry = cache.fetch(newMetaDataProviderLinks.first()) as PresentValue<Anime>
                    watchListMappings[watchListEntry] = Link(cacheEntry.value.sources.first())
                }
            }
        }

        val ignoreListEntriesWithoutMapping = mutableListOf<IgnoreListEntry>()
        val ignoreListEntriesMultipleMappings = mutableMapOf<IgnoreListEntry, Set<Link>>()
        val ignoreListMappings = mutableMapOf<IgnoreListEntry, Link>()

        ignoreList.forEach { ignoreListEntry ->
            val newMetaDataProviderLinks = cache.mapToMetaDataProvider(ignoreListEntry.link.asLink().uri, metaDataProviderTo)

            when {
                newMetaDataProviderLinks.isEmpty() -> ignoreListEntriesWithoutMapping.add(ignoreListEntry)
                newMetaDataProviderLinks.size > 1 -> {
                    val cacheEntries = newMetaDataProviderLinks.map { cache.fetch(it) as PresentValue<Anime>}.map { it.value }.flatMap { it.sources }.map { Link(it) }
                    ignoreListEntriesMultipleMappings[ignoreListEntry] = cacheEntries.toSet()
                }
                else -> {
                    val cacheEntry = cache.fetch(newMetaDataProviderLinks.first()) as PresentValue<Anime>
                    ignoreListMappings[ignoreListEntry] = Link(cacheEntry.value.sources.first())
                }
            }
        }

        eventBus.metaDataProviderMigrationState.update { current ->
            current.copy(
                isRunning = false,
                animeListEntriesWithoutMapping = animeListEntriesWithoutMapping,
                animeListEntriesMultipleMappings = animeListEntriesMultipleMappings,
                animeListMappings = animeListMappings,
                watchListEntriesWithoutMapping = watchListEntriesWithoutMapping,
                watchListEntriesMultipleMappings = watchListEntriesMultipleMappings,
                watchListMappings = watchListMappings,
                ignoreListEntriesWithoutMapping = ignoreListEntriesWithoutMapping,
                ignoreListEntriesMultipleMappings = ignoreListEntriesMultipleMappings,
                ignoreListMappings = ignoreListMappings,
            )
        }
    }

    override fun migrate(
        animeListMappings: Map<AnimeListEntry, Link>,
        watchListMappings: Map<WatchListEntry, Link>,
        ignoreListMappings: Map<IgnoreListEntry, Link>,
    ) {
        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdMigrateEntries(
                animeListMappings = animeListMappings,
                watchListMappings = watchListMappings,
                ignoreListMappings = ignoreListMappings,
            )
        ).execute()
    }

    override fun removeUnmapped(
        animeListEntriesWithoutMapping: Collection<AnimeListEntry>,
        watchListEntriesWithoutMapping: Collection<WatchListEntry>,
        ignoreListEntriesWithoutMapping: Collection<IgnoreListEntry>,
    ) {
        GenericReversibleCommand(
            state = state,
            commandHistory = commandHistory,
            command = CmdRemoveUnmappedMigrationEntries(
                animeListEntriesWithoutMapping = animeListEntriesWithoutMapping,
                watchListEntriesWithoutMapping = watchListEntriesWithoutMapping,
                ignoreListEntriesWithoutMapping = ignoreListEntriesWithoutMapping,
            )
        ).execute()
    }

    companion object {
        /**
         * Singleton of [DefaultMetaDataMigrationHandler]
         * @since 4.0.0
         */
        val instance: DefaultMetaDataMigrationHandler by lazy { DefaultMetaDataMigrationHandler() }
    }
}