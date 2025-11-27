package io.github.manamiproject.manami.gui.components.animetable

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
    val animeTableConfig = AnimeTableConfig().apply { config() }
    viewModel.isSortable(animeTableConfig.withSortableTitle)

    LaunchedEffect(entries) {
        viewModel.restoreScrollPosition()
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.saveScrollPosition()
        }
    }

    ManamiTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = viewModel.listState,
                modifier = Modifier.fillMaxSize().padding(16.dp),
            ) {
                item {
                    AnimeTableHeaderRow(
                        config = config,
                        onSortRequested = { newDirection -> viewModel.sort(newDirection) },
                    )
                }
                // TODO 4.1.0 Change key back to `items(items = entries, key = { it.link }) { entry ->`
                items(items = entries, key = { "${it.link}-${it.title}" }) { entry ->
                    AnimeTableRow(
                        config = config,
                        anime = entry,
                        viewModel = viewModel,
                    )
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(viewModel.listState),
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}