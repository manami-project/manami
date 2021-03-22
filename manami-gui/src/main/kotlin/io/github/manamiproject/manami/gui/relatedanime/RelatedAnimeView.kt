package io.github.manamiproject.manami.gui.relatedanime

import io.github.manamiproject.manami.gui.*
import io.github.manamiproject.manami.gui.components.animeTable
import io.github.manamiproject.manami.gui.components.simpleServiceStart
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class RelatedAnimeView : View() {

    private val manamiAccess: ManamiAccess by inject()
    private val finishedTasks: SimpleIntegerProperty = SimpleIntegerProperty(0)
    private val tasks: SimpleIntegerProperty = SimpleIntegerProperty(0)

    private val entries: ObjectProperty<ObservableList<BigPicturedAnimeEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList()
    )

    init {
        subscribe<AnimeListRelatedAnimeFoundGuiEvent> { event ->
            entries.value.add(BigPicturedAnimeEntry(event.anime))
        }
        subscribe<AnimeListRelatedAnimeStatusGuiEvent> { event ->
            finishedTasks.set(event.finishedChecking)
            tasks.set(event.toBeChecked)

            if (event.finishedChecking == 1) {
                entries.get().clear()
            }
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

            simpleServiceStart {
                finishedTasksProperty.bindBidirectional(finishedTasks)
                numberOfTasksProperty.bindBidirectional(tasks)
                onStart = {
                    entries.get().clear()
                    manamiAccess.findRelatedAnimeForAnimeList()
                }
            }

            animeTable<BigPicturedAnimeEntry> {
                manamiApp = manamiAccess
                items = entries
            }
        }
    }
}