package io.github.manamiproject.manami.gui.inconsistencies

import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.gui.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*
import kotlin.reflect.KClass

class InconsistenciesView : View() {

    private val manamiAccess: ManamiAccess by inject()

    private val metaDataSelected = SimpleBooleanProperty(false)
    private val deadEntriesSelected = SimpleBooleanProperty(false)

    private val progressIndicatorVisibleProperty = SimpleBooleanProperty(false)
    private val progressIndicatorValueProperty = SimpleDoubleProperty(0.0)

    private val items: MutableMap<KClass<*>, HBox> = mutableMapOf()
    private val activeItems = FXCollections.observableArrayList<HBox>()

    init {
        subscribe<InconsistenciesProgressGuiEvent> { event ->
            progressIndicatorValueProperty.set(event.finishedTasks.toDouble() / event.numberOfTasks.toDouble())
        }
        subscribe<InconsistenciesCheckFinishedGuiEvent> {
            progressIndicatorVisibleProperty.set(false)
        }
        subscribe<MetaDataInconsistenciesResultGuiEvent> { event ->
            val messageBox = createMetaDataMessageBox(event)
            items[MetaDataInconsistenciesResultGuiEvent::class] = messageBox
            activeItems.add(messageBox)
        }
        subscribe<DeadEntriesInconsistenciesResultGuiEvent> { event ->
            val messageBox = createDeadEntriesMessageBox(event)
            items[DeadEntriesInconsistenciesResultGuiEvent::class] = messageBox
            activeItems.add(messageBox)
        }
    }

    override val root = pane {

        vbox {
            vgrow = ALWAYS
            hgrow = ALWAYS
            fitToParentSize()

            vbox {
                vgrow = ALWAYS
                hgrow = ALWAYS
                alignment = CENTER
                padding = Insets(10.0)
                spacing = 5.0

                form {
                    fieldset {
                        field("MetaData") {
                            checkbox {
                                selectedProperty().bindBidirectional(metaDataSelected)
                            }
                        }
                        field("DeadEntries") {
                            checkbox {
                                selectedProperty().bindBidirectional(deadEntriesSelected)
                            }
                        }
                        field {
                            button("Start") {
                                isDefaultButton = true

                                action {
                                    if (!metaDataSelected.value && !deadEntriesSelected.value) {
                                        return@action
                                    }

                                    activeItems.clear()
                                    items.clear()
                                    progressIndicatorVisibleProperty.set(true)
                                    runAsync {
                                        manamiAccess.findInconsistencies(
                                            InconsistenciesSearchConfig(
                                                checkMetaData = metaDataSelected.value,
                                                checkDeadEntries = deadEntriesSelected.value,
                                            )
                                        )
                                    }
                                }
                            }

                            progressindicator {
                                progressProperty().bindBidirectional(progressIndicatorValueProperty)
                                visibleProperty().bindBidirectional(progressIndicatorVisibleProperty)
                            }
                        }
                    }
                }

                listview(activeItems) {
                    fitToParentSize()
                }
            }
        }
    }

    private fun createMetaDataMessageBox(event: MetaDataInconsistenciesResultGuiEvent): HBox {
        return HBox().apply {
            add(
                form {
                    fieldset {
                        field("Found ${event.numberOfAffectedEntries} entries in watch list and ignore list with outdated meta data.") {
                            button("fix") {
                                action {
                                    activeItems.remove(items[MetaDataInconsistenciesResultGuiEvent::class])
                                    runAsync {
                                        manamiAccess.fixMetaDataInconsistencies()
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    private fun createDeadEntriesMessageBox(event: DeadEntriesInconsistenciesResultGuiEvent): HBox {
        return HBox().apply {
            add(
                form {
                    fieldset {
                        field("Found ${event.numberOfAffectedEntries} dead entries in watch list and ignore list.") {
                            button("fix") {
                                action {
                                    activeItems.remove(items[DeadEntriesInconsistenciesResultGuiEvent::class])
                                    runAsync {
                                        manamiAccess.fixDeadEntryInconsistencies()
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}