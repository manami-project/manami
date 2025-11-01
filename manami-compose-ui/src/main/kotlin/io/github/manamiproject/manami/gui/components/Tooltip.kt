package io.github.manamiproject.manami.gui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.manami.gui.theme.ThemeState

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Tooltip(
    text: String,
    colorScheme: ColorScheme = ThemeState.instance.currentScheme,
    content: @Composable () -> Unit,
) {
    ManamiTheme {
        TooltipArea(
            tooltip = {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colorScheme.onSurface.copy(alpha = 0.9f)
                ) {
                    Text(
                        text = text,
                        color = colorScheme.surface,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            },
            delayMillis = 700,
            tooltipPlacement = TooltipPlacement.CursorPoint(
                offset = DpOffset(x = 12.dp, y = 8.dp)
            )
        ) {
            content()
        }
    }
}