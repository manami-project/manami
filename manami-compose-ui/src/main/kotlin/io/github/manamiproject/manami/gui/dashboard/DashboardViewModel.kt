package io.github.manamiproject.manami.gui.dashboard

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.app.lists.Link
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.round

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

    val metaDataProviderNumberOfAnime: StateFlow<Map<String, String>>
        get() = combine(app.dashboardState, app.animeListState, app.watchListState, app.ignoreListState) {
            val animeListEntries = app.animeListState.value.entries
            val watchListEntries = app.watchListState.value.entries
            val ignoreListEntries = app.ignoreListState.value.entries

            app.dashboardState.value.entries
                .toList()
                .sortedByDescending { it.second }
                .toMap()
                .map { (key, value) ->
                    val numberOfAnimeListEntries = animeListEntries.filter { it.link is Link }.count { it.link.asLink().uri.host == key }
                    val numberOfWatchListEntries = watchListEntries.count { it.link.uri.host == key }
                    val numberOfIgnoreListEntries = ignoreListEntries.count { it.link.uri.host == key }
                    val sumOfEntries = numberOfAnimeListEntries + numberOfWatchListEntries + numberOfIgnoreListEntries
                    val percent = round(sumOfEntries.toDouble()/value.toDouble() * 100.00)
                    val detailed = "$value | Anime List: $numberOfAnimeListEntries | Watch List: $numberOfWatchListEntries | Ignore List: $numberOfIgnoreListEntries | $percent %"

                    key to if (sumOfEntries > 0) detailed else value.toString()
                }
                .toMap()

        }.stateIn(viewModelScope, Eagerly, emptyMap<String, String>())

            /*app.dashboardState
            .map { event -> event.entries.toList()
            .stateIn(
                scope = viewModelScope,
                started = Eagerly,
                initialValue = emptyMap(),
            )*/

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