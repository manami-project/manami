package io.github.manamiproject.manami.gui.search.file

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.app.lists.ignorelist.IgnoreListEntry
import io.github.manamiproject.manami.app.lists.watchlist.WatchListEntry
import io.github.manamiproject.manami.gui.events.FileSearchAnimeListResultsGuiEvent
import io.github.manamiproject.manami.gui.events.FileSearchIgnoreListResultsGuiEvent
import io.github.manamiproject.manami.gui.events.FileSearchWatchListResultsGuiEvent
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.components.animeTable
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Side.RIGHT
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class FileSearchView : View() {

    private val manamiAccess: ManamiAccess by inject()

    private val animeListTitleProperty = SimpleStringProperty("AnimeList")
    private val animeListResultsExpanded = SimpleBooleanProperty(false)
    private val animeListResults: ObjectProperty<ObservableList<AnimeListEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList()
    )

    private val watchListTitleProperty = SimpleStringProperty("WatchList")
    private val watchListResultsExpanded = SimpleBooleanProperty(false)
    private val watchListResults: ObjectProperty<ObservableList<WatchListEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList()
    )

    private val ignoreListTitleProperty = SimpleStringProperty("IgnoreList")
    private val ignoreListResultsExpanded = SimpleBooleanProperty(false)
    private val ignoreListResults: ObjectProperty<ObservableList<IgnoreListEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList()
    )

    init {
        animeListResults.get().addListener(ListChangeListener {
            animeListTitleProperty.set(createAnimeListTitle(animeListResults.get().size))
        })
        subscribe<FileSearchAnimeListResultsGuiEvent> { event ->
            animeListTitleProperty.set(createAnimeListTitle(event.anime.size))
            animeListResults.get().clear()
            event.anime.forEach { animeListResults.get().add(it) }
            animeListResultsExpanded.set(event.anime.isNotEmpty())
        }

        watchListResults.get().addListener(ListChangeListener {
            watchListTitleProperty.set(createWatchListTitle(watchListResults.get().size))
        })
        subscribe<FileSearchWatchListResultsGuiEvent> { event ->
            watchListTitleProperty.set(createWatchListTitle(event.anime.size))
            watchListResults.get().clear()
            event.anime.forEach { watchListResults.get().add(it) }
            watchListResultsExpanded.set(event.anime.isNotEmpty() && !animeListResultsExpanded.get())
        }

        ignoreListResults.get().addListener(ListChangeListener {
            ignoreListTitleProperty.set(createIgnoreListTitle(ignoreListResults.get().size))
        })
        subscribe<FileSearchIgnoreListResultsGuiEvent> { event ->
            ignoreListTitleProperty.set(createIgnoreListTitle(event.anime.size))
            ignoreListResults.get().clear()
            event.anime.forEach { ignoreListResults.get().add(it) }
            ignoreListResultsExpanded.set(event.anime.isNotEmpty() && !animeListResultsExpanded.get() && !watchListResultsExpanded.get())
        }
    }

    override val root = pane {

        drawer(side = RIGHT, multiselect = false) {
            vgrow = ALWAYS
            hgrow = ALWAYS
            fitToParentSize()

            item(animeListTitleProperty) {
                expandedProperty.bindBidirectional(animeListResultsExpanded)

                animeTable<AnimeListEntry> {
                    manamiApp = manamiAccess
                    items = animeListResults
                    withToWatchListButton = false
                    withToIgnoreListButton = false
                    withHideButton = false
                    withDeleteButton = false
                    hostServicesInstance = hostServices
                }
            }

            item(watchListTitleProperty) {
                expandedProperty.bindBidirectional(watchListResultsExpanded)

                animeTable<WatchListEntry> {
                    manamiApp = manamiAccess
                    items = watchListResults
                    withToWatchListButton = false
                    withToIgnoreListButton = false
                    withHideButton = false
                    withDeleteButton = false
                    hostServicesInstance = hostServices
                }
            }

            item(ignoreListTitleProperty) {
                expandedProperty.bindBidirectional(ignoreListResultsExpanded)

                animeTable<IgnoreListEntry> {
                    manamiApp = manamiAccess
                    items = ignoreListResults
                    withToWatchListButton = false
                    withToIgnoreListButton = false
                    withHideButton = false
                    withDeleteButton = true
                    onDelete = {
                        manamiAccess.removeIgnoreListEntry(it)
                    }
                    hostServicesInstance = hostServices
                }
            }
        }
    }

    private fun createAnimeListTitle(numberOfEntries: Int) = "AnimeList ($numberOfEntries)"

    private fun createWatchListTitle(numberOfEntries: Int) = "WatchList ($numberOfEntries)"

    private fun createIgnoreListTitle(numberOfEntries: Int) = "IgnoreList ($numberOfEntries)"
}