package io.github.manamiproject.manami.gui.components.simpletable

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun SimpleTable(
    viewModel: DefaultSimpleTableViewModel = DefaultSimpleTableViewModel.instance,
    data: Map<String, Any>,
    config: SimpleTableConfig.() -> Unit = {},
) {
    val dataList = data.toList()

    LaunchedEffect(dataList) {
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
                modifier = Modifier.padding(16.dp),
            ) {
                item {
                    SimpleTableHeaderRow(
                        config = config,
                    )
                }
                items(items = dataList, key = { it.first }) { entry ->
                    SimpleTableRow(
                        config = config,
                        data = entry,
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