package io.github.manamiproject.manami.gui.animelist

import io.github.manamiproject.manami.app.lists.Link
import io.github.manamiproject.manami.app.lists.NoLink
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.gui.ManamiAccess
import io.github.manamiproject.manami.gui.components.PathChooser
import io.github.manamiproject.modb.core.extensions.EMPTY
import io.github.manamiproject.modb.core.models.Anime
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos.CENTER_RIGHT
import javafx.scene.Parent
import org.controlsfx.validation.ValidationResult
import org.controlsfx.validation.ValidationSupport
import org.controlsfx.validation.Validator
import tornadofx.*
import java.net.URI
import kotlin.Int.Companion.MAX_VALUE

class AnimeForm: Fragment() {

    private val manamiAccess: ManamiAccess by inject()
    private val textFieldMinWidth = SimpleDoubleProperty(500.0)
    private val validationSupport = ValidationSupport()
    private val validator = Validator<String> { control, value ->
        return@Validator ValidationResult.fromErrorIf(control, "Required. Must not be empty", value.isBlank())
    }

    val selectedTitle = SimpleStringProperty(EMPTY)
    val selectedEpisodes = SimpleIntegerProperty(1)
    val selectedType = SimpleStringProperty("TV")
    val selectedThumbnail = SimpleStringProperty("https://cdn.myanimelist.net/images/qm_50.gif")
    val selectedLink = SimpleStringProperty(EMPTY)
    val selectedLocation = SimpleStringProperty(EMPTY)
    val isEdit = SimpleBooleanProperty(false)

    override val root: Parent = pane {
        form {
            fieldset {
                vbox(20.0) {
                    vbox {
                        field("Title") {
                            textfield(selectedTitle) {
                                minWidthProperty().bindBidirectional(textFieldMinWidth)
                            }.apply {
                                validationSupport.registerValidator( this, true, validator )
                            }
                        }

                        field("Episodes") {
                            spinner(min = 1, max = MAX_VALUE, enableScroll = true, property = selectedEpisodes)
                        }

                        field("Type") {
                            combobox<String>(selectedType) {
                                items = FXCollections.observableArrayList(Anime.Type.values().map { it.toString() })
                            }
                        }

                        field("Thumbnail") {
                            textfield(selectedThumbnail) {
                                minWidthProperty().bindBidirectional(textFieldMinWidth)
                            }
                        }

                        field("Link") {
                            textfield(selectedLink) {
                                minWidthProperty().bindBidirectional(textFieldMinWidth)
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

                        button("save") {
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
                thumbnail = if (selectedThumbnail.get().trim().isNotBlank()) URI(selectedThumbnail.get()) else URI("https://cdn.myanimelist.net/images/qm_50.gif"),
                link = if (selectedLink.get().trim().isNotBlank()) Link(selectedLink.get().trim()) else NoLink,
                location = URI(selectedLocation.get()),
            )

            if (isEdit.get()) {
            } else {
                manamiAccess.addAnimeListEntry(entry)
            }
        }
    }
}