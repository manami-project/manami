package io.github.manamiproject.manami.gui.ignorelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.gui.*
import io.github.manamiproject.manami.gui.components.animeTable
import io.github.manamiproject.manami.gui.components.simpleAnimeAddition
import io.github.manamiproject.manami.gui.components.simpleServiceStart
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class IgnoreListView : View() {

    private val manamiAccess: ManamiAccess by inject()

    private val finishedAddingEntriesTasks: SimpleIntegerProperty = SimpleIntegerProperty(0)
    private val addingEntriesTasks: SimpleIntegerProperty = SimpleIntegerProperty(0)

    private val finishedRelatedAnimeTasks: SimpleIntegerProperty = SimpleIntegerProperty(0)
    private val relatedAnimeTasks: SimpleIntegerProperty = SimpleIntegerProperty(0)

    private val entries: ObjectProperty<ObservableList<IgnoreListEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList()
    )

    init {
        subscribe<AddIgnoreListStatusUpdateGuiEvent> { event ->
            finishedAddingEntriesTasks.set(event.finishedTasks)
            addingEntriesTasks.set(event.tasks)
        }
        subscribe<IgnoreListRelatedAnimeFoundGuiEvent> { event ->
            entries.value.add(IgnoreListEntry(event.anime))
        }
        subscribe<IgnoreListRelatedAnimeStatusGuiEvent> { event ->
            finishedRelatedAnimeTasks.set(event.finishedChecking)
            relatedAnimeTasks.set(event.toBeChecked)

            if (event.finishedChecking == 1) {
                entries.get().clear()
            }
        }
        subscribe<AddAnimeListEntryGuiEvent> { event ->
            val uris = event.entries.map { it.link }.filterIsInstance<Link>().map { it.uri }.toSet()
            entries.get().removeIf { uris.contains(it.link.uri) } // TODO: change to conversion to BigPictureEntry as soon as animeListEntries have a thumbnail
        }
        subscribe<AddWatchListEntryGuiEvent> { event ->
            entries.get().removeAll(event.entries.map { IgnoreListEntry(it) })
        }
        subscribe<AddIgnoreListEntryGuiEvent> { event ->
            entries.get().removeAll(event.entries)
        }
        subscribe<FileOpenedGuiEvent> {
            entries.get().clear()
        }
    }

    override val root = pane {

        vbox {
            vgrow = ALWAYS
            hgrow = ALWAYS
            fitToParentSize()

            vbox {
                simpleAnimeAddition {
                    finishedTasksProperty = finishedAddingEntriesTasks
                    numberOfTasksProperty = addingEntriesTasks
                    onAdd = { entry ->
                        manamiAccess.addIgnoreListEntry(entry)
                    }
                }

                simpleServiceStart {
                    finishedTasksProperty = finishedRelatedAnimeTasks
                    numberOfTasksProperty = relatedAnimeTasks
                    onStart = {
                        entries.get().clear()
                        manamiAccess.findRelatedAnimeForIgnoreList()
                    }
                }
            }

            animeTable<IgnoreListEntry> {
                manamiApp = manamiAccess
                items = entries
            }
        }
    }
}