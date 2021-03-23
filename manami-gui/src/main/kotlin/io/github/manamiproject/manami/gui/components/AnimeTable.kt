package io.github.manamiproject.manami.gui.components

import io.github.manamiproject.manami.app.ManamiApp
import io.github.manamiproject.manami.app.lists.AnimeEntry
import io.github.manamiproject.manami.gui.ReadOnlyObservableValue
import io.github.manamiproject.manami.gui.extensions.hyperlink
import io.github.manamiproject.modb.core.models.Anime
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import javafx.event.EventTarget
import javafx.geometry.Pos.CENTER
import javafx.geometry.Pos.CENTER_LEFT
import javafx.scene.Group
import javafx.scene.control.Hyperlink
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.layout.Priority.NEVER
import tornadofx.*

data class AnimeTableConfig<T: AnimeEntry>(
    var manamiApp: ManamiApp? = null,
    var withToWatchListButton: Boolean = true,
    var withToIgnoreListButton: Boolean = true,
    var withHideButton: Boolean = true,
    var withDeleteButton: Boolean = false,
    var withSortableTitle: Boolean = true,
    var onDelete: (T) -> Unit = {},
    var items: ObjectProperty<ObservableList<T>> = SimpleObjectProperty(observableListOf()),
)

inline fun <reified T: AnimeEntry> EventTarget.animeTable(config: AnimeTableConfig<T>.() -> Unit): TableView<T> {
    val animeTableConfig = AnimeTableConfig<T>().apply(config)
    requireNotNull(animeTableConfig.manamiApp) { "Parameter manamiApp must be set" }
    val manamiApp: ManamiApp = animeTableConfig.manamiApp!!

    val imageColWidth = SimpleDoubleProperty(0.0)
    val actionsColWidth = SimpleDoubleProperty(0.0)
    val columnSpacer = 20.0

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
                prefWidthProperty().bindBidirectional(imageColWidth)
                setCellValueFactory { column ->
                    ReadOnlyObservableValue<ImageView> {
                        val image = Image(column.value.thumbnail.toString(), true)
                        val cachedImageView = ImageView(image).apply {
                            isCache = true
                        }

                        image.widthProperty().addListener { _, oldValue, newValue ->
                            if (oldValue.toDouble() < newValue.toDouble()) {
                                imageColWidth.set(newValue.toDouble() + columnSpacer)
                            }
                        }

                        cachedImageView
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
                columnResizePolicy = CONSTRAINED_RESIZE_POLICY
                setCellValueFactory { column ->
                    ReadOnlyObservableValue<Hyperlink> {
                        hyperlink {
                            title = column.value.title
                            uri = column.value.link.uri
                        }
                    }
                }
            },
            TableColumn<T, Group>("Actions").apply {
                isSortable = false
                isEditable = false
                isResizable = false
                style {
                    alignment = CENTER
                }
                prefWidthProperty().bindBidirectional(actionsColWidth)
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
                                            runAsync { manamiApp.addWatchListEntry(setOf(cellValueFactory.value.link.uri)) }
                                        }
                                    }
                                }

                                if (animeTableConfig.withToIgnoreListButton) {
                                    button("ignore") {
                                        action {
                                            animeTableConfig.items.get().remove(cellValueFactory.value)
                                            runAsync { manamiApp.addIgnoreListEntry(setOf(cellValueFactory.value.link.uri)) }
                                        }
                                    }
                                }

                                if (animeTableConfig.withDeleteButton) {
                                    button("Delete") {
                                        action {
                                            animeTableConfig.items.get().remove(cellValueFactory.value)
                                            runAsync { animeTableConfig.onDelete.invoke(cellValueFactory.value) }
                                        }
                                    }
                                }

                                if (animeTableConfig.withHideButton) {
                                    button("hide") {
                                        action { animeTableConfig.items.get().remove(cellValueFactory.value) }
                                    }
                                }

                                widthProperty().addListener { _, oldValue, newValue ->
                                    if (oldValue.toDouble() < newValue.toDouble()) {
                                        actionsColWidth.set(newValue.toDouble() + columnSpacer)
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