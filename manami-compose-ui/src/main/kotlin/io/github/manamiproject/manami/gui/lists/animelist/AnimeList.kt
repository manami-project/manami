package io.github.manamiproject.manami.gui.lists.animelist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.manamiproject.manami.gui.components.RotatingDotsProgress
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun AnimeList(viewModel: AnimeListViewModel = AnimeListViewModel.instance) {
    val isFileOpeningRunning = viewModel.isFileOpeningRunning.collectAsState()

    ManamiTheme {
        if (isFileOpeningRunning.value) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.align(Alignment.Center)) {
                    RotatingDotsProgress()
                }
            }
        } else {
            AnimeTable(viewModel = viewModel) {
                withOpenDirectoryButton = true
                withToWatchListButton = false
                withToIgnoreListButton = false
                withHideButton = false
                withEditButton = true
                withDeleteButton = true
            }
        }
    }
}