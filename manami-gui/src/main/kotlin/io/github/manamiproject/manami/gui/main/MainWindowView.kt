package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.gui.*
import io.github.manamiproject.manami.gui.extensions.focus
import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventHandler
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*
import java.util.*

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
        this.primaryStage.isMaximized = true
        this.primaryStage.onCloseRequest = EventHandler { event -> quitController.quit(); event.consume() }
        subscribe<FileOpenedGuiEvent> { event ->
            customTitleProperty.set(generateTitle(event.fileName))
        }
        subscribe<SavedAsFileGuiEvent> { event ->
            customTitleProperty.set(generateTitle(event.fileName))
        }
        subscribe<FileSavedStatusChangedGuiEvent> {
            handleUnsavedIndicator()
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

    companion object {
        private const val FILE_SAVED_INDICATOR = "*"
    }
}