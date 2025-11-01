package io.github.manamiproject.manami.gui.lists.ignorelist

import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun IgnoreList(viewModel: IgnoreListViewModel = IgnoreListViewModel.instance) {
    ManamiTheme {
        AnimeTable(viewModel = viewModel) {
            withToWatchListButton = false
            withToIgnoreListButton = false
            withHideButton = false
            withDeleteButton = true
        }
    }
}