package io.github.manamiproject.manami.gui.components.simpletable

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun SimpleTable(
    data: Map<String, Any>,
    config: SimpleTableConfig.() -> Unit = {},
) {
    val listState = rememberLazyListState()
    val dataList = data.toList()

    ManamiTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(Modifier.padding(16.dp)) {
                item {
                    SimpleTableHeaderRow(
                        config = config,
                    )
                }
                items(items = dataList, key = { it.first }) { entry ->
                    SimpleTableRow(
                        config = config,
                        data = entry
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