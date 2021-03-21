package io.github.manamiproject.manami.gui.ignorelist

import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.gui.AddIgnoreListEntryGuiEvent
import io.github.manamiproject.manami.gui.AddIgnoreListStatusUpdateGuiEvent
import io.github.manamiproject.manami.gui.AddWatchListStatusUpdateGuiEvent
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.components.animeTable
import io.github.manamiproject.manami.gui.components.simpleAnimeAddition
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class IgnoreListView : View() {

    private val manamiAccess: ManamiAccess by inject()
    private val finishedTasks: SimpleIntegerProperty = SimpleIntegerProperty(0)
    private val tasks: SimpleIntegerProperty = SimpleIntegerProperty(0)

    private val ignoreListEntries: ObjectProperty<ObservableList<IgnoreListEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList()
    )

    init {
        subscribe<AddIgnoreListStatusUpdateGuiEvent> { event ->
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
                    manamiAccess.addIgnoreListEntry(entry)
                }
            }

            animeTable<IgnoreListEntry> {
                items = ignoreListEntries
            }
        }
    }
}