package io.github.manamiproject.manami.gui.find.inconsistencies

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun FindInconsistencies(viewModel: FindInconsistenciesViewModel = FindInconsistenciesViewModel.instance) {
    ManamiTheme {
        Text("FindInconsistencies")
    }
}