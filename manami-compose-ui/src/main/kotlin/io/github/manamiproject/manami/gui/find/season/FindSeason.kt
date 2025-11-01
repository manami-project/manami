package io.github.manamiproject.manami.gui.find.season

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun FindSeason(viewModel: FindSeasonViewModel = FindSeasonViewModel.instance) {
    ManamiTheme {
        Text("FindSeason")
    }
}