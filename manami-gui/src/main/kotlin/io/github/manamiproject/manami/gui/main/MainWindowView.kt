package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.app.versioning.SemanticVersion
import io.github.manamiproject.manami.gui.*
import io.github.manamiproject.manami.gui.extensions.focus
import io.github.manamiproject.manami.gui.search.SearchBoxView
import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType.INFORMATION
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class MainWindowView : View() {

    private val manamiAccess: ManamiAccess by inject()
    private val menuView: MenuView by inject()
    private val searchBoxView: SearchBoxView by inject()
    private val tabPaneView: TabPaneView by inject()
    private val quitController: QuitController by inject()
    private val customTitleProperty = SimpleStringProperty("Manami")

    init {
        primaryStage.titleProperty().unbind()
        titleProperty.bindBidirectional(customTitleProperty)
        manamiAccess.primaryStage.isMaximized = true
        manamiAccess.primaryStage.onCloseRequest = EventHandler { event -> quitController.quit(); event.consume() }
        subscribe<FileOpenedGuiEvent> { event ->
            customTitleProperty.set(generateTitle(event.fileName))
        }
        subscribe<SavedAsFileGuiEvent> { event ->
            customTitleProperty.set(generateTitle(event.fileName))
        }
        subscribe<FileSavedStatusChangedGuiEvent> {
            handleUnsavedIndicator()
        }
        subscribe<NewVersionAvailableGuiEvent> { event ->
            handleNewVersionAvailable(event.version)
        }
    }

    override val root = vbox {
        hgrow = ALWAYS
        vgrow = ALWAYS

        hbox {
            hgrow = ALWAYS

            add(menuView.root)
            add(searchBoxView.root)
        }

        add(tabPaneView.root)
    }.apply { focus() }

    private fun generateTitle(fileName: String = EMPTY): String {
        val titleBuilder = StringBuilder("Manami")

        if (fileName.isNotBlank()) {
            titleBuilder.append(" - $fileName")
        }

        if (manamiAccess.isUnsaved()) {
            titleBuilder.append(FILE_SAVED_INDICATOR)
        }

        return titleBuilder.toString()
    }

    private fun handleUnsavedIndicator() {
        val newTitle = when {
            manamiAccess.isUnsaved() && !customTitleProperty.get().endsWith(FILE_SAVED_INDICATOR) -> "$title*"
            manamiAccess.isSaved() && customTitleProperty.get().endsWith(FILE_SAVED_INDICATOR) -> title.dropLast(1)
            else -> customTitleProperty.get()
        }
        customTitleProperty.set(newTitle)
    }

    private fun handleNewVersionAvailable(version: SemanticVersion) {
        Alert(INFORMATION).apply {
            title = "New version available"
            headerText = "There is a new version available: $version"
            contentText = "https://github.com/manami-project/manami/releases/latest"
            buttonTypes.clear()
            buttonTypes.addAll(
                ButtonType("OK", ButtonBar.ButtonData.OK_DONE),
            )
        }.showAndWait().get()
    }

    companion object {
        private const val FILE_SAVED_INDICATOR = "*"
    }
}