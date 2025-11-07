package io.github.manamiproject.manami.gui.find.relatedanime

import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun FindRelatedAnime(viewModel: FindRelatedAnimeViewModel = FindRelatedAnimeViewModel.instance) {
    ManamiTheme {
        AnimeTable(viewModel = viewModel)
    }
}