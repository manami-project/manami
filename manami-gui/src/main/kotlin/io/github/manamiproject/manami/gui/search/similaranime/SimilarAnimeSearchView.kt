package io.github.manamiproject.manami.gui.search.similaranime

import io.github.manamiproject.manami.gui.BigPicturedAnimeEntry
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.components.animeTable
import io.github.manamiproject.manami.gui.events.FileOpenedGuiEvent
import io.github.manamiproject.manami.gui.events.SimilarAnimeFoundGuiEvent
import io.github.manamiproject.manami.gui.events.SimilarAnimeSearchFinishedGuiEvent
import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.text.FontWeight.EXTRA_BOLD
import tornadofx.*
import java.net.URI

class SimilarAnimeSearchView : View() {

    private val manamiAccess: ManamiAccess by inject()

    private val isProgessIndicatorVisible = SimpleBooleanProperty(false)

    private val entries: ObjectProperty<ObservableList<BigPicturedAnimeEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList()
    )

    init {
        subscribe<SimilarAnimeSearchFinishedGuiEvent> {
            isProgessIndicatorVisible.set(false)
        }
        subscribe<SimilarAnimeFoundGuiEvent> { event ->
            entries.get().addAll(event.entries.map { BigPicturedAnimeEntry(it) })
            isProgessIndicatorVisible.set(false)
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

            hbox {
                hgrow = ALWAYS
                alignment = CENTER
                padding = Insets(10.0)
                spacing = 5.0

                label {
                    text = "URL"
                    style {
                        fontWeight = EXTRA_BOLD
                    }
                }

                val txtUrl = textfield {
                    promptText = "https://myanimelist.net/anime/1535"
                    minWidth = 250.0
                }

                button {
                    text = "search"
                    isDefaultButton = true

                    isProgessIndicatorVisible.addListener(ChangeListener { _, oldValue, newValue ->
                        if (!oldValue && newValue) {
                            disableProperty().set(true)
                        } else if (oldValue && !newValue) {
                            disableProperty().set(false)
                        }
                    })

                    action {
                        if (txtUrl.text.isBlank()) {
                            txtUrl.text = EMPTY
                        }

                        if (txtUrl.text.isNotEmpty()) {
                            isProgessIndicatorVisible.set(true)
                            manamiAccess.findSimilarAnime(URI(txtUrl.text.trim()))
                            txtUrl.text = EMPTY
                        }
                    }
                }

                progressindicator {
                    visibleProperty().bindBidirectional(isProgessIndicatorVisible)
                    progress = -1.0
                    maxHeightProperty().set(20.0)
                    maxWidthProperty().set(20.0)
                }
            }

            animeTable<BigPicturedAnimeEntry> {
                manamiApp = manamiAccess
                withSortableTitle = false
                items = entries
            }
        }
    }
}