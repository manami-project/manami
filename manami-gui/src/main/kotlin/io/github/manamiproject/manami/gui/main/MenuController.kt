package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.SafelyExecuteActionController
import io.github.manamiproject.manami.gui.components.PathChooser
import io.github.manamiproject.modb.core.extensions.RegularFile
import javafx.scene.control.Alert
import tornadofx.Controller

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
        Alert(Alert.AlertType.INFORMATION).apply {
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