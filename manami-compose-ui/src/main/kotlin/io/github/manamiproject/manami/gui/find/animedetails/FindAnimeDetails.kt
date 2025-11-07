package io.github.manamiproject.manami.gui.find.animedetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.manamiproject.manami.gui.components.RotatingDotsProgress
import io.github.manamiproject.manami.gui.components.simpletable.SimpleTable
import io.github.manamiproject.manami.gui.extensions.toMap

@Composable
internal fun FindAnimeDetails(viewModel: FindAnimeDetailsViewModel = FindAnimeDetailsViewModel.instance) {
    val animeDetails = viewModel.animeDetails.collectAsState()
    val isAnimeDetailsRunning = viewModel.isAnimeDetailsRunning.collectAsState()

    if (isAnimeDetailsRunning.value) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.align(Alignment.Center)) {
                RotatingDotsProgress()
            }
        }
    } else {
        SimpleTable(animeDetails.value?.toMap() ?: emptyMap())
    }
}