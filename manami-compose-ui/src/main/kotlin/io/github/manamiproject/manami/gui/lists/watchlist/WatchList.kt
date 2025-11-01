package io.github.manamiproject.manami.gui.lists.watchlist

import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun WatchList(viewModel: WatchListViewModel = WatchListViewModel.instance) {
    ManamiTheme {
        AnimeTable(viewModel = viewModel) {
            withToWatchListButton = false
            withHideButton = false
            withDeleteButton = true
        }
    }
}