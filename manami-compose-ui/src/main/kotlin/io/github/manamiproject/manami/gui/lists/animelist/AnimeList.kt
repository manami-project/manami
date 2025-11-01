package io.github.manamiproject.manami.gui.lists.animelist

import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun AnimeList(viewModel: AnimeListViewModel = AnimeListViewModel.instance) {
    ManamiTheme {
        AnimeTable(viewModel = viewModel) {
            withToWatchListButton = false
            withToIgnoreListButton = false
            withHideButton = false
            withEditButton = true
            withDeleteButton = true
        }
    }
}