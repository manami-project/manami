package io.github.manamiproject.manami.gui.search.season

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.gui.*
import io.github.manamiproject.manami.gui.components.animeTable
import io.github.manamiproject.manami.gui.components.simpleServiceStart
import io.github.manamiproject.manami.gui.events.*
import io.github.manamiproject.modb.core.anime.AnimeSeason
import javafx.beans.property.*
import javafx.collections.FXCollections.observableArrayList
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*
import java.time.LocalDate

class AnimeSeasonView : View() {

    private val manamiAccess: ManamiAccess by inject()
    private val selectedSeason = SimpleStringProperty(currentSeason())
    private val selectedYear = SimpleIntegerProperty(LocalDate.now().year)
    private val selectedMetaDataProvider = SimpleStringProperty(manamiAccess.availableMetaDataProviders().first())
    private val availableMetaDataProvider: ObservableList<String> = observableArrayList(manamiAccess.availableMetaDataProviders())
    private val progressIndicatorVisible = SimpleBooleanProperty(false)
    private val entries: ObjectProperty<ObservableList<BigPicturedAnimeEntry>> = SimpleObjectProperty(
        observableArrayList()
    )

    init {
        subscribe<AnimeSeasonEntryFoundGuiEvent> { event ->
            entries.value.add(BigPicturedAnimeEntry(event.anime))
        }
        subscribe<AnimeSeasonSearchFinishedGuiEvent> {
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

            vbox {
                hgrow = ALWAYS
                alignment = CENTER
                padding = Insets(10.0)
                spacing = 5.0

                hbox {
                    hgrow = ALWAYS
                    alignment = CENTER
                    padding = Insets(10.0)
                    spacing = 5.0

                    spinner(min = 1907, max = LocalDate.now().year + 5, property = selectedYear)

                    combobox<String>(selectedSeason) {
                        items = observableArrayList("Spring", "Summer", "Fall", "Winter")
                    }

                    combobox<String>(selectedMetaDataProvider) {
                        items = availableMetaDataProvider
                    }
                }

                simpleServiceStart {
                    progressIndicatorVisibleProperty.bindBidirectional(progressIndicatorVisible)
                    onStart = {
                        entries.get().clear()
                        manamiAccess.findSeason(AnimeSeason(AnimeSeason.Season.of(selectedSeason.get()), selectedYear.value), selectedMetaDataProvider.get())
                    }
                }
            }

            animeTable<BigPicturedAnimeEntry> {
                manamiApp = manamiAccess
                items = entries
                hostServicesInstance = hostServices
            }
        }
    }

    private fun currentSeason(): String {
        return when(LocalDate.now().month.value) {
            1, 2, 3 -> "Winter"
            4, 5, 6 -> "Spring"
            7, 8, 9 -> "Summer"
            else -> "Fall"
        }
    }
}