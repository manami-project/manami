package io.github.manamiproject.manami.gui.watchlist

import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.gui.AddWatchListEntryGuiEvent
import io.github.manamiproject.manami.gui.AddWatchListStatusUpdateGuiEvent
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.RemoveWatchListEntryGuiEvent
import io.github.manamiproject.manami.gui.components.animeTable
import io.github.manamiproject.manami.gui.components.simpleAnimeAddition
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class WatchListView : View() {

    private val manamiAccess: ManamiAccess by inject()
    private val finishedTasks: SimpleIntegerProperty = SimpleIntegerProperty(0)
    private val totalNumberOfTasks: SimpleIntegerProperty = SimpleIntegerProperty(0)

    private val watchListEntries: ObjectProperty<ObservableList<WatchListEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList(manamiAccess.watchList())
    )

    init {
        subscribe<AddWatchListEntryGuiEvent> { event ->
            watchListEntries.value.addAll(event.entry)
        }
        subscribe<RemoveWatchListEntryGuiEvent> { event ->
            watchListEntries.value.removeAll(event.entry)
        }
        subscribe<AddWatchListStatusUpdateGuiEvent> { event ->
            finishedTasks.set(event.finishedTasks)
            totalNumberOfTasks.set(event.tasks)
        }
    }

    override val root = pane {

        vbox {
            vgrow = ALWAYS
            hgrow = ALWAYS
            fitToParentSize()

            simpleAnimeAddition {
                finishedTasksProperty = finishedTasks
                totalNumberOfTasksProperty = totalNumberOfTasks
                onAdd = { entry ->
                    manamiAccess.addWatchListEntry(entry)
                }
            }

            animeTable<WatchListEntry> {
                items = watchListEntries
            }
        }
    }
}