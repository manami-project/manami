package io.github.manamiproject.manami.gui.migration

import io.github.manamiproject.manami.gui.components.Alerts.AlertOption
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType

object MigrationAlerts {

    fun migrateEntries(
        numberOfEntriesAnimeList: Int,
        numberOfEntriesWatchList: Int,
        numberOfEntriesIgnoreList: Int,
    ): AlertOption {
        val totalEntries = numberOfEntriesAnimeList + numberOfEntriesWatchList + numberOfEntriesIgnoreList
        val alertResult = Alert(Alert.AlertType.CONFIRMATION).apply {
            title = "Migrate entries?"
            headerText = "Do you really want to migrate $totalEntries entries?"
            contentText = "Anime List: $numberOfEntriesAnimeList\nWatch List: $numberOfEntriesWatchList\nIgnore List: $numberOfEntriesIgnoreList"
            buttonTypes.clear()
            buttonTypes.addAll(
                ButtonType("Yes", ButtonBar.ButtonData.YES),
                ButtonType("No", ButtonBar.ButtonData.NO),
            )
        }.showAndWait().get()

        return when(alertResult.buttonData.typeCode) {
            ButtonBar.ButtonData.YES.typeCode -> AlertOption.YES
            ButtonBar.ButtonData.NO.typeCode -> AlertOption.NO
            else -> throw IllegalStateException("Unknown ButtonType")
        }
    }

    fun removeUnmappedEntries(
        numberOfEntriesAnimeList: Int,
        numberOfEntriesWatchList: Int,
        numberOfEntriesIgnoreList: Int,
    ): AlertOption {
        val totalEntries = numberOfEntriesAnimeList + numberOfEntriesWatchList + numberOfEntriesIgnoreList
        val alertResult = Alert(Alert.AlertType.CONFIRMATION).apply {
            title = "Remove unmapped entries?"
            headerText = "Do you want to remove $totalEntries entries which couldn't be mapped to the new meta data provider?"
            contentText = "Anime List: $numberOfEntriesAnimeList\nWatch List: $numberOfEntriesWatchList\nIgnore List: $numberOfEntriesIgnoreList"
            buttonTypes.clear()
            buttonTypes.addAll(
                ButtonType("Yes", ButtonBar.ButtonData.YES),
                ButtonType("No", ButtonBar.ButtonData.NO),
            )
        }.showAndWait().get()

        return when(alertResult.buttonData.typeCode) {
            ButtonBar.ButtonData.YES.typeCode -> AlertOption.YES
            ButtonBar.ButtonData.NO.typeCode -> AlertOption.NO
            else -> throw IllegalStateException("Unknown ButtonType")
        }
    }
}