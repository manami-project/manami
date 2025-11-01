package io.github.manamiproject.manami.gui.find.relatedanime

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun FindRelatedAnime(viewModel: FindRelatedAnimeViewModel = FindRelatedAnimeViewModel.instance) {
    ManamiTheme {
        Text("FindRelatedAnime")
    }
}