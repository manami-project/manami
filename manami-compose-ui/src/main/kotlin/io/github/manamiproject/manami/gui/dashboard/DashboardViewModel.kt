package io.github.manamiproject.manami.gui.dashboard

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.gui.tabs.TabBarViewModel
import io.github.manamiproject.manami.gui.tabs.Tabs.FIND_BY_TITLE
import io.github.manamiproject.modb.core.anime.Title
import io.github.manamiproject.modb.core.config.Hostname
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.eitherNullOrBlank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class DashboardViewModel(
    private val app: Manami = Manami.instance,
    private val tabBarViewModel: TabBarViewModel = TabBarViewModel.instance,
) {

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

    val metaDataProviders: StateFlow<List<Hostname>>
        get() = app.dashboardState
            .map { event -> event.entries.toList().sortedByDescending { it.second }.map { it.first } }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyList(),
            )

    val newVersion: StateFlow<String>
        get() = app.dashboardState
            .map { event -> event.newVersion?.version ?: EMPTY }
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = EMPTY,
            )

    fun findByTitle(metaDataProvider: Hostname, title: Title) {
        if (title.eitherNullOrBlank()) return

        viewModelScope.launch {
            app.findByTitle(metaDataProvider, title.trim())
        }

        tabBarViewModel.openOrActivate(FIND_BY_TITLE)
    }

    internal companion object {
        /**
         * Singleton of [DashboardViewModel]
         * @since 4.0.0
         */
        val instance: DashboardViewModel by lazy { DashboardViewModel() }
    }
}