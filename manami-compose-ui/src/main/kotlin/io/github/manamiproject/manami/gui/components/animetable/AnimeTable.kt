package io.github.manamiproject.manami.gui.components.animetable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun <T : AnimeEntry> AnimeTable(
    viewModel: AnimeTableViewModel<T>,
    config: AnimeTableConfig.() -> Unit = {},
) {
    val entries by viewModel.entries.collectAsState()

    ManamiTheme {
        LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
            item {
                HeaderTableRow(
                    config = config,
                    onSortRequested = { newDirection -> viewModel.sort(newDirection) },
                )
            }
            items(entries) { entry ->
                AnimeTableRow(
                    config = config,
                    anime = entry,
                    viewModel = viewModel,
                )
            }
        }
    }
}