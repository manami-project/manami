package io.github.manamiproject.manami.gui.components.simpletable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.manami.gui.theme.ThemeState

@Composable
internal fun SimpleTableRow(
    config: SimpleTableConfig.() -> Unit = {},
    data: Pair<String, Any>,
) {
    val simpleTableConfig = SimpleTableConfig().apply(config)
    val backgroundColor = ThemeState.instance.currentScheme.value.surface
    val padding = 8.dp
    val textStyle = TextStyle.Default.copy(
        color = ThemeState.instance.currentScheme.value.onSurface,
        fontSize = TextUnit(simpleTableConfig.contentFontSize, TextUnitType.Sp),
    )

    ManamiTheme {
        Row(Modifier.fillMaxWidth().background(backgroundColor).height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier.weight(simpleTableConfig.weights[0])
                    .padding(padding)
                    .fillMaxHeight(),
                contentAlignment = simpleTableConfig.contentAlignment,
            ) {
                Text(
                    text = data.first,
                    style = textStyle,
                )
            }

            Box(
                modifier = Modifier.weight(simpleTableConfig.weights[1])
                    .background(backgroundColor)
                    .fillMaxHeight()
                    .padding(padding),
                contentAlignment = simpleTableConfig.contentAlignment,
            ) {
                Text(
                    text = data.second.toString(),
                    style = textStyle,
                )
            }
        }
    }
}