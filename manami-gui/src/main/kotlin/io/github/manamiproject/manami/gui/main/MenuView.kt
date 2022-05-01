package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.app.versioning.ResourceBasedVersionProvider
import io.github.manamiproject.manami.gui.events.CachePopulatorFinishedGuiEvent
import io.github.manamiproject.manami.gui.events.FileOpenedGuiEvent
import io.github.manamiproject.manami.gui.events.FileSavedStatusChangedGuiEvent
import io.github.manamiproject.manami.gui.events.UndoRedoStatusGuiEvent
import io.github.manamiproject.manami.gui.animelist.ShowAnimeListTabRequest
import io.github.manamiproject.manami.gui.components.PathChooser
import io.github.manamiproject.manami.gui.ignorelist.ShowIgnoreListTabRequest
import io.github.manamiproject.manami.gui.inconsistencies.ShowInconsistenciesTabRequest
import io.github.manamiproject.manami.gui.migration.ShowMetaDataProviderMigrationViewTabRequest
import io.github.manamiproject.manami.gui.relatedanime.ShowRelatedAnimeTabRequest
import io.github.manamiproject.manami.gui.search.anime.ShowAnimeSearchTabRequest
import io.github.manamiproject.manami.gui.search.season.ShowAnimeSeasonTabRequest
import io.github.manamiproject.manami.gui.watchlist.ShowWatchListTabRequest
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class MenuView : View() {

    private val controller: MenuController by inject()
    private val isFileSaved = SimpleBooleanProperty(true)
    private val isUndoPossible = SimpleBooleanProperty(true)
    private val isRedoPossible = SimpleBooleanProperty(true)
    private val isInconsistenciesDisabled = SimpleBooleanProperty(true)
    private val isMetaDataProviderMigrationDisabled = SimpleBooleanProperty(true)
    private val isDisabledBecauseCacheIsNotYetPopulated = SimpleBooleanProperty(true)

    init {
        subscribe<FileSavedStatusChangedGuiEvent> { event ->
            isFileSaved.set(event.isFileSaved)
        }
        subscribe<FileOpenedGuiEvent> {
            isInconsistenciesDisabled.set(false)
            isMetaDataProviderMigrationDisabled.set(false)
        }
        subscribe<UndoRedoStatusGuiEvent> { event ->
            isUndoPossible.set(!event.isUndoPossible)
            isRedoPossible.set(!event.isRedoPossible)
            isInconsistenciesDisabled.set(!event.isUndoPossible)
        }
        subscribe<CachePopulatorFinishedGuiEvent> {
            isDisabledBecauseCacheIsNotYetPopulated.set(false)
        }
    }

    override val root = menubar {
        hgrow = ALWAYS
        vgrow = ALWAYS

        menu("File") {
            item("New", createMnemonic("N")) {
                action { controller.newFile() }
            }
            item("Open", createMnemonic("O")) {
                action { controller.open(PathChooser.showOpenFileDialog(primaryStage)) }
            }
            separator()
            item("Save", createMnemonic("S")) {
                disableProperty().bindBidirectional(isFileSaved)
                action { controller.save() }
            }
            item("Save as...", createMnemonic("Shift+S")) {
                disableProperty().bindBidirectional(isFileSaved)
                action { controller.saveAs(PathChooser.showSaveAsFileDialog(primaryStage)) }
            }
            separator()
            item("Quit", createMnemonic("Q")) {
                action { controller.quit() }
            }
        }
        menu("Edit") {
            item("Undo", createMnemonic("Z")) {
                disableProperty().bindBidirectional(isUndoPossible)
                action { controller.undo() }
            }
            item("Redo", createMnemonic("Shift+N")) {
                disableProperty().bindBidirectional(isRedoPossible)
                action { controller.redo() }
            }
        }
        menu("Lists") {
            item("Anime List", createMnemonic("1")) {
                action { fire(ShowAnimeListTabRequest) }
            }
            item("Watch List", createMnemonic("2")) {
                action { fire(ShowWatchListTabRequest) }
            }
            item("Ignore List", createMnemonic("3")) {
                action { fire(ShowIgnoreListTabRequest) }
            }
        }
        menu("Find") {
            item("Anime", createMnemonic("5")) {
                disableProperty().bindBidirectional(isDisabledBecauseCacheIsNotYetPopulated)
                action { fire(ShowAnimeSearchTabRequest) }
            }
            item("Season", createMnemonic("6")) {
                disableProperty().bindBidirectional(isDisabledBecauseCacheIsNotYetPopulated)
                action { fire(ShowAnimeSeasonTabRequest) }
            }
            item("Inconsistencies", createMnemonic("7")) {
                disableProperty().bindBidirectional(isInconsistenciesDisabled)
                action { fire(ShowInconsistenciesTabRequest) }
            }
            item("Meta Data Provider Migration", createMnemonic("8")) {
                disableProperty().bindBidirectional(isMetaDataProviderMigrationDisabled)
                action { fire(ShowMetaDataProviderMigrationViewTabRequest) }
            }
            item("Related Anime", createMnemonic("9")) {
                isDisable = false
                action { fire(ShowRelatedAnimeTabRequest) }
            }
        }
        menu("Help") {
            item("About", createMnemonic("H")).action { showHelp() }
        }
    }

    private fun showHelp() {
        Alert(INFORMATION).apply {
            title = "Help"
            headerText = "Version: ${ResourceBasedVersionProvider.version()}"
            contentText = """
                Free non-commercial software. (AGPLv3.0)

                Project / Source code: https://github.com/manami-project/manami
                License: https://github.com/manami-project/manami/blob/${ResourceBasedVersionProvider.version()}/LICENSE
            """.trimIndent()
            dialogPane.minWidth = 450.0
        }.showAndWait()
    }

    private fun createMnemonic(key: String): String {
        val initiator = if (isMac()) "Meta" else "Ctrl"
        return "$initiator+$key"
    }

    private fun isMac(): Boolean = System.getProperty("os.name").startsWith("Mac")
}