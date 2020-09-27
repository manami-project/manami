package io.github.manamiproject.manami.gui.components

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.scene.control.ButtonBar.ButtonData.*
import javafx.scene.control.ButtonType

object Alerts {

    fun unsavedChangedAlert(): AlertOption {
        val buttonType = Alert(CONFIRMATION).apply {
            title = "Unsaved changes"
            headerText = "Your changes will be lost if you don't save them."
            contentText = "Do you want to save your changes?"
            buttonTypes.clear()
            buttonTypes.addAll(
                    ButtonType("Yes", YES),
                    ButtonType("No", NO),
                    ButtonType("Cancel", CANCEL_CLOSE),
            )
        }.showAndWait().get()

        return when(buttonType.buttonData.typeCode) {
            YES.typeCode -> AlertOption.YES
            NO.typeCode -> AlertOption.NO
            CANCEL_CLOSE.typeCode -> AlertOption.CANCEL
            else -> throw IllegalStateException("Unknown ButtonType")
        }
    }

    enum class AlertOption {
        YES,
        NO,
        CANCEL,
        NONE,
    }
}