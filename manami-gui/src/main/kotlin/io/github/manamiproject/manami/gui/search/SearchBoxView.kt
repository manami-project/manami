package io.github.manamiproject.manami.gui.search

import impl.org.controlsfx.autocompletion.SuggestionProvider
import io.github.manamiproject.manami.gui.AddAnimeListEntryGuiEvent
import io.github.manamiproject.manami.gui.AddIgnoreListEntryGuiEvent
import io.github.manamiproject.manami.gui.AddWatchListEntryGuiEvent
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.search.file.ShowFileSearchTabRequest
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.models.Title
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos.CENTER_RIGHT
import tornadofx.*
import tornadofx.controlsfx.bindAutoCompletion

class SearchBoxView: View() {

    private val controller: SearchBoxController by inject()
    private val searchStringProperty = SimpleStringProperty()
    private val suggestedEntries = mutableSetOf<String>()
    private val autoCompleteProvider: SuggestionProvider<String> = SuggestionProvider.create(emptyList())

    init {
        subscribe<AddAnimeListEntryGuiEvent> { event ->
            addTitlesToSuggestions(event.entries.map { it.title })
        }
        subscribe<AddWatchListEntryGuiEvent> { event ->
            addTitlesToSuggestions(event.entries.map { it.title })
        }
        subscribe<AddIgnoreListEntryGuiEvent> { event ->
            addTitlesToSuggestions(event.entries.map { it.title })
        }
        subscribe<ClearAutoCompleteSuggestionsGuiEvent> {
            autoCompleteProvider.clearSuggestions()
            suggestedEntries.clear()
        }
    }

    override val root = hbox {
        alignment = CENTER_RIGHT
        spacing = 5.0

        textfield {
            promptText = "Title"
            textProperty().bindBidirectional(searchStringProperty)
            bindAutoCompletion(autoCompleteProvider)
            minWidth = 250.0
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

    private fun addTitlesToSuggestions(titles: Collection<Title>) {
        val suggestionsToAdd = titles.map { it }.distinct().filterNot { suggestedEntries.contains(it) }
        autoCompleteProvider.addPossibleSuggestions(suggestionsToAdd)
        suggestedEntries.addAll(suggestionsToAdd)
    }
}

class SearchBoxController: Controller() {

    private val manamiAccess: ManamiAccess by inject()

    fun search(searchString: String) {
        runAsync {
            manamiAccess.findInLists(searchString)
        }
        fire(ShowFileSearchTabRequest)
    }
}