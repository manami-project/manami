package io.github.manamiproject.manami.gui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.manamiproject.manami.gui.components.RotatingDotsProgress
import io.github.manamiproject.manami.gui.components.simpletable.SimpleTable
import io.github.manamiproject.manami.gui.extensions.toOnClick
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import java.net.URI

@Composable
internal fun Dashboard(viewModel: DashboardViewModel = DashboardViewModel.instance) {
    val isLoading = viewModel.isLoading.collectAsState()
    val data = viewModel.metaDataProviderNumberOfAnime.collectAsState()
    val newVersion = viewModel.newVersion.collectAsState()

    ManamiTheme {
        if (newVersion.value.neitherNullNorBlank()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = URI("https://github.com/manami-project/manami/releases/tag/${newVersion.value}").toOnClick()) {
                    Text("\uD83D\uDE80 ${newVersion.value} available")
                }
            }
        }

        if (isLoading.value) {
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.align(Alignment.Center)) {
                    RotatingDotsProgress()
                }
            }
        } else {
            SimpleTable(data.value) {
                keyHeadline = "MetaDataProvider"
                valueHeadline = "Number of anime"
            }
        }
    }
}