package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.gui.search.ShowSearchTabRequest
import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos.CENTER_RIGHT
import tornadofx.*

class SearchBoxView: View() {

    private val controller: SearchBoxController by inject()
    private val searchStringProperty = SimpleStringProperty()

    override val root = hbox {
        alignment = CENTER_RIGHT
        spacing = 5.0

        textfield {
            promptText = "Title"
            textProperty().bindBidirectional(searchStringProperty)
        }
        button("Search") {
            isDefaultButton = true
            isDisable = true
            searchStringProperty.onChange { e -> isDisable = e?.isBlank() ?: true }
            action {
                controller.search(searchStringProperty.get())
                searchStringProperty.set(EMPTY)
            }
        }
    }
}

class SearchBoxController: Controller() {

    fun search(searchString: String) {
        runAsync {
            Manami.search(searchString)
        }
        fire(ShowSearchTabRequest)
    }
}