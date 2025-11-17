package io.github.manamiproject.manami.gui.find.bycriteria

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun FindByCriteria(viewModel: FindByCriteriaViewModel = FindByCriteriaViewModel.instance) {
    ManamiTheme {
        Text("FindByCriteria")
    }
}