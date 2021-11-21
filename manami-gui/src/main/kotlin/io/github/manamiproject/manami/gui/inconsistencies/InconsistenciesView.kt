package io.github.manamiproject.manami.gui.inconsistencies

import io.github.manamiproject.manami.app.inconsistencies.InconsistenciesSearchConfig
import io.github.manamiproject.manami.app.inconsistencies.animelist.episodes.EpisodeDiff
import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import io.github.manamiproject.manami.gui.*
import io.github.manamiproject.manami.gui.events.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos.CENTER
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority.ALWAYS
import javafx.stage.StageStyle.UTILITY
import tornadofx.*

class InconsistenciesView : View() {

    private val manamiAccess: ManamiAccess by inject()

    private val animeListDeadEntries = SimpleBooleanProperty(false)
    private val animeListMetaData = SimpleBooleanProperty(false)
    private val animeListEpisodes = SimpleBooleanProperty(false)
    private val metaDataSelected = SimpleBooleanProperty(false)
    private val deadEntriesSelected = SimpleBooleanProperty(false)

    private val progressIndicatorVisibleProperty = SimpleBooleanProperty(false)
    private val progressIndicatorValueProperty = SimpleDoubleProperty(0.0)

    private val items: MutableMap<String, HBox> = mutableMapOf()
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
            items[META_DATA_RESULT_ENTRY] = messageBox
            activeItems.add(messageBox)
        }
        subscribe<DeadEntriesInconsistenciesResultGuiEvent> { event ->
            val messageBox = createDeadEntriesMessageBox(event)
            items[DEAD_ENTRIES_RESULT_ENTRY] = messageBox
            activeItems.add(messageBox)
        }
        subscribe<AnimeListMetaDataInconsistenciesResultGuiEvent> { event ->
            val messageBox = createAnimeListMetaDataDiffMessageBox(event)
            items["$ANIME_LIST_META_DATA_PREFIX-${event.diff.currentEntry.link}"] = messageBox
            activeItems.add(messageBox)
        }
        subscribe<AnimeListDeadEntriesInconsistenciesResultGuiEvent> { event ->
            event.entries.forEach {
                val messageBox = createAnimeListDeadEntryMessageBox(it)
                items["$ANIME_LIST_DEAD_ENTRIES_PREFIX-${it.link}"] = messageBox
                activeItems.add(messageBox)
            }
        }
        subscribe<AnimeListEpisodesInconsistenciesResultGuiEvent> { event ->
            event.entries.forEach {
                val messageBox = createAnimeListEpisodesMessageBox(it)
                items["${ANIME_LIST_EPISODES_PREFIX}-${it.animeListEntry.link}"] = messageBox
                activeItems.add(messageBox)
            }
        }
        subscribe<FileOpenedGuiEvent> {
            items.clear()
            activeItems.clear()
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
                        fieldset("AnimeList") {
                            field("MetaData") {
                                checkbox {
                                    selectedProperty().bindBidirectional(animeListMetaData)
                                }
                            }
                            field("DeadEntries") {
                                checkbox {
                                    selectedProperty().bindBidirectional(animeListDeadEntries)
                                }
                            }
                            field("Episodes") {
                                checkbox {
                                    selectedProperty().bindBidirectional(animeListEpisodes)
                                }
                            }
                        }
                        fieldset("WatchList / IgnoreList") {
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
                        }
                        field {
                            button("Start") {
                                isDefaultButton = true

                                action {
                                    val options = listOf(
                                        !animeListMetaData.value,
                                        !animeListDeadEntries.value,
                                        !animeListEpisodes.value,
                                        !metaDataSelected.value,
                                        !deadEntriesSelected.value,
                                    )
                                    if (options.all { !it }) {
                                        return@action
                                    }

                                    activeItems.clear()
                                    items.clear()
                                    progressIndicatorVisibleProperty.set(true)
                                    runAsync {
                                        manamiAccess.findInconsistencies(
                                            InconsistenciesSearchConfig(
                                                checkAnimeListMetaData = animeListMetaData.value,
                                                checkAnimeListDeadEnties = animeListDeadEntries.value,
                                                checkAnimeListEpisodes = animeListEpisodes.value,
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
                        field("Found ${event.numberOfAffectedEntries} entries in WatchList and IgnoreList with outdated meta data.") {
                            button("fix") {
                                action {
                                    activeItems.remove(items[META_DATA_RESULT_ENTRY])
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
                        field("Found ${event.numberOfAffectedEntries} dead entries in WatchList and IgnoreList.") {
                            button("fix") {
                                action {
                                    activeItems.remove(items[DEAD_ENTRIES_RESULT_ENTRY])
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

    private fun createAnimeListMetaDataDiffMessageBox(event: AnimeListMetaDataInconsistenciesResultGuiEvent): HBox {
        return HBox().apply {
            add(
                form {
                    fieldset {
                        field("Found difference in AnimeList entry ${event.diff.currentEntry.title}") {
                            button("show diff") {
                                action {
                                    find<DiffFragment>().apply {
                                        diff.set(event.diff)
                                    }.openModal(stageStyle = UTILITY)
                                }
                            }
                            button("fix") {
                                action {
                                    activeItems.remove(items["$ANIME_LIST_META_DATA_PREFIX-${event.diff.currentEntry.link}"])
                                    runAsync {
                                        manamiAccess.fixAnimeListEntryMetaDataInconsistencies(event.diff)
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    private fun createAnimeListDeadEntryMessageBox(animeListEntry: AnimeListEntry): HBox {
        return HBox().apply {
            add(
                form {
                    fieldset {
                        field("Found dead entry in AnimeList: ${animeListEntry.title} ( ${animeListEntry.link} )") {
                            button("hide") {
                                action {
                                    activeItems.remove(items["$ANIME_LIST_DEAD_ENTRIES_PREFIX-${animeListEntry.link}"])
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    private fun createAnimeListEpisodesMessageBox(episodeDiff: EpisodeDiff): HBox {
        return HBox().apply {
            add(
                form {
                    fieldset {
                        field("${episodeDiff.animeListEntry.title} ( ${episodeDiff.animeListEntry.link} ) expects ${episodeDiff.animeListEntry.episodes} files, but found ${episodeDiff.numberOfFiles}") {
                            button("hide") {
                                action {
                                    activeItems.remove(items["$ANIME_LIST_EPISODES_PREFIX-${episodeDiff.animeListEntry.link}"])
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    companion object {
        private const val ANIME_LIST_DEAD_ENTRIES_PREFIX = "anime-list-dead-entries"
        private const val ANIME_LIST_META_DATA_PREFIX = "anime-list-meta-data"
        private const val ANIME_LIST_EPISODES_PREFIX = "anime-list-episodes"
        private const val META_DATA_RESULT_ENTRY = "meta-data-result-event"
        private const val DEAD_ENTRIES_RESULT_ENTRY = "dead-entries-result-event"
    }
}