package io.github.manamiproject.manami.gui

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.gui.components.showOpenFileDialog
import io.github.manamiproject.manami.gui.components.showSaveAsFileDialog
import io.github.manamiproject.manami.gui.components.unsavedchangesdialog.UnsavedChangesDialogState
import io.github.manamiproject.manami.gui.tabs.TabBarViewModel
import io.github.manamiproject.manami.gui.tabs.Tabs.*
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.launch
import java.awt.Toolkit

internal class MainViewModel(
    private val app: Manami = Manami.instance,
    private val tabBarViewModel: TabBarViewModel = TabBarViewModel.instance,
) {
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

    val isAnyListContainingEntries: StateFlow<Boolean> = combine(app.animeListState,app.watchListState,app.ignoreListState) { animeListState, watchListState, ignoreListState ->
        animeListState.entries.isNotEmpty() || watchListState.entries.isNotEmpty() || ignoreListState.entries.isNotEmpty()
    }.stateIn(
        scope = viewModelScope,
        started = Eagerly,
        initialValue = false,
    )

    val windowTitle = combine(isSaved, openedFile) { saved, file ->
        buildString {
            append("Manami")
            if (file.neitherNullNorBlank()) append(" - $file")
            if (!saved) append("*")
        }
    }.stateIn(viewModelScope, Eagerly, "Manami")

    private val _showAboutDialog = MutableStateFlow(false)
    val showAboutDialog = _showAboutDialog.asStateFlow()

    private val _showUnsavedChangesDialog = MutableStateFlow(UnsavedChangesDialogState())
    val showUnsavedChangesDialogState = _showUnsavedChangesDialog.asStateFlow()

    private val _showSafelyQuitDialog = MutableStateFlow(false)
    val showSafelyQuitDialog = _showSafelyQuitDialog.asStateFlow()

    fun windowSize(): DpSize {
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        val screenWidth = screenSize.width.dp
        val screenHeight = screenSize.height.dp
        return DpSize(screenWidth, screenHeight)
    }

    fun new(parent: FrameWindowScope, ignoreUnsavedChanges: Boolean = false) {
        if (isSaved.value || (!isSaved.value && ignoreUnsavedChanges)) {
            viewModelScope.launch {
                app.newFile(ignoreUnsavedChanges)
            }
        } else {
            _showUnsavedChangesDialog.update {
                UnsavedChangesDialogState(
                    showUnsavedChangesDialog = true,
                    onCloseRequest = { _showUnsavedChangesDialog.update { UnsavedChangesDialogState() } },
                    onYes = { save(parent) },
                    onNo = { new(parent, true) },
                )
            }
        }
    }

    fun open(parent: FrameWindowScope, ignoreUnsavedChanges: Boolean = false) {
        if (isSaved.value || (!isSaved.value && ignoreUnsavedChanges)) {
            val file = parent.showOpenFileDialog()

            if (file != null) {
                viewModelScope.launch {
                    app.open(file)
                }
            }
        } else {
            _showUnsavedChangesDialog.update {
                UnsavedChangesDialogState(
                    showUnsavedChangesDialog = true,
                    onCloseRequest = { _showUnsavedChangesDialog.update { UnsavedChangesDialogState() } },
                    onYes = { save(parent) },
                    onNo = { open(parent, true) }
                )
            }
        }
    }

    fun save(parent: FrameWindowScope) {
        when (openedFile.value == EMPTY) {
            true -> saveAs(parent)
            false -> viewModelScope.launch { app.save() }
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

    fun openAnimeListTab() {
        tabBarViewModel.openOrActivate(ANIME_LIST)
    }

    fun openWatchListTab() {
        tabBarViewModel.openOrActivate(WATCH_LIST)
    }

    fun openIgnoreListTab() {
        tabBarViewModel.openOrActivate(IGNORE_LIST)
    }

    fun openFindAnimeTab() {
        tabBarViewModel.openOrActivate(FIND_ANIME)
    }

    fun openFindSeasonTab() {
        tabBarViewModel.openOrActivate(FIND_SEASON)
    }

    fun openFindInconsistenciesTab() {
        tabBarViewModel.openOrActivate(FIND_INCONSISTENCIES)
    }

    fun openFindRelatedAnimeTab() {
        tabBarViewModel.openOrActivate(FIND_RELATED_ANIME)
    }

    fun quit(parent: FrameWindowScope, ignoreUnsavedChanges: Boolean = false) {
        if (isSaved.value || (!isSaved.value && ignoreUnsavedChanges)) {
            viewModelScope.launch {
                app.quit(ignoreUnsavedChanges)
            }
        } else {
            _showUnsavedChangesDialog.update {
                UnsavedChangesDialogState(
                    showUnsavedChangesDialog = true,
                    onCloseRequest = { _showUnsavedChangesDialog.update { UnsavedChangesDialogState() } },
                    onYes = { save(parent) },
                    onNo = { quit(parent, true) },
                )
            }
        }
    }

    fun showAboutDialog() {
        _showAboutDialog.update { true }
    }

    fun closeAboutDialog() {
        _showAboutDialog.update { false }
    }

    fun showSafelyQuitDialog() {
        _showSafelyQuitDialog.update { true }
    }

    fun closeSafelyQuitDialog() {
        _showSafelyQuitDialog.update { false }
    }

    internal companion object {
        /**
         * Singleton of [MainViewModel]
         * @since 4.0.0
         */
        val instance: MainViewModel by lazy { MainViewModel() }
    }
}