package io.github.manamiproject.manami.gui.inconsistencies

import io.github.manamiproject.manami.app.inconsistencies.animelistmetadata.AnimeListMetaDataDiff
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.models.Episodes
import io.github.manamiproject.modb.core.models.Title
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Parent
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class DiffFragment: Fragment() {

    val diff: SimpleObjectProperty<AnimeListMetaDataDiff> = SimpleObjectProperty<AnimeListMetaDataDiff>().apply {
        onChange {
            if (it == null) {
                return@onChange
            }

            entries.value.add(
                DiffViewerEntry(
                    entryType = "current",
                    title = it.currentEntry.title,
                    type = it.currentEntry.type.toString(),
                    episodes = it.currentEntry.episodes,
                    thumbnail = it.currentEntry.thumbnail.toString(),
                )
            )

            entries.value.add(
                DiffViewerEntry(
                    entryType = "replacement",
                    title = it.replacementEntry.title,
                    type = it.replacementEntry.type.toString(),
                    episodes = it.replacementEntry.episodes,
                    thumbnail = it.replacementEntry.thumbnail.toString(),
                )
            )
        }
    }
    private val entries: ObjectProperty<ObservableList<DiffViewerEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList()
    )

    private val widthProperty = SimpleDoubleProperty()
    private val heightProperty = SimpleDoubleProperty()

    override val root: Parent = pane {
        widthProperty.bindBidirectional(prefWidthProperty())
        heightProperty.bindBidirectional(prefHeightProperty())

        tableview<DiffViewerEntry> {
            hgrow = ALWAYS
            vgrow = ALWAYS
            fitToParentSize()

            itemsProperty().bind(entries)
            widthProperty.bindBidirectional(prefWidthProperty())
            heightProperty.bindBidirectional(prefHeightProperty())

            readonlyColumn(EMPTY, DiffViewerEntry::entryType) {
                sortableProperty().set(false)
            }
            readonlyColumn("Title", DiffViewerEntry::title) {
                sortableProperty().set(false)
            }
            readonlyColumn("Type", DiffViewerEntry::type) {
                sortableProperty().set(false)
            }
            readonlyColumn("Episodes", DiffViewerEntry::episodes) {
                sortableProperty().set(false)
            }
            readonlyColumn("Thumbnail", DiffViewerEntry::thumbnail) {
                sortableProperty().set(false)
            }
        }
    }
}

private data class DiffViewerEntry(
    val entryType: String,
    val title: Title,
    val type: String,
    val episodes: Episodes,
    val thumbnail: String,
)