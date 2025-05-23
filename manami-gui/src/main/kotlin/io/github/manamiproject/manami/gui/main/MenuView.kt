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
import io.github.manamiproject.manami.gui.search.similaranime.ShowSimilarAnimeSearchTabRequest
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
    private val isFunctionsWhichNeedListEntriesDisabled = SimpleBooleanProperty(true)
    private val isDisabledBecauseCacheIsNotYetPopulated = SimpleBooleanProperty(true)

    init {
        subscribe<FileSavedStatusChangedGuiEvent> { event ->
            isFileSaved.set(event.isFileSaved)
        }
        subscribe<FileOpenedGuiEvent> {
            isFunctionsWhichNeedListEntriesDisabled.set(false)
        }
        subscribe<UndoRedoStatusGuiEvent> { event ->
            isUndoPossible.set(!event.isUndoPossible)
            isRedoPossible.set(!event.isRedoPossible)
            isFunctionsWhichNeedListEntriesDisabled.set(!event.isUndoPossible)
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
            item("Anime List", createMnemonic("A")) {
                action { fire(ShowAnimeListTabRequest) }
            }
            item("Watch List", createMnemonic("W")) {
                action { fire(ShowWatchListTabRequest) }
            }
            item("Ignore List", createMnemonic("I")) {
                action { fire(ShowIgnoreListTabRequest) }
            }
        }
        menu("Find") {
            item("Anime", createMnemonic("1")) {
                disableProperty().bindBidirectional(isDisabledBecauseCacheIsNotYetPopulated)
                action { fire(ShowAnimeSearchTabRequest) }
            }
            item("Season", createMnemonic("2")) {
                disableProperty().bindBidirectional(isDisabledBecauseCacheIsNotYetPopulated)
                action { fire(ShowAnimeSeasonTabRequest) }
            }
            item("Inconsistencies", createMnemonic("3")) {
                disableProperty().bindBidirectional(isFunctionsWhichNeedListEntriesDisabled)
                action { fire(ShowInconsistenciesTabRequest) }
            }
            item("Related Anime", createMnemonic("4")) {
                disableProperty().bindBidirectional(isFunctionsWhichNeedListEntriesDisabled)
                action { fire(ShowRelatedAnimeTabRequest) }
            }
            item("Similar Anime", createMnemonic("5")) {
                disableProperty().bindBidirectional(isDisabledBecauseCacheIsNotYetPopulated)
                action { fire(ShowSimilarAnimeSearchTabRequest) }
            }
            item("Meta Data Provider Migration", createMnemonic("6")) {
                disableProperty().bindBidirectional(isFunctionsWhichNeedListEntriesDisabled)
                action { fire(ShowMetaDataProviderMigrationViewTabRequest) }
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
                
                Uses data from https://github.com/manami-project/anime-offline-database which is made available here under the Open Database License (ODbL)
                License: https://opendatacommons.org/licenses/odbl/1-0/
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