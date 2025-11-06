package io.github.manamiproject.manami.gui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
internal fun ManamiTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ThemeState.instance.currentScheme.value) {
        CompositionLocalProvider(LocalContentColor provides ThemeState.instance.currentScheme.value.onBackground) {
            content()
        }
    }
}