package io.github.manamiproject.manami.gui.dashboard

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import io.github.manamiproject.manami.gui.components.simpletable.SimpleTable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun Dashboard(viewModel: DashboardViewModel = DashboardViewModel.instance) {

    val data = viewModel.metaDataProviderNumberOfAnime.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()

    ManamiTheme {
        if (isLoading.value) {
            Text("Initializing")
        } else {
            SimpleTable(data.value) {
                keyHeadline = "MetaDataProvider"
                valueHeadline = "Number of anime"
            }
        }
    }
}