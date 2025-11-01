package io.github.manamiproject.manami.gui.tabs

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.github.manamiproject.manami.gui.tabs.Tabs.DASHBOARD

internal class TabBarViewModel {
    val openTabs = mutableStateListOf(DASHBOARD)
    var activeTab = mutableStateOf(DASHBOARD)

    fun openOrActivate(tab: Tabs) {
        if (!openTabs.contains(tab)) {
            openTabs.add(tab)
        }
        activeTab.value = tab
    }

    fun closeTab(tab: Tabs) {
        if (openTabs.contains(tab)) {
            if (tab == activeTab.value) {
                val index = (openTabs.indexOf(tab) - 1).coerceAtLeast(0)
                activeTab.value = openTabs[index]
            }
            openTabs.remove(tab)
        }
    }

    internal companion object {
        /**
         * Singleton of [TabBarViewModel]
         * @since 4.0.0
         */
        val instance: TabBarViewModel by lazy { TabBarViewModel() }
    }
}