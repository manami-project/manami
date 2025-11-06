package io.github.manamiproject.manami.gui.components.animetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.components.IconButton
import io.github.manamiproject.manami.gui.components.animetable.AnimeTableSortDirection.ASC
import io.github.manamiproject.manami.gui.components.animetable.AnimeTableSortDirection.DESC
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.manami.gui.theme.ThemeState

@Composable
internal fun AnimeTableHeaderRow(
    config: AnimeTableConfig.() -> Unit = {},
    onSortRequested: (AnimeTableSortDirection) -> Unit = {},
) {
    val animeTableConfig = AnimeTableConfig().apply(config)
    val padding = 8.dp
    val backgroundColor = ThemeState.instance.currentScheme.value.primaryContainer
    val textColor = ThemeState.instance.currentScheme.value.onPrimaryContainer
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = textColor,
        fontWeight = Bold,
    )
    ManamiTheme {
        Row(Modifier.fillMaxSize().background(backgroundColor)) {
            Box(
                modifier = Modifier
                    .weight(animeTableConfig.weights[0])
                    .background(backgroundColor)
                    .padding(padding),
                contentAlignment = Center,
            ) {
                Text(
                    text = "Image",
                    style = textStyle,
                )
            }

            Box(
                modifier = Modifier
                    .weight(animeTableConfig.weights[1])
                    .background(backgroundColor)
                    .padding(padding),
                contentAlignment = Center
            ) {
                Row {
                    Text(
                        text = "Title",
                        style = textStyle,
                    )

                    if (animeTableConfig.withSortableTitle) {
                        IconButton(
                            icon = Icons.Filled.KeyboardArrowUp,
                            description = "sort ascending",
                            onClick = { onSortRequested(ASC) },
                        )
                        IconButton(
                            icon = Icons.Filled.KeyboardArrowDown,
                            description = "sort descending",
                            onClick = { onSortRequested(DESC) },
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .weight(animeTableConfig.weights[2])
                    .background(backgroundColor)
                    .padding(padding),
                contentAlignment = Center
            ) {
                Text(
                    text = "Actions",
                    style = textStyle,
                )
            }
        }
    }
}