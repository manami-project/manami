package io.github.manamiproject.manami.gui.find.bytitle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun FindByTitle(viewModel: FindByTitleViewModel = FindByTitleViewModel.instance) {
    val numberOfAnimeListResults = viewModel.numberOfAnimeListResults.collectAsState()
    val numberOfWatchListResults = viewModel.numberOfWatchListResults.collectAsState()
    val numberOfIgnoreListResults = viewModel.numberOfIgnoreListResults.collectAsState()
    val numberOfUnlistedResults = viewModel.numberOfUnlistedResults.collectAsState()

    ManamiTheme {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (numberOfAnimeListResults.value > 0) {
                Button(
                    onClick = { viewModel.showAnimeListResults() },
                    modifier = Modifier.padding(0.dp, 0.dp, 5.dp, 0.dp)
                ) {
                    Text("AnimeList (${numberOfAnimeListResults.value})")
                }
            }

            if (numberOfWatchListResults.value > 0) {
                Button(
                    onClick = { viewModel.showWatchListResults() },
                    modifier = Modifier.padding(0.dp, 0.dp, 5.dp, 0.dp)
                ) {
                    Text("WatchList (${numberOfWatchListResults.value})")
                }
            }

            if (numberOfIgnoreListResults.value > 0) {
                Button(
                    onClick = { viewModel.showIgnoreListResults() },
                    modifier = Modifier.padding(0.dp, 0.dp, 5.dp, 0.dp)
                ) {
                    Text("IgnoreList (${numberOfIgnoreListResults.value})")
                }
            }

            if (numberOfUnlistedResults.value > 0) {
                Button(
                    onClick = { viewModel.showUnlistResults() },
                    modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    Text("Unlisted (${numberOfUnlistedResults.value})")
                }
            }
        }
        if (viewModel.isShowAnimeListResults.value) {
            AnimeListResults()
        }

        if (viewModel.isShowWatchListResults.value) {
            WatchListResults()
        }

        if (viewModel.isShowIgnoreListResults.value) {
            IgnoreListResults()
        }

        if (viewModel.isShowUnlistResults.value) {
            UnlistedResults()
        }
    }
}