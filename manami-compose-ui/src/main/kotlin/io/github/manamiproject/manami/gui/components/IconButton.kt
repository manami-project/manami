package io.github.manamiproject.manami.gui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.manami.gui.theme.ThemeState

@Composable
internal fun IconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    size: Dp = 18.dp,
    colorScheme: ColorScheme = ThemeState.instance.currentScheme,
) {
    ManamiTheme {
        Tooltip(text = description) {
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(size),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = description,
                    tint = colorScheme.primary,
                )
            }
        }
    }
}