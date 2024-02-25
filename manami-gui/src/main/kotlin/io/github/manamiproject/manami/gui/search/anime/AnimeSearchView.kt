package io.github.manamiproject.manami.gui.search.anime

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.search.SearchType
import io.github.manamiproject.manami.app.search.SearchType.AND
import io.github.manamiproject.manami.app.search.SearchType.OR
import io.github.manamiproject.manami.gui.*
import io.github.manamiproject.manami.gui.components.animeTable
import io.github.manamiproject.manami.gui.components.simpleServiceStart
import io.github.manamiproject.manami.gui.events.*
import io.github.manamiproject.modb.core.models.Anime
import io.github.manamiproject.modb.core.models.Anime.Status.*
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*
import tornadofx.controlsfx.listSelectionView

class AnimeSearchView: View() {

    private val manamiAccess: ManamiAccess by inject()

    private val availableTags = manamiAccess.availableTags().sorted()
    private val selectedTags = SimpleListProperty<String>(FXCollections.observableArrayList())
    private val selectableTags = SimpleListProperty(FXCollections.observableArrayList(availableTags))
    private val isFinishedSelected = SimpleBooleanProperty(true)
    private val isOngoingSelected = SimpleBooleanProperty(true)
    private val isUpcomingSelected = SimpleBooleanProperty(true)
    private val isUnknownSelected = SimpleBooleanProperty(true)

    private val searchBoxExpanded = SimpleBooleanProperty(true).apply {
        addListener { _, _, newValue ->
            when(newValue) {
                true -> collapsableText.set("collapse")
                false -> collapsableText.set("expand")
            }
        }
    }
    private val collapsableText = SimpleStringProperty("collapse")

    private val selectedMetaDataProvider = SimpleStringProperty(manamiAccess.availableMetaDataProviders().first())
    private val availableMetaDataProvider: ObservableList<String> = FXCollections.observableArrayList(
        manamiAccess.availableMetaDataProviders()
    )

    private val selectedSearchType = SimpleStringProperty(AND.toString())

    private val progressIndicatorVisible = SimpleBooleanProperty(false)

    private val entries: ObjectProperty<ObservableList<BigPicturedAnimeEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList()
    )

    init {
        subscribe<AnimeSearchEntryFoundGuiEvent> { event ->
            entries.value.add(BigPicturedAnimeEntry(event.anime))
        }
        subscribe<AnimeSearchFinishedGuiEvent> {
            progressIndicatorVisible.set(false)
        }
        subscribe<AddAnimeListEntryGuiEvent> { event ->
            val uris = event.entries.map { it.link }.filterIsInstance<Link>().map { it.uri }.toSet()
            entries.get().removeIf { uris.contains(it.link.uri) }
        }
        subscribe<AddWatchListEntryGuiEvent> { event ->
            val uris = event.entries.map { it.link }.map { it.uri }.toSet()
            entries.get().removeIf { uris.contains(it.link.uri) }
        }
        subscribe<AddIgnoreListEntryGuiEvent> { event ->
            val uris = event.entries.map { it.link }.map { it.uri }.toSet()
            entries.get().removeIf { uris.contains(it.link.uri) }
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

            vbox(5) {
                hgrow = ALWAYS
                alignment = CENTER
                padding = Insets(10.0)
                managedProperty().bindBidirectional(searchBoxExpanded)

                vbox(5) {
                    hgrow = ALWAYS
                    alignment = CENTER
                    padding = Insets(10.0)
                    visibleProperty().bindBidirectional(searchBoxExpanded)

                    form {
                        fieldset {
                            field("MetaDataProvider") {
                                combobox<String>(selectedMetaDataProvider) {
                                    items = availableMetaDataProvider
                                }
                            }

                            field("Status") {
                                checkbox("Finished") { selectedProperty().bindBidirectional(isFinishedSelected) }
                                checkbox("Ongoing") { selectedProperty().bindBidirectional(isOngoingSelected) }
                                checkbox("Upcoming") { selectedProperty().bindBidirectional(isUpcomingSelected) }
                                checkbox("Unknown") { selectedProperty().bindBidirectional(isUnknownSelected) }
                            }

                            hbox(20) {
                                field("Tag filter") {
                                    textfield {
                                        textProperty().addListener { _, _, newValue ->
                                            selectableTags.get().clear()
                                            selectableTags.get().addAll(availableTags.filter { it.startsWith(newValue) })
                                        }
                                    }
                                }

                                field("Connect tags by") {
                                    togglebutton(selectedSearchType) {
                                        onAction = EventHandler {
                                            when(selectedSearchType.get()) {
                                                AND.toString() -> selectedSearchType.set(OR.toString())
                                                else -> selectedSearchType.set(AND.toString())
                                            }
                                        }
                                    }
                                }
                            }

                            field("Tags") {
                                listSelectionView<String>(sourceItems = selectableTags, targetItems = selectedTags)
                            }

                            simpleServiceStart {
                                progressIndicatorVisibleProperty.bindBidirectional(progressIndicatorVisible)
                                onStart = {
                                    entries.get().clear()
                                    manamiAccess.findByTag(
                                        tags = selectedTags.toSet(),
                                        metaDataProvider = selectedMetaDataProvider.get(),
                                        searchType = SearchType.of(selectedSearchType.get()),
                                        status = fetchStatusSelection()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            vbox(5) {
                hgrow = ALWAYS
                alignment = CENTER
                padding = Insets(10.0)
                fitToParentSize()

                button {
                    textProperty().bindBidirectional(collapsableText)
                    action {
                        searchBoxExpanded.set(!searchBoxExpanded.get())
                    }
                }
                animeTable<BigPicturedAnimeEntry> {
                    manamiApp = manamiAccess
                    items = entries
                    hostServicesInstance = hostServices
                }
            }
        }
    }

    private fun fetchStatusSelection(): Set<Anime.Status> {
        val ret = mutableSetOf<Anime.Status>()

        if (isFinishedSelected.get()) {
            ret.add(FINISHED)
        }

        if (isOngoingSelected.get()) {
            ret.add(ONGOING)
        }

        if (isUpcomingSelected.get()) {
            ret.add(UPCOMING)
        }

        if (isUnknownSelected.get()) {
            ret.add(UNKNOWN)
        }

        return ret
    }
}