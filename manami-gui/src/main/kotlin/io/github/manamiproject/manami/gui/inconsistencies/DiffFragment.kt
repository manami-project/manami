package io.github.manamiproject.manami.gui.inconsistencies

import io.github.manamiproject.manami.app.inconsistencies.animelistmetadata.AnimeListMetaDataDiff
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.models.Episodes
import io.github.manamiproject.modb.core.models.Title
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Parent
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class DiffFragment: Fragment() {

    private val entries: ObjectProperty<ObservableList<DiffViewerEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList()
    )

    private val tableViewWidthProperty = SimpleDoubleProperty()
    private val tableViewHeightProperty = SimpleDoubleProperty()

    private val showTitleProperty = SimpleBooleanProperty(false)
    private val showTypeProperty = SimpleBooleanProperty(false)
    private val showEpiosdesProperty = SimpleBooleanProperty(false)
    private val showThumbnailProperty = SimpleBooleanProperty(false)

    val diff: SimpleObjectProperty<AnimeListMetaDataDiff> = SimpleObjectProperty<AnimeListMetaDataDiff>().apply {
        onChange {
            if (it == null) {
                return@onChange
            }

            val current = DiffViewerEntry(
                entryType = "current",
                title = it.currentEntry.title,
                type = it.currentEntry.type.toString(),
                episodes = it.currentEntry.episodes,
                thumbnail = it.currentEntry.thumbnail.toString(),
            )
            entries.value.add(current)

            val replacement = DiffViewerEntry(
                entryType = "replacement",
                title = it.replacementEntry.title,
                type = it.replacementEntry.type.toString(),
                episodes = it.replacementEntry.episodes,
                thumbnail = it.replacementEntry.thumbnail.toString(),
            )
            entries.value.add(replacement)

            showTitleProperty.set(current.title != replacement.title)
            showTypeProperty.set(current.type != replacement.type)
            showEpiosdesProperty.set(current.episodes != replacement.episodes)
            showThumbnailProperty.set(current.thumbnail != replacement.thumbnail)
        }
    }


    override val root: Parent = pane {
        prefWidthProperty().bind(tableViewWidthProperty)
        prefHeightProperty().bind(tableViewHeightProperty)

        tableview<DiffViewerEntry> {
            hgrow = ALWAYS
            vgrow = ALWAYS
            fitToParentSize()

            itemsProperty().bind(entries)
            tableViewWidthProperty.bindBidirectional(prefWidthProperty())
            tableViewHeightProperty.bindBidirectional(prefHeightProperty())

            readonlyColumn(EMPTY, DiffViewerEntry::entryType) {
                sortableProperty().set(false)
            }
            readonlyColumn("Title", DiffViewerEntry::title) {
                sortableProperty().set(false)
                visibleProperty().bindBidirectional(showTitleProperty)
            }
            readonlyColumn("Type", DiffViewerEntry::type) {
                sortableProperty().set(false)
                visibleProperty().bindBidirectional(showTypeProperty)
            }
            readonlyColumn("Episodes", DiffViewerEntry::episodes) {
                sortableProperty().set(false)
                visibleProperty().bindBidirectional(showEpiosdesProperty)
            }
            readonlyColumn("Thumbnail", DiffViewerEntry::thumbnail) {
                sortableProperty().set(false)
                visibleProperty().bindBidirectional(showThumbnailProperty)
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