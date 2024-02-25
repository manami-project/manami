package io.github.manamiproject.manami.gui.watchlist

import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.gui.events.AddWatchListEntryGuiEvent
import io.github.manamiproject.manami.gui.events.AddWatchListStatusUpdateGuiEvent
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.events.RemoveWatchListEntryGuiEvent
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
    private val tasks: SimpleIntegerProperty = SimpleIntegerProperty(0)

    private val entries: ObjectProperty<ObservableList<WatchListEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList(manamiAccess.watchList())
    )

    init {
        subscribe<AddWatchListEntryGuiEvent> { event ->
            entries.value.addAll(event.entries)
        }
        subscribe<RemoveWatchListEntryGuiEvent> { event ->
            entries.value.removeAll(event.entries)
        }
        subscribe<AddWatchListStatusUpdateGuiEvent> { event ->
            finishedTasks.set(event.finishedTasks)
            tasks.set(event.tasks)
        }
    }

    override val root = pane {

        vbox {
            vgrow = ALWAYS
            hgrow = ALWAYS
            fitToParentSize()

            simpleAnimeAddition {
                finishedTasksProperty = finishedTasks
                numberOfTasksProperty = tasks
                onAdd = { entry ->
                    manamiAccess.addWatchListEntry(entry)
                }
            }

            animeTable<WatchListEntry> {
                manamiApp = manamiAccess
                withToWatchListButton = false
                withHideButton = false
                withDeleteButton = true
                onDelete = { entry -> manamiAccess.removeWatchListEntry(entry) }
                items = entries
                hostServicesInstance = hostServices
            }
        }
    }
}