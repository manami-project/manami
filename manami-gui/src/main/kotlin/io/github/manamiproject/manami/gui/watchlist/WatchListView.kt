package io.github.manamiproject.manami.gui.watchlist

import io.github.manamiproject.manami.app.models.WatchListEntry
import io.github.manamiproject.manami.gui.AddWatchListEntry
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.RemoveWatchListEntry
import io.github.manamiproject.manami.gui.components.animeTable
import io.github.manamiproject.manami.gui.components.simpleAnimeAddition
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*
import java.net.URI

class WatchListView : View() {

    private val manamiAccess: ManamiAccess by inject()

    private val watchListEntries: ObjectProperty<ObservableList<WatchListEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList(manamiAccess.watchList())
    )

    init {
        subscribe<AddWatchListEntry> { event ->
            watchListEntries.value.addAll(event.entry)
        }
        subscribe<RemoveWatchListEntry> { event ->
            watchListEntries.value.removeAll(event.entry)
        }
    }

    override val root = pane {

        vbox {
            vgrow = ALWAYS
            hgrow = ALWAYS
            fitToParentSize()

            simpleAnimeAddition {
                onAdd = { entry -> manamiAccess.addWatchListEntry(entry) }
            }

            animeTable<WatchListEntry> {
                items = watchListEntries
            }
        }
    }
}