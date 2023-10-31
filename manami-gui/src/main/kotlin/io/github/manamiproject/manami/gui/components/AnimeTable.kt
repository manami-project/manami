package io.github.manamiproject.manami.gui.components

import io.github.manamiproject.manami.app.ManamiApp
import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.gui.GuiCaches
import io.github.manamiproject.manami.gui.ReadOnlyObservableValue
import io.github.manamiproject.manami.gui.components.Alerts.AlertOption.YES
import io.github.manamiproject.manami.gui.extensions.hyperlink
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.models.Anime.Status.UNKNOWN
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Pos.CENTER
import javafx.geometry.Pos.CENTER_LEFT
import javafx.scene.Group
import javafx.scene.control.Hyperlink
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.Priority.NEVER
import tornadofx.*
import java.net.URI

data class AnimeTableConfig<T: AnimeEntry>(
    var manamiApp: ManamiApp? = null,
    var withToWatchListButton: Boolean = true,
    var withToIgnoreListButton: Boolean = true,
    var withHideButton: Boolean = true,
    var withDeleteButton: Boolean = false,
    var onDelete: (T) -> Unit = {},
    var withSortableTitle: Boolean = true,
    var withEditButton: Boolean = false,
    var onEdit: (T) -> Unit = {},
    var items: ObjectProperty<ObservableList<T>> = SimpleObjectProperty(observableListOf()),
)

inline fun <reified T: AnimeEntry> EventTarget.animeTable(config: AnimeTableConfig<T>.() -> Unit): TableView<T> {
    val animeTableConfig = AnimeTableConfig<T>().apply(config)
    requireNotNull(animeTableConfig.manamiApp) { "Parameter manamiApp must be set" }
    val manamiApp: ManamiApp = animeTableConfig.manamiApp!!

    val tableView = TableView<T>().apply {
        style = "-fx-selection-bar: #e1e7f5; -fx-selection-bar-non-focused: #ebebeb;"
        isCache = true
        hgrow = ALWAYS
        vgrow = ALWAYS
        fitToParentSize()
        itemsProperty().bind(animeTableConfig.items)

        columns.addAll(
            TableColumn<T, ImageView>("Image").apply {
                isSortable = false
                isEditable = false
                isResizable = true
                style {
                    alignment = CENTER
                }
                setCellValueFactory { column ->
                    ReadOnlyObservableValue<ImageView> {
                        ImageView((GuiCaches.imageCache.fetch(column.value.thumbnail) as PresentValue).value).apply {
                            isCache = true
                        }
                    }
                }
            },
            TableColumn<T, Hyperlink>("Title").apply {
                isSortable = animeTableConfig.withSortableTitle
                isEditable = false
                isResizable = true
                style {
                    alignment = CENTER_LEFT
                }
                comparator = Comparator<Hyperlink> { o1, o2 -> o1.text.compareTo(o2.text, true) }
                columnResizePolicy = CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
                setCellValueFactory { column ->
                    ReadOnlyObservableValue<Hyperlink> {
                        hyperlink {
                            title = column.value.title
                            uri = if(column.value.link is NoLink) URI(EMPTY) else column.value.link.asLink().uri
                            isDisable = column.value.link is NoLink
                            animeStatus = if (T::class == WatchListEntry::class) (column.value as WatchListEntry).status else UNKNOWN
                        }
                    }
                }
            },
            TableColumn<T, Group>("Actions").apply {
                isSortable = false
                isEditable = false
                isResizable = true
                style {
                    alignment = CENTER
                }
                setCellValueFactory { cellValueFactory ->
                    ReadOnlyObservableValue<Group> {
                        group {
                            hbox {
                                spacing = 5.0
                                hgrow = NEVER
                                vgrow = NEVER

                                if (animeTableConfig.withToWatchListButton) {
                                    button("watch") {
                                        action {
                                            animeTableConfig.items.get().remove(cellValueFactory.value)
                                            runAsync { manamiApp.addWatchListEntry(setOf(cellValueFactory.value.link.asLink().uri)) }
                                        }
                                    }
                                }

                                if (animeTableConfig.withToIgnoreListButton) {
                                    button("ignore") {
                                        action {
                                            animeTableConfig.items.get().remove(cellValueFactory.value)
                                            runAsync { manamiApp.addIgnoreListEntry(setOf(cellValueFactory.value.link.asLink().uri)) }
                                        }
                                    }
                                }

                                if (animeTableConfig.withEditButton) {
                                    button("Edit") {
                                        action {
                                            animeTableConfig.onEdit.invoke(cellValueFactory.value)
                                        }
                                    }
                                }

                                if (animeTableConfig.withDeleteButton) {
                                    button("Delete") {
                                        action {
                                            if (Alerts.removeEntry(cellValueFactory.value.title) == YES) {
                                                animeTableConfig.items.get().remove(cellValueFactory.value)
                                                runAsync { animeTableConfig.onDelete.invoke(cellValueFactory.value) }
                                            }
                                        }
                                    }
                                }

                                if (animeTableConfig.withHideButton) {
                                    button("hide") {
                                        action { animeTableConfig.items.get().remove(cellValueFactory.value) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
    this.addChildIfPossible(tableView)

    return tableView
}