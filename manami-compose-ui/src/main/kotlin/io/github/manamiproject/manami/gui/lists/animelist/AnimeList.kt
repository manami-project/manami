package io.github.manamiproject.manami.gui.lists.animelist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun AnimeList(viewModel: AnimeListViewModel = AnimeListViewModel.instance) {
    ManamiTheme {
        Text("AnimeList")
    }
}