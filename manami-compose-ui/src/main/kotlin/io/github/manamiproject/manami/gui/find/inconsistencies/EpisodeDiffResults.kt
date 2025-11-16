package io.github.manamiproject.manami.gui.find.inconsistencies

import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun EpisodeDiffResults(viewModel: EpisodeDiffResultsViewModel = EpisodeDiffResultsViewModel.instance) {
    ManamiTheme {
        AnimeTable(viewModel = viewModel) {
            withOpenDirectoryButton = true
            withEditButton = false
            withShowAnimeDetailsButton = false
            withFindRelatedAnimeButton = false
            withFindSimilarAnimeButton = false
            withToAnimeListButton = false
            withToWatchListButton = false
            withToIgnoreListButton = false
            withHideButton = true
            withDeleteButton = false
            withSortableTitle = false
        }
    }
}