package io.github.manamiproject.manami.gui.lists.ignorelist

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun IgnoreList(viewModel: IgnoreListViewModel = IgnoreListViewModel.instance) {
    ManamiTheme {
        Text("IgnoreList")
    }
}