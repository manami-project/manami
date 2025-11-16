package io.github.manamiproject.manami.gui.find.inconsistencies

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.components.IconButton
import io.github.manamiproject.manami.gui.components.RotatingDotsProgress
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun FindInconsistencies(viewModel: FindInconsistenciesViewModel = FindInconsistenciesViewModel.instance) {
    val isRunning = viewModel.isRunning.collectAsState()
    val numberOfMetaDataDiffs = viewModel.numberOfMetaDataDiffs.collectAsState()
    val numberOfEpisodesDiffs = viewModel.numberOfEpisodesDiffs.collectAsState()
    val numberOfDeadEntries = viewModel.numberOfDeadEntries.collectAsState()

    LaunchedEffect(true) {
        viewModel.findInconsistencies()
    }

    ManamiTheme {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isRunning.value) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.align(Alignment.Center)) {
                        RotatingDotsProgress()
                    }
                }
            } else {
                IconButton(
                    icon = Icons.Filled.Refresh,
                    size = 40.dp,
                    description = "Find inconsistencies",
                    onClick = { viewModel.findInconsistencies() },
                )

                if (numberOfMetaDataDiffs.value > 0) {
                    Button(
                        onClick = { viewModel.showMetaDataDiffResults() },
                        modifier = Modifier.padding(5.dp, 0.dp, 5.dp, 0.dp)
                    ) {
                        Text("Meta data diffs (${numberOfMetaDataDiffs.value})")
                    }
                }

                if (numberOfEpisodesDiffs.value > 0) {
                    Button(
                        onClick = { viewModel.showEpisodesDiffResults() },
                        modifier = Modifier.padding(0.dp, 0.dp, 5.dp, 0.dp)
                    ) {
                        Text("Episode diffs (${numberOfEpisodesDiffs.value})")
                    }
                }

                if (numberOfDeadEntries.value > 0) {
                    Button(
                        onClick = { viewModel.showDeadEntryResults() },
                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp)
                    ) {
                        Text("Dead entries (${numberOfDeadEntries.value})")
                    }
                }
            }
        }

        if (viewModel.isShowMetaDataDiffResults.value) {
            MetaDataDiffResults()
        }

        if (viewModel.isShowEpisodeDiffResults.value) {
            EpisodeDiffResults()
        }

        if (viewModel.isShowDeadEntryResults.value) {
            DeadEntryResults()
        }
    }
}