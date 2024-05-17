package io.github.manamiproject.manami.gui.migration

import io.github.manamiproject.manami.app.cache.PresentValue
import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.gui.GuiCaches
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.ReadOnlyObservableValue
import io.github.manamiproject.manami.gui.components.Alerts.AlertOption.YES
import io.github.manamiproject.manami.gui.components.simpleServiceStart
import io.github.manamiproject.manami.gui.events.FileOpenedGuiEvent
import io.github.manamiproject.manami.gui.events.MetaDataMigrationResultGuiEvent
import io.github.manamiproject.manami.gui.events.MetaDataProviderMigrationGuiEvent
import io.github.manamiproject.manami.gui.extensions.hyperlink
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Pos.CENTER
import javafx.geometry.Pos.CENTER_LEFT
import javafx.scene.control.Hyperlink
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*
import java.awt.Desktop
import java.net.URI

class MetaDataProviderMigrationView : View() {

    private val manamiAccess: ManamiAccess by inject()
    private val tasks: SimpleIntegerProperty = SimpleIntegerProperty(0)
    private val finishedTasks: SimpleIntegerProperty = SimpleIntegerProperty(0)
    private val isProgressIndicatorVisible = SimpleBooleanProperty(false)

    private val availableMetaDataProvider: ObservableList<String> = FXCollections.observableArrayList(
        manamiAccess.availableMetaDataProviders()
    )
    private val selectedMetaDataProviderFrom = SimpleStringProperty(manamiAccess.availableMetaDataProviders().first())
    private val selectedMetaDataProviderTo = SimpleStringProperty(manamiAccess.availableMetaDataProviders().first())

    private val items = SimpleObjectProperty(observableListOf<MigrationTableEntry>())

    init {
        subscribe<FileOpenedGuiEvent> {
            items.get().clear()
        }
        subscribe<MetaDataProviderMigrationGuiEvent> {
            tasks.set(it.tasks)
            finishedTasks.set(it.finishedTasks)
        }
        subscribe<MetaDataMigrationResultGuiEvent> { event ->
            val itemCatcher = mutableListOf<MigrationTableEntry>()

            event.animeListEntiresMultipleMappings.forEach { (animeListEntry, options) ->
                itemCatcher.add(
                    MigrationTableEntry(
                        thumbnail = animeListEntry.thumbnail,
                        title = animeListEntry.title,
                        currentLink = animeListEntry.link.asLink(),
                        animeListEntry = animeListEntry,
                        alternatives = options
                    )
                )
            }

            event.watchListEntiresMultipleMappings.forEach { (watchListEntry, options) ->
                itemCatcher.add(
                    MigrationTableEntry(
                        thumbnail = watchListEntry.thumbnail,
                        title = watchListEntry.title,
                        currentLink = watchListEntry.link.asLink(),
                        watchListEntry = watchListEntry,
                        alternatives = options,
                    )
                )
            }

            event.ignoreListEntiresMultipleMappings.forEach { (ignoreListEntry, options) ->
                itemCatcher.add(
                    MigrationTableEntry(
                        thumbnail = ignoreListEntry.thumbnail,
                        title = ignoreListEntry.title,
                        currentLink = ignoreListEntry.link.asLink(),
                        ignoreListEntry = ignoreListEntry,
                        alternatives = options,
                    )
                )
            }

            items.set(itemCatcher.toObservable())

            if (event.animeListMappings.isNotEmpty() || event.watchListMappings.isNotEmpty() || event.ignoreListMappings.isNotEmpty()) {
                val choice = MigrationAlerts.migrateEntries(
                    numberOfEntriesAnimeList = event.animeListMappings.size,
                    numberOfEntriesWatchList = event.watchListMappings.size,
                    numberOfEntriesIgnoreList = event.ignoreListMappings.size,
                )

                if (choice == YES) {
                    runAsync {
                        manamiAccess.migrate(
                            animeListMappings = event.animeListMappings,
                            watchListMappings = event.watchListMappings,
                            ignoreListMappings = event.ignoreListMappings,
                        )
                    }
                }
            }

            if (event.animeListEntriesWithoutMapping.isNotEmpty() || event.watchListEntriesWithoutMapping.isNotEmpty() || event.ignoreListEntriesWithoutMapping.isNotEmpty()) {
                val choice = MigrationAlerts.removeUnmappedEntries(
                    numberOfEntriesAnimeList = event.animeListEntriesWithoutMapping.size,
                    numberOfEntriesWatchList = event.watchListEntriesWithoutMapping.size,
                    numberOfEntriesIgnoreList = event.ignoreListEntriesWithoutMapping.size,
                )

                if (choice == YES) {
                    runAsync {
                        manamiAccess.removeUnmapped(
                            animeListEntriesWithoutMapping = event.animeListEntriesWithoutMapping,
                            watchListEntriesWithoutMapping = event.watchListEntriesWithoutMapping,
                            ignoreListEntriesWithoutMapping = event.ignoreListEntriesWithoutMapping,
                        )
                    }
                }
            }

            isProgressIndicatorVisible.set(false)
        }
    }

    override val root = pane {
        vbox {
            vgrow = ALWAYS
            hgrow = ALWAYS
            fitToParentSize()

                form {
                    fieldset {
                        hbox(20.0) {
                            alignment = CENTER
                            field("from") {
                                combobox<String>(selectedMetaDataProviderFrom) {
                                    items = availableMetaDataProvider
                                }
                            }
                            field("to") {
                                combobox<String>(selectedMetaDataProviderTo) {
                                    items = availableMetaDataProvider
                                }
                            }
                        }
                    }
                }

            simpleServiceStart {
                finishedTasksProperty.bindBidirectional(finishedTasks)
                numberOfTasksProperty.bindBidirectional(tasks)
                progressIndicatorVisibleProperty.bindBidirectional(isProgressIndicatorVisible)
                onStart = {
                    if (selectedMetaDataProviderFrom.get() != selectedMetaDataProviderTo.get()) {
                        items.get().clear()
                        manamiAccess.checkMigration(selectedMetaDataProviderFrom.get(), selectedMetaDataProviderTo.get())
                    } else {
                        progressIndicatorVisibleProperty.set(false)
                    }
                }
            }

            add(createTable(items, manamiAccess))
        }
    }
}

private fun createTable(tableItems: SimpleObjectProperty<ObservableList<MigrationTableEntry>>, manamiAccess: ManamiAccess): TableView<MigrationTableEntry> {
    return TableView<MigrationTableEntry>().apply {
        style = "-fx-selection-bar: #e1e7f5; -fx-selection-bar-non-focused: #ebebeb;"
        isCache = true
        hgrow = ALWAYS
        vgrow = ALWAYS
        fitToParentSize()
        itemsProperty().bind(tableItems)

        columns.addAll(
            TableColumn<MigrationTableEntry, ImageView>().apply {
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
            TableColumn<MigrationTableEntry, Hyperlink>("Title").apply {
                isSortable = true
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
                            uri = column.value.currentLink.uri
                            isDisable = false
                        }
                    }
                }
            },
            TableColumn<MigrationTableEntry, HBox>().apply {
                isSortable = false
                isEditable = false
                isResizable = true
                style {
                    alignment = CENTER
                }
                setCellValueFactory { column ->
                    ReadOnlyObservableValue<HBox> {
                        hbox(10.0) {
                            val options = combobox<String> {
                                items = column.value.alternatives.map { it.toString() }.toObservable()
                            }
                            button("open") {
                                action {
                                    if (options.value != null && options.value.neitherNullNorBlank()) {
                                        Desktop.getDesktop().browse(URI(options.value))
                                    }
                                }
                            }
                            button("migrate") {
                                action {
                                    val migrationTableEntry = column.value
                                    tableItems.get().remove(migrationTableEntry)
                                    when {
                                        migrationTableEntry.animeListEntry != null -> manamiAccess.migrate(
                                            animeListMappings = mapOf(migrationTableEntry.animeListEntry to Link(options.value))
                                        )
                                        migrationTableEntry.watchListEntry != null -> manamiAccess.migrate(
                                            watchListMappings = mapOf(migrationTableEntry.watchListEntry to Link(options.value))
                                        )
                                        migrationTableEntry.ignoreListEntry != null -> manamiAccess.migrate(
                                            ignoreListMappings = mapOf(migrationTableEntry.ignoreListEntry to Link(options.value))
                                        )
                                    }
                                }
                            }
                            button("hide") {
                                action { tableItems.get().remove(column.value) }
                            }
                        }
                    }
                }
            },
        )
    }
}