package io.github.manamiproject.manami.gui.dashboard

import io.github.manamiproject.manami.app.Manami
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class DashboardViewModel(private val app: Manami = Manami.instance) {

    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    val isLoading: StateFlow<Boolean>
        get() = app.dashboardState
            .map { it.isAnimeCachePopulatorRunning }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = true,
            )

    val metaDataProviderNumberOfAnime: StateFlow<Map<String, Int>>
        get() = app.dashboardState
            .map { event -> event.entries.toList().sortedByDescending { it.second }.toMap() }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyMap(),
            )

    internal companion object {
        /**
         * Singleton of [DashboardViewModel]
         * @since 4.0.0
         */
        val instance: DashboardViewModel by lazy { DashboardViewModel() }
    }
}