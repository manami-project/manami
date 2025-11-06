package io.github.manamiproject.manami.gui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.State

internal class ThemeState() {
    private var isLight = true

    private var _currentColorScheme by mutableStateOf(LIGHT_COLOR_SCHEME)
    val currentScheme: State<ColorScheme>
        get() = derivedStateOf { _currentColorScheme }

    private var _caption by mutableStateOf(CAPTION_TO_DARK_THEME)
    val caption: String
        get() = _caption

    fun toggle() {
        when (isLight) {
            true -> {
                _currentColorScheme = DARK_COLOR_SCHEME
                _caption = CAPTION_TO_LIGHT_THEME
            }
            false -> {
                _currentColorScheme = LIGHT_COLOR_SCHEME
                _caption = CAPTION_TO_DARK_THEME
            }
        }
        isLight = !isLight
    }

    internal companion object {
        private const val CAPTION_TO_DARK_THEME = "Change to 'dark' theme"
        private const val CAPTION_TO_LIGHT_THEME = "Change to 'light' theme"
        private val LIGHT_COLOR_SCHEME = lightColorScheme()
        private val DARK_COLOR_SCHEME = darkColorScheme(
            background = Color(42, 42, 48),
        )

        /**
         * Singleton of [ThemeState]
         * @since 4.0.0
         */
        val instance: ThemeState by lazy { ThemeState() }
    }
}