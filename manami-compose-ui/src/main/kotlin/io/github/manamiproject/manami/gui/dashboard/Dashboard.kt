package io.github.manamiproject.manami.gui.dashboard

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun Dashboard(viewModel: DashboardViewModel = DashboardViewModel.instance) {
    ManamiTheme {
        Text("Dashboard")
    }
}