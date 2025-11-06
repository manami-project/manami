package io.github.manamiproject.manami.gui.lists.watchlist

import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.components.IconButton
import io.github.manamiproject.manami.gui.components.animetable.AnimeTable
import io.github.manamiproject.manami.gui.components.simpletable.SimpleTable
import io.github.manamiproject.manami.gui.extensions.toMap
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun WatchList(viewModel: WatchListViewModel = WatchListViewModel.instance) {
    val animeDetails = viewModel.animeDetails.collectAsState()
    val showAnimeDetails = viewModel.showAnimeDetails.collectAsState()
    val isAnimeDetailsRunning = viewModel.isAnimeDetailsRunning.collectAsState()

    ManamiTheme {
        if (!showAnimeDetails.value) {
            AnimeTable(viewModel = viewModel) {
                withToWatchListButton = false
                withHideButton = false
                withDeleteButton = true
            }
        } else {
            IconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                size = 40.dp,
                description = "Back to WatchList",
                onClick = { viewModel.hideAnimeDetails() },
            )
            if (isAnimeDetailsRunning.value) {
                Text("Loading")
            } else {
                SimpleTable(animeDetails.value?.toMap() ?: emptyMap())
            }
        }
    }
}