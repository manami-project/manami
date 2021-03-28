package io.github.manamiproject.manami.gui.animelist

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.gui.*
import io.github.manamiproject.manami.gui.components.ApplicationBlockedLoading
import io.github.manamiproject.manami.gui.components.animeTable
import io.github.manamiproject.modb.core.extensions.EMPTY
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.Priority.ALWAYS
import javafx.scene.text.FontWeight.EXTRA_BOLD
import javafx.stage.Modality.APPLICATION_MODAL
import javafx.stage.StageStyle.UNDECORATED
import javafx.stage.StageStyle.UTILITY
import tornadofx.*
import java.net.URI

class AnimeListView : View() {

    private val manamiAccess: ManamiAccess by inject()
    private val loadingIndicator = find<ApplicationBlockedLoading>()

    private val entries: ObjectProperty<ObservableList<AnimeListEntry>> = SimpleObjectProperty(
        FXCollections.observableArrayList(manamiAccess.animeList())
    )

    init {
        subscribe<AddAnimeListEntryGuiEvent> { event ->
            entries.value.addAll(event.entries)
        }
        subscribe<RemoveAnimeListEntryGuiEvent> { event ->
            entries.value.removeAll(event.entries)
        }
        subscribe<AnimeSearchEntryFoundGuiEvent> { event ->
            loadingIndicator.close()
            find<AnimeForm>().apply {
                selectedTitle.set(event.anime.title)
                selectedEpisodes.set(event.anime.episodes)
                selectedType.set(event.anime.type.toString())
                selectedThumbnail.set(event.anime.thumbnail.toString())
                selectedLink.set(event.anime.sources.first().toString())
            }.openModal(stageStyle = UTILITY, APPLICATION_MODAL)
        }
        subscribe<AnimeSearchFinishedGuiEvent> {
            loadingIndicator.close()
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
                    text = "add"
                    isDefaultButton = true
                    action {
                        if (txtUrl.text.isBlank()) {
                            txtUrl.text = EMPTY
                        }

                        if (txtUrl.text.isNotEmpty()) {
                            loadingIndicator.openModal(UNDECORATED)
                            manamiAccess.find(URI(txtUrl.text))
                            txtUrl.text = EMPTY
                        }
                    }
                }

                button {
                    text = "custom"
                    isDefaultButton = false
                    isCancelButton = true
                    action {
                        if (txtUrl.text.isBlank()) {
                            txtUrl.text = EMPTY
                        }

                        find<AnimeForm>().openModal(stageStyle = UTILITY, APPLICATION_MODAL)
                    }
                }
            }

            animeTable<AnimeListEntry> {
                manamiApp = manamiAccess
                items = entries
                withToWatchListButton = false
                withToIgnoreListButton = false
                withHideButton = false
                withEditButton = true
                onEdit = {
                    find<AnimeForm>().apply {
                        selectedTitle.set(it.title)
                        selectedEpisodes.set(it.episodes)
                        selectedType.set(it.type.toString())
                        selectedThumbnail.set(it.thumbnail.toString())
                        selectedLink.set(it.link.toString())
                        selectedLocation.set((it.location.toString()))
                        isEdit.set(true)
                    }.openModal(stageStyle = UTILITY, APPLICATION_MODAL)
                }
                withDeleteButton = true
                onDelete = {
                    manamiAccess.removeAnimeListEntry(it)
                }
            }
        }
    }
}
