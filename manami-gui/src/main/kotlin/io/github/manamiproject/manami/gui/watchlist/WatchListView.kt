package io.github.manamiproject.manami.gui.watchlist

import io.github.manamiproject.manami.app.models.WatchListEntry
import io.github.manamiproject.manami.gui.AddWatchListEntry
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.RemoveWatchListEntry
import io.github.manamiproject.manami.gui.components.animeTable
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.text.FontWeight
import javafx.scene.text.FontWeight.BOLD
import javafx.scene.text.FontWeight.EXTRA_BOLD
import tornadofx.*

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

            hbox {
                hgrow = ALWAYS
                alignment = CENTER
                padding = Insets(10.0)
                spacing = 5.0

                label {
                    text = "URL"
                    style {
                        fontWeight = EXTRA_BOLD
                    }
                }

                textfield {
                    promptText = "https://myanimelist.net/anime/1535"
                    prefWidth = 200.0
                }

                button {
                    text = "add"
                    isDefaultButton = true
                }
            }

            animeTable<WatchListEntry> {
                items = watchListEntries
            }
        }
    }
}