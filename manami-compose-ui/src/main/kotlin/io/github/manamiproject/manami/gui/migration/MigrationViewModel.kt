package io.github.manamiproject.manami.gui.migration

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class MigrationViewModel(private val app: Manami = Manami.instance) {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

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

    fun start() {
        if (metaDataProviderToText.neitherNullNorBlank() && metaDataProviderFromText.neitherNullNorBlank()) {
            viewModelScope.launch {
                app.checkMigration(metaDataProviderFromText, metaDataProviderToText)
            }
        }
    }

    fun migrate() {
        viewModelScope.launch {
            // TODO: merge with manual selections
            app.removeUnmapped(
                animeListEntriesWithoutMapping = emptyList(),
                watchListEntriesWithoutMapping = app.metaDataProviderMigrationState.value.watchListEntriesWithoutMapping,
                ignoreListEntriesWithoutMapping = app.metaDataProviderMigrationState.value.ignoreListEntriesWithoutMapping,
            )
            app.migrate(
                animeListMappings = app.metaDataProviderMigrationState.value.animeListMappings,
                watchListMappings = app.metaDataProviderMigrationState.value.watchListMappings,
                ignoreListMappings = app.metaDataProviderMigrationState.value.ignoreListMappings,
            )
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