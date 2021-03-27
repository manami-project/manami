package io.github.manamiproject.manami.gui.animelist

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.gui.AddAnimeListEntryGuiEvent
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.RemoveAnimeListEntryGuiEvent
import io.github.manamiproject.manami.gui.components.animeTable
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class AnimeListView : View() {

    private val manamiAccess: ManamiAccess by inject()

    private val entries: ObjectProperty<ObservableList<AnimeListEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList(manamiAccess.animeList())
    )

    init {
        subscribe<AddAnimeListEntryGuiEvent> { event ->
            entries.value.addAll(event.entries)
        }
        subscribe<RemoveAnimeListEntryGuiEvent> { event ->
            entries.value.removeAll(event.entries)
        }
    }

    override val root = pane {

        vbox {
            vgrow = ALWAYS
            hgrow = ALWAYS
            fitToParentSize()

            animeTable<AnimeListEntry> {
                manamiApp = manamiAccess
                items = entries
                withToWatchListButton = false
                withToIgnoreListButton = false
                withHideButton = false
                withDeleteButton = true
                onDelete = {
                    manamiAccess.removeAnimeListEntry(it)
                }
            }
        }
    }
}
