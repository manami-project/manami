package io.github.manamiproject.manami.gui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
        if (isLoading.value) {
            Text("Loading")
        } else {
            if (newVersion.value.neitherNullNorBlank()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(onClick = URI("https://github.com/manami-project/manami/releases/tag/${newVersion.value}").toOnClick()) {
                        Text("\uD83D\uDE80 ${newVersion.value} available")
                    }
                }
            }

            SimpleTable(data.value) {
                keyHeadline = "MetaDataProvider"
                valueHeadline = "Number of anime"
            }
        }
    }
}