package io.github.manamiproject.manami.gui.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.animelist.AnimeFormTrigger.*
import io.github.manamiproject.manami.gui.components.PathChooser
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.extensions.eitherNullOrBlank
import io.github.manamiproject.modb.core.extensions.neitherNullNorBlank
import io.github.manamiproject.modb.core.models.Anime
import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.geometry.Pos.CENTER_RIGHT
import javafx.scene.Parent
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.ValidationSupport
import org.controlsfx.validation.Validator
import tornadofx.*
import java.net.URI
import kotlin.Int.Companion.MAX_VALUE
import kotlin.io.path.Path

class AnimeForm: Fragment() {

    private val manamiAccess: ManamiAccess by inject()
    private val textFieldMinWidth = SimpleDoubleProperty(500.0)
    private val validationSupport = ValidationSupport()
    private val validator = Validator<String> { control, value ->
        return@Validator ValidationResult.fromErrorIf(control, "Required. Must not be empty", value.eitherNullOrBlank())
    }

    private val disableTitleProperty = SimpleBooleanProperty(false)
    private val selectedTitle = SimpleStringProperty(EMPTY)

    private val disableEpisodesProperty = SimpleBooleanProperty(false)
    private val selectedEpisodes = SimpleIntegerProperty(1)

    private val disableTypeProperty = SimpleBooleanProperty(false)
    private val selectedType = SimpleStringProperty("TV")

    private val disableThumbnailProperty = SimpleBooleanProperty(false)
    private val selectedThumbnail = SimpleStringProperty(EMPTY)

    private val disableLinkProperty = SimpleBooleanProperty(false)
    private val selectedLink = SimpleStringProperty(EMPTY)

    private val selectedLocation = SimpleStringProperty(EMPTY)

    val animeProperty = SimpleAnimeProperty().apply {
        addListener { _, _, newValue ->
            if (newValue != null) {
                selectedTitle.set(newValue.title)
                selectedEpisodes.set(newValue.episodes)
                selectedType.set(newValue.type.toString())
                selectedThumbnail.set(newValue.thumbnail.toString())
                selectedLink.set(newValue.sources.first().toString())
            }
        }
    }

    val animeListEntryProperty = SimpleAnimeListEntryProperty().apply {
        addListener { _, _, newValue ->
            if (newValue != null) {
                selectedTitle.set(newValue.title)
                selectedEpisodes.set(newValue.episodes)
                selectedType.set(newValue.type.toString())
                selectedThumbnail.set(newValue.thumbnail.toString())
                selectedLink.set(newValue.link.toString())
                selectedLocation.set(newValue.location.toString())
            }
        }
    }

    val trigger = SimpleAnimeFormTriggerProperty().apply {
        addListener { _, _, newValue ->
            when {
                newValue == CREATE_AUTOMATICALLY || newValue == EDIT && selectedLink.get().neitherNullNorBlank() -> {
                    disableTitleProperty.set(true)
                    disableEpisodesProperty.set(true)
                    disableTypeProperty.set(true)
                    disableThumbnailProperty.set(true)
                    disableLinkProperty.set(true)
                }
                else -> {
                    disableTitleProperty.set(false)
                    disableEpisodesProperty.set(false)
                    disableTypeProperty.set(false)
                    disableThumbnailProperty.set(false)
                    disableLinkProperty.set(false)
                }
            }
        }
    }

    override val root: Parent = pane {
        form {
            fieldset {
                vbox(20.0) {
                    vbox {
                        field("Title") {
                            textfield(selectedTitle) {
                                minWidthProperty().bindBidirectional(textFieldMinWidth)
                                disableProperty().bindBidirectional(disableTitleProperty)
                            }.apply {
                                validationSupport.registerValidator( this, true, validator )
                            }
                        }

                        field("Episodes") {
                            spinner(min = 1, max = MAX_VALUE, enableScroll = true, property = selectedEpisodes) {
                                disableProperty().bindBidirectional(disableEpisodesProperty)
                            }
                        }

                        field("Type") {
                            combobox<String>(selectedType) {
                                items = FXCollections.observableArrayList(Anime.Type.entries.map { it.toString() })
                                disableProperty().bindBidirectional(disableTypeProperty)
                            }
                        }

                        field("Thumbnail") {
                            textfield(selectedThumbnail) {
                                minWidthProperty().bindBidirectional(textFieldMinWidth)
                                disableProperty().bindBidirectional(disableThumbnailProperty)
                            }
                        }

                        field("Link") {
                            textfield(selectedLink) {
                                minWidthProperty().bindBidirectional(textFieldMinWidth)
                                disableProperty().bindBidirectional(disableLinkProperty)
                            }
                        }

                        field("Location") {
                            hbox {
                                textfield(selectedLocation) {
                                    isEditable = false
                                    minWidthProperty().bindBidirectional(textFieldMinWidth)
                                }.apply {
                                    validationSupport.registerValidator( this, true, validator )
                                }
                                button("browse") {
                                    action {
                                        val dir = PathChooser.showBrowseForFolderDialog(currentStage!!)
                                        if (dir != null) {
                                            selectedLocation.set(dir.toAbsolutePath().toString())
                                        }
                                    }
                                }
                            }
                        }
                    }

                    hbox(10.0) {
                        alignment = CENTER_RIGHT

                        button("cancel") {
                            isCancelButton = true
                            action {
                                close()
                            }
                        }

                        button("add") {
                            isDefaultButton = true
                            action {
                                if (!validationSupport.isInvalid) {
                                    save()
                                    close()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun save() {
        runAsync {
            val entry = AnimeListEntry(
                title = selectedTitle.get().trim(),
                episodes = selectedEpisodes.get(),
                type = Anime.Type.valueOf(selectedType.get()),
                thumbnail = if (selectedThumbnail.get().trim().neitherNullNorBlank()) URI(selectedThumbnail.get()) else URI("https://raw.githubusercontent.com/manami-project/anime-offline-database/master/pics/no_pic_thumbnail.png"),
                link = if (selectedLink.get().trim().neitherNullNorBlank()) Link(selectedLink.get().trim()) else NoLink,
                location = Path(selectedLocation.get()),
            )

            if (trigger.value == EDIT) {
                manamiAccess.replaceAnimeListEntry(animeListEntryProperty.value, entry)
            } else {
                manamiAccess.addAnimeListEntry(entry)
            }
        }
    }
}