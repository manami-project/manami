package io.github.manamiproject.manami.gui.components.animetable

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
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
    val listState = rememberLazyListState()
    val entries by viewModel.entries.collectAsState()
    val animeTableConfig = AnimeTableConfig().apply { config() }
    viewModel.isSortable(animeTableConfig.withSortableTitle)

    ManamiTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(16.dp),
            ) {
                item {
                    AnimeTableHeaderRow(
                        config = config,
                        onSortRequested = { newDirection -> viewModel.sort(newDirection) },
                    )
                }
                items(items = entries, key = { it.link }) { entry ->
                    AnimeTableRow(
                        config = config,
                        anime = entry,
                        viewModel = viewModel,
                    )
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(listState),
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}