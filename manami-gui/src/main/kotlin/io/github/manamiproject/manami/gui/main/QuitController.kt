package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.SafelyExecuteActionController
import tornadofx.Controller

class QuitController : Controller() {

    private val manamiAccess: ManamiAccess by inject()
    private val safelyExecuteActionController: SafelyExecuteActionController by inject()

    fun quit() {
        safelyExecuteActionController.safelyExecute { ignoreUnsavedChanged ->
            manamiAccess.quit(ignoreUnsavedChanged)
        }
    }
}