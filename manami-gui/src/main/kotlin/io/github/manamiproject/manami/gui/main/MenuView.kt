package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.app.fileexport.FileFormat
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.animelist.ShowAnimeListTabRequest
import io.github.manamiproject.manami.gui.components.Alerts
import io.github.manamiproject.manami.gui.components.Alerts.AlertOption.*
import io.github.manamiproject.manami.gui.components.PathChooser
import io.github.manamiproject.manami.gui.ignorelist.ShowIgnoreListTabRequest
import io.github.manamiproject.manami.gui.inconsistencies.ShowInconsistenciesTabRequest
import io.github.manamiproject.manami.gui.recommendations.ShowRecommendationsTabRequest
import io.github.manamiproject.manami.gui.relatedanime.ShowRelatedAnimeTabRequest
import io.github.manamiproject.manami.gui.search.ShowSearchTabRequest
import io.github.manamiproject.manami.gui.watchlist.ShowWatchListTabRequest
import io.github.manamiproject.modb.core.extensions.RegularFile
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class MenuView : View() {

    private val controller: MenuController by inject()

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
            item("Export") {
                isDisable = true
                action { controller.export(PathChooser.showExportDialog(primaryStage)) }
            }
            separator()
            item("Save") {
                isDisable = true
                action { controller.save() }
            }
            item("Save as...") {
                isDisable = true
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

    fun newFile() {
        val action = if (manamiAccess.isUnsaved()) {
            Alerts.unsavedChangedAlert()
        } else {
            NONE
        }

        if (action == YES) {
            save()
        }

        if (action != CANCEL) {
            runAsync {
                manamiAccess.newFile(ignoreUnsavedChanged = action == NO)
            }
        }
    }

    fun open(file: RegularFile?) {
        if (file == null) {
            return
        }

        val action = if (manamiAccess.isUnsaved()) {
            Alerts.unsavedChangedAlert()
        } else {
            NONE
        }

        if (action == YES) {
            save()
        }

        if (action != CANCEL) {
            runAsync {
                manamiAccess.open(file = file, ignoreUnsavedChanged = action == NO)
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

    fun export(file: RegularFile?) {
        if (file != null) {
            runAsync {
                manamiAccess.export(file, FileFormat.JSON)
            }
        }
    }

    fun save() {
        runAsync {
            manamiAccess.save()
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
        Platform.exit()
    }
}