package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.gui.FileSavedStatusChangedGuiEvent
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.SafelyExecuteActionController
import io.github.manamiproject.manami.gui.animelist.ShowAnimeListTabRequest
import io.github.manamiproject.manami.gui.components.PathChooser
import io.github.manamiproject.manami.gui.ignorelist.ShowIgnoreListTabRequest
import io.github.manamiproject.manami.gui.inconsistencies.ShowInconsistenciesTabRequest
import io.github.manamiproject.manami.gui.recommendations.ShowRecommendationsTabRequest
import io.github.manamiproject.manami.gui.relatedanime.ShowRelatedAnimeTabRequest
import io.github.manamiproject.manami.gui.search.ShowSearchTabRequest
import io.github.manamiproject.manami.gui.watchlist.ShowWatchListTabRequest
import io.github.manamiproject.modb.core.extensions.RegularFile
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class MenuView : View() {

    private val controller: MenuController by inject()
    private val isFileSaved = SimpleBooleanProperty(true)

    init {
        subscribe<FileSavedStatusChangedGuiEvent> { event ->
            isFileSaved.set(event.isFileSaved)
        }
    }

    override val root = menubar {
        hgrow = ALWAYS
        vgrow = ALWAYS

        menu("File") {
            item("New") {
                action { controller.newFile() }
            }
            item("Open") {
                action { controller.open(PathChooser.showOpenFileDialog(primaryStage)) }
            }
            separator()
            item("Import") {
                action { controller.import(PathChooser.showImportFileDialog(primaryStage)) }
            }
            separator()
            item("Save") {
                disableProperty().bindBidirectional(isFileSaved)
                action { controller.save() }
            }
            item("Save as...") {
                disableProperty().bindBidirectional(isFileSaved)
                action { controller.saveAs(PathChooser.showSaveAsFileDialog(primaryStage)) }
            }
            separator()
            item("Quit") {
                action { controller.quit() }
            }
        }
        menu("Edit") {
            item("Undo") {
                isDisable = true
                action { controller.undo() }
            }
            item("Redo") {
                isDisable = true
                action { controller.redo() }
            }
        }
        menu("Lists") {
            item("Anime List") {
                action { fire(ShowAnimeListTabRequest) }
            }
            item("Watch List") {
                action { fire(ShowWatchListTabRequest) }
            }
            item("Ignore List") {
                action { fire(ShowIgnoreListTabRequest) }
            }
        }
        menu("Find") {
            item("Anime") {
                isDisable = true
                action { fire(ShowSearchTabRequest) }
            }
            item("Inconsistencies") {
                isDisable = true
                action { fire(ShowInconsistenciesTabRequest) }
            }
            item("Recommendations") {
                isDisable = true
                action { fire(ShowRecommendationsTabRequest) }
            }
            item("Related Anime") {
                isDisable = true
                action { fire(ShowRelatedAnimeTabRequest) }
            }
        }
        menu("Help") {
            item("About").action { controller.help() }
        }
    }
}

class MenuController : Controller() {

    private val manamiAccess: ManamiAccess by inject()
    private val safelyExecuteActionController: SafelyExecuteActionController by inject()
    private val quitController: QuitController by inject()

    fun newFile() {
        safelyExecuteActionController.safelyExecute { ignoreUnsavedChanged ->
            runAsync {
                manamiAccess.newFile(ignoreUnsavedChanged = ignoreUnsavedChanged)
            }
        }
    }

    fun open(file: RegularFile?) {
        if (file == null) {
            return
        }

        safelyExecuteActionController.safelyExecute { ignoreUnsavedChanged ->
            runAsync {
                manamiAccess.open(file = file, ignoreUnsavedChanged = ignoreUnsavedChanged)
            }
        }
    }

    fun import(file: RegularFile?) {
        if (file != null) {
            runAsync {
                manamiAccess.import(file)
            }
        }
    }

    fun save() {
        if (manamiAccess.isOpenFileSet()) {
            runAsync {
                manamiAccess.save()
            }
        } else {
            saveAs(PathChooser.showSaveAsFileDialog(primaryStage))
        }
    }

    fun saveAs(file: RegularFile?) {
        if (file != null) {
            runAsync {
                manamiAccess.saveAs(file)
            }
        }
    }

    fun undo() {
        runAsync {
            manamiAccess.undo()
        }
    }

    fun redo() {
        runAsync {
            manamiAccess.redo()
        }
    }

    fun help() {
        Alert(INFORMATION).apply {
            title = "Help"
            headerText = "Version: #.#.#"
            contentText = """
                Free non-commercial software. (AGPLv3.0)

                https://github.com/manami-project/manami
            """.trimIndent()
        }.showAndWait()
    }

    fun quit() {
        quitController.quit()
    }
}