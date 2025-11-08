package io.github.manamiproject.manami.gui.find.bytitle

import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun UnlistedResults(viewModel: UnlistedResultsViewModel = UnlistedResultsViewModel.instance) {
    ManamiTheme {
        AnimeTable(viewModel = viewModel) {
            withToAnimeListButton = true
            withToWatchListButton = true
            withToIgnoreListButton = true
            withHideButton = false
            withSortableTitle = false
        }
    }
}