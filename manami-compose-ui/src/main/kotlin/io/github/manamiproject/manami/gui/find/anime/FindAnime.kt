package io.github.manamiproject.manami.gui.find.anime

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun FindAnime(viewModel: FindAnimeViewModel = FindAnimeViewModel.instance) {
    ManamiTheme {
        Text("FindAnime")
    }
}