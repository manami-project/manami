package io.github.manamiproject.manami.gui.lists.watchlist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun WatchList(viewModel: WatchListViewModel = WatchListViewModel.instance) {
    ManamiTheme {
        Text("WatchList")
    }
}