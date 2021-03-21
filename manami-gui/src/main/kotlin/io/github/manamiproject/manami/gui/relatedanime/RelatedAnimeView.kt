package io.github.manamiproject.manami.gui.relatedanime

import io.github.manamiproject.manami.gui.*
import io.github.manamiproject.manami.gui.components.animeTable
import io.github.manamiproject.manami.gui.components.simpleAnimeAddition
import io.github.manamiproject.manami.gui.components.simpleServiceStart
import io.github.manamiproject.modb.core.models.Anime
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.Pos.CENTER
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
        subscribe<RelatedAnimeFoundGuiEvent> { event ->
            entries.value.add(BigPicturedAnimeEntry(event.anime))
        }
        subscribe<RelatedAnimeStatusGuiEvent> { event ->
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
                onStart = { manamiAccess.findRelatedAnimeForAnimeList() }
            }

            animeTable<BigPicturedAnimeEntry> {
                items = entries
            }
        }
    }
}