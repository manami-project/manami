package io.github.manamiproject.manami.gui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.gui.components.showOpenFileDialog
import io.github.manamiproject.manami.gui.components.showSaveAsFileDialog
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.awt.Toolkit

internal class MainWindowViewModel(private val app: Manami = Manami.instance) {
    private val viewModelScope = CoroutineScope(Default + SupervisorJob())

    val isSaved: StateFlow<Boolean> = app.generalAppState
        .map { it.isFileSaved }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = false
        )

    val isUndoPossible: StateFlow<Boolean> = app.generalAppState
        .map { it.isUndoPossible }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = false
        )

    val isRedoPossible: StateFlow<Boolean> = app.generalAppState
        .map { it.isRedoPossible }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = false
        )

    val openedFile: StateFlow<String> = app.generalAppState
        .map { it.openedFile }
        .stateIn(
            scope = viewModelScope,
            started = Eagerly,
            initialValue = EMPTY
        )

    val windowTitle = combine(isSaved, openedFile) { saved, file ->
        buildString {
            append("Manami")
            if (file.neitherNullNorBlank()) append(" - $file")
            if (!saved) append("*")
        }
    }.stateIn(viewModelScope, Eagerly, "Manami")

    fun windowSize(): DpSize {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val screenWidth = screenSize.width.dp
        val screenHeight = screenSize.height.dp
        return DpSize(screenWidth, screenHeight)
    }

    fun new() {
        viewModelScope.launch {
            app.newFile()
        }
    }

    fun open(parent: FrameWindowScope) {
        val file = parent.showOpenFileDialog()

        if (file != null) {
            viewModelScope.launch {
                app.open(file)
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            app.save()
        }
    }

    fun saveAs(parent: FrameWindowScope) {
        val file = parent.showSaveAsFileDialog()

        if (file != null) {
            viewModelScope.launch {
                app.saveAs(file)
            }
        }
    }

    fun undo() {
        viewModelScope.launch {
            app.undo()
        }
    }

    fun redo() {
        viewModelScope.launch {
            app.redo()
        }
    }

    fun quit() {
        //TODO 4.0.0: Check if state is currently saved
        viewModelScope.launch {
            app.quit()
        }
    }

    companion object {
        /**
         * Singleton of [MainWindowViewModel]
         * @since 4.0.0
         */
        val instance: MainWindowViewModel by lazy { MainWindowViewModel() }
    }
}