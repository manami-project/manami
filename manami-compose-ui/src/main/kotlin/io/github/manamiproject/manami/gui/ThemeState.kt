package io.github.manamiproject.manami.gui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

private val lightColorScheme = lightColorScheme()
private val darkColorScheme = darkColorScheme()

internal class ThemeState() {
    private var isLight = true

    private var _currentColorScheme by mutableStateOf(lightColorScheme)
    val currentScheme: ColorScheme
        get() = _currentColorScheme

    private var _caption by mutableStateOf(CAPTION_TO_DARK_THEME)
    val caption: String
        get() = _caption

    fun toggle() {
        when (isLight) {
            true -> {
                _currentColorScheme = darkColorScheme
                _caption = CAPTION_TO_LIGHT_THEME
            }
            false -> {
                _currentColorScheme = lightColorScheme
                _caption = CAPTION_TO_DARK_THEME
            }
        }
        isLight = !isLight
    }

    companion object {
        private const val CAPTION_TO_DARK_THEME = "Change to 'dark' theme"
        private const val CAPTION_TO_LIGHT_THEME = "Change to 'light' theme"

        /**
         * Singleton of [ThemeState]
         * @since 4.0.0
         */
        val instance: ThemeState by lazy { ThemeState() }
    }
}