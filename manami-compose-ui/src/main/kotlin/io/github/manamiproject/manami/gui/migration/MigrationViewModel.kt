package io.github.manamiproject.manami.gui.migration

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.launch

internal class MigrationViewModel(private val app: Manami = Manami.instance) {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    private var lastIndex = 0
    private var lastOffset = 0
    val listState = LazyListState()

    var metaDataProviderFromText by mutableStateOf(EMPTY)
    var metaDataProviderToText by mutableStateOf(EMPTY)

    val isRunning: StateFlow<Boolean>
        get() = app.metaDataProviderMigrationState
            .map { it.isRunning }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = false,
            )

    val metaDataProviders: StateFlow<List<Hostname>>
        get() = app.dashboardState
            .map { event -> event.entries.toList().sortedByDescending { it.second }.map { it.first } }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    val containsResults: StateFlow<Boolean>
        get() = app.metaDataProviderMigrationState
            .map { event ->
                listOf(
                    event.animeListMappings.isNotEmpty(),
                    event.animeListEntriesMultipleMappings.isNotEmpty(),
                    event.animeListEntriesWithoutMapping.isNotEmpty(),
                    event.watchListMappings.isNotEmpty(),
                    event.watchListEntriesWithoutMapping.isNotEmpty(),
                    event.watchListEntriesMultipleMappings.isNotEmpty(),
                    event.ignoreListMappings.isNotEmpty(),
                    event.ignoreListEntriesWithoutMapping.isNotEmpty(),
                    event.ignoreListEntriesMultipleMappings.isNotEmpty(),
                ).any { it }
            }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = false,
            )

    private val _manualSelections = MutableStateFlow<Map<AnimeEntry, Link>>(emptyMap())
    val manualSelections: StateFlow<Map<AnimeEntry, Link>> = _manualSelections

    val entries: StateFlow<List<MigrationSelectionEntry>>
        get() = app.metaDataProviderMigrationState
            .map { event ->
                val animeList = event.animeListEntriesMultipleMappings.map { (key, value) ->
                    MigrationSelectionEntry(
                        animeEntry = key,
                        possibleMappings = value
                    )
                }
                val watchList = event.watchListEntriesMultipleMappings.map { (key, value) ->
                    MigrationSelectionEntry(
                        animeEntry = key,
                        possibleMappings = value
                    )
                }
                val ignoreList = event.ignoreListEntriesMultipleMappings.map { (key, value) ->
                    MigrationSelectionEntry(
                        animeEntry = key,
                        possibleMappings = value
                    )
                }
                animeList.union(watchList).union(ignoreList).toList()
            }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    fun start() {
        if (metaDataProviderToText.neitherNullNorBlank() && metaDataProviderFromText.neitherNullNorBlank()) {
            viewModelScope.launch {
                app.checkMigration(metaDataProviderFromText, metaDataProviderToText)
            }
        }
    }

    fun migrate() {
        viewModelScope.launch {
            app.removeUnmapped(
                animeListEntriesWithoutMapping = emptyList(),
                watchListEntriesWithoutMapping = app.metaDataProviderMigrationState.value.watchListEntriesWithoutMapping,
                ignoreListEntriesWithoutMapping = app.metaDataProviderMigrationState.value.ignoreListEntriesWithoutMapping,
            )
            app.migrate(
                animeListMappings = app.metaDataProviderMigrationState.value.animeListMappings + _manualSelections.value.filter { (key, _) -> key is AnimeListEntry }.map { (key, value) -> key as AnimeListEntry to  value},
                watchListMappings = app.metaDataProviderMigrationState.value.watchListMappings + _manualSelections.value.filter { (key, _) -> key is WatchListEntry }.map { (key, value) -> key as WatchListEntry to  value},
                ignoreListMappings = app.metaDataProviderMigrationState.value.ignoreListMappings + _manualSelections.value.filter { (key, _) -> key is IgnoreListEntry }.map { (key, value) -> key as IgnoreListEntry to  value},
            )
        }
    }

    fun saveScrollPosition() {
        lastIndex = listState.firstVisibleItemIndex
        lastOffset = listState.firstVisibleItemScrollOffset
    }

    suspend fun restoreScrollPosition() {
        listState.scrollToItem(lastIndex, lastOffset)
    }

    fun selectMapping(entry: AnimeEntry, link: Link) {
        _manualSelections.update { current ->
            current + (entry to link)
        }
    }

    internal companion object {
        /**
         * Singleton of [MigrationViewModel]
         * @since 4.0.0
         */
        val instance: MigrationViewModel by lazy { MigrationViewModel() }
    }
}

internal data class MigrationSelectionEntry(
    val animeEntry: AnimeEntry,
    val possibleMappings: Set<Link>,
)