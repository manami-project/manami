package io.github.manamiproject.manami.gui.components.simpletable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.manami.gui.theme.ThemeState

@Composable
internal fun SimpleTableHeaderRow(
    config: SimpleTableConfig.() -> Unit = {},
) {
    val simpleTableConfig = SimpleTableConfig().apply(config)
    val padding = 8.dp
    val backgroundColor = ThemeState.instance.currentScheme.value.primaryContainer
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        color = ThemeState.instance.currentScheme.value.onPrimaryContainer,
        fontWeight = Bold,
        fontSize = TextUnit(simpleTableConfig.contentFontSize, TextUnitType.Sp),
    )
    ManamiTheme {
        Row(Modifier.fillMaxWidth().background(backgroundColor)) {
            Box(
                modifier = Modifier.weight(simpleTableConfig.weights[0])
                    .background(backgroundColor)
                    .padding(padding),
                contentAlignment = simpleTableConfig.contentAlignment,
            ) {
                Text(
                    text = simpleTableConfig.keyHeadline,
                    style = textStyle,
                )
            }

            Box(
                modifier = Modifier.weight(simpleTableConfig.weights[1])
                    .background(backgroundColor)
                    .padding(padding),
                contentAlignment = simpleTableConfig.contentAlignment,
            ) {
                Row {
                    Text(
                        text = simpleTableConfig.valueHeadline,
                        style = textStyle,
                    )
                }
            }
        }
    }
}