package io.github.manamiproject.manami.gui.find.season

import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun FindSeason(viewModel: FindSeasonViewModel = FindSeasonViewModel.instance) {
    ManamiTheme {
        AnimeTable(viewModel = viewModel)
    }
}