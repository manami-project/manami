package io.github.manamiproject.manami.gui.components.simpletable

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.theme.ManamiTheme

@Composable
internal fun SimpleTable(
    data: Map<String, Any>,
    config: SimpleTableConfig.() -> Unit = {},
) {
    val dataList = data.toList()

    ManamiTheme {
        LazyColumn(Modifier.padding(16.dp)) {
            item {
                SimpleTableHeaderRow(
                    config = config,
                )
            }
            items(dataList.size) { index ->
                SimpleTableRow(
                    config = config,
                    data = dataList[index]
                )
            }
        }
    }
}