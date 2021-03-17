package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.gui.components.Alerts.AlertOption.*
import io.github.manamiproject.manami.gui.components.Alerts.unsavedChangedAlert
import io.github.manamiproject.manami.gui.components.PathChooser.showSaveAsFileDialog
import tornadofx.Controller

class SafelyExecuteActionController : Controller() {

    private val manamiAccess: ManamiAccess by inject()

    fun safelyExecute(action: (Boolean) -> Unit) {
        var option = if (manamiAccess.isUnsaved()) {
            unsavedChangedAlert()
        } else {
            NONE
        }

        if (option == YES) {
            if (manamiAccess.isOpenFileSet()) {
                runAsync {
                    manamiAccess.save()
                }
            } else {
                val file = showSaveAsFileDialog(primaryStage)

                if (file != null) {
                    manamiAccess.saveAs(file)
                } else {
                    option = CANCEL
                }
            }
        }

        if (option != CANCEL) {
            runAsync {
                action.invoke(option == NO)
            }
        }
    }
}