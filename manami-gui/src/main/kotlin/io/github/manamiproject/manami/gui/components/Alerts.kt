package io.github.manamiproject.manami.gui.components

import io.github.manamiproject.modb.core.models.Title
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.CONFIRMATION
import javafx.scene.control.ButtonBar.ButtonData.*
import javafx.scene.control.ButtonType

object Alerts {

    fun unsavedChangedAlert(): AlertOption {
        val alertResult = Alert(CONFIRMATION).apply {
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

        return when(alertResult.buttonData.typeCode) {
            YES.typeCode -> AlertOption.YES
            NO.typeCode -> AlertOption.NO
            CANCEL_CLOSE.typeCode -> AlertOption.CANCEL
            else -> throw IllegalStateException("Unknown ButtonType")
        }
    }

    fun removeEntry(animeTitle: Title): AlertOption {
        val alertResult = Alert(CONFIRMATION).apply {
            title = "Remove Entry"
            headerText = "Do you really want to remove this entry?"
            contentText = animeTitle
            buttonTypes.clear()
            buttonTypes.addAll(
                ButtonType("Yes", YES),
                ButtonType("No", NO),
            )
        }.showAndWait().get()

        return when(alertResult.buttonData.typeCode) {
            YES.typeCode -> AlertOption.YES
            NO.typeCode -> AlertOption.NO
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