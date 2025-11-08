package io.github.manamiproject.manami.gui.find.bytitle

import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun WatchListResults(viewModel: WatchListResultsViewModel = WatchListResultsViewModel.instance) {
    ManamiTheme {
        AnimeTable(viewModel = viewModel) {
            withToAnimeListButton = false
            withToWatchListButton = false
            withToIgnoreListButton = false
            withHideButton = false
            withSortableTitle = false
        }
    }
}