package io.github.manamiproject.manami.app.events

import io.github.manamiproject.manami.app.state.NoFile
import io.github.manamiproject.manami.app.state.OpenedFile

/**
 * @since 4.0.0
 */
data class GeneralAppState(
    val isFileSaved: Boolean = true,
    val isUndoPossible: Boolean = false,
    val isRedoPossible: Boolean = false,
    val isOpeningFileRunning: Boolean = false,
    val openedFile: OpenedFile = NoFile,
)
