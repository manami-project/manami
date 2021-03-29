package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.gui.CachePopulatorFinishedGuiEvent
import io.github.manamiproject.manami.gui.FileSavedStatusChangedGuiEvent
import io.github.manamiproject.manami.gui.ImportFinishedGuiEvent
import io.github.manamiproject.manami.gui.UndoRedoStatusGuiEvent
import io.github.manamiproject.manami.gui.animelist.ShowAnimeListTabRequest
import io.github.manamiproject.manami.gui.components.ApplicationBlockedLoading
import io.github.manamiproject.manami.gui.components.PathChooser
import io.github.manamiproject.manami.gui.ignorelist.ShowIgnoreListTabRequest
import io.github.manamiproject.manami.gui.inconsistencies.ShowInconsistenciesTabRequest
import io.github.manamiproject.manami.gui.recommendations.ShowRecommendationsTabRequest
import io.github.manamiproject.manami.gui.relatedanime.ShowRelatedAnimeTabRequest
import io.github.manamiproject.manami.gui.search.anime.ShowAnimeSearchTabRequest
import io.github.manamiproject.manami.gui.search.season.ShowAnimeSeasonTabRequest
import io.github.manamiproject.manami.gui.watchlist.ShowWatchListTabRequest
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.Priority.ALWAYS
import javafx.stage.StageStyle.UNDECORATED
import tornadofx.*

class MenuView : View() {

    private val controller: MenuController by inject()
    private val isFileSaved = SimpleBooleanProperty(true)
    private val isUndoPossible = SimpleBooleanProperty(true)
    private val isRedoPossible = SimpleBooleanProperty(true)
    private val isDisabledBecauseCacheIsNotYetPopulated = SimpleBooleanProperty(true)

    init {
        subscribe<FileSavedStatusChangedGuiEvent> { event ->
            isFileSaved.set(event.isFileSaved)
        }
        subscribe<UndoRedoStatusGuiEvent> { event ->
            isUndoPossible.set(!event.isUndoPossible)
            isRedoPossible.set(!event.isRedoPossible)
        }
        subscribe<CachePopulatorFinishedGuiEvent> {
            isDisabledBecauseCacheIsNotYetPopulated.set(false)
        }
    }

    override val root = menubar {
        hgrow = ALWAYS
        vgrow = ALWAYS

        menu("File") {
            item("New", createMnemonic("N")) {
                action { controller.newFile() }
            }
            item("Open", createMnemonic("O")) {
                action { controller.open(PathChooser.showOpenFileDialog(primaryStage)) }
            }
            separator()
            item("Import", createMnemonic("I")) {
                action {
                    find<ApplicationBlockedLoading>().openModal(resizable = false, escapeClosesWindow = false, stageStyle = UNDECORATED)
                    when(val file = PathChooser.showImportFileDialog(primaryStage)) {
                        null -> fire(ImportFinishedGuiEvent)
                        else -> controller.import(file)
                    }
                }
            }
            separator()
            item("Save", createMnemonic("S")) {
                disableProperty().bindBidirectional(isFileSaved)
                action { controller.save() }
            }
            item("Save as...", createMnemonic("Shift+S")) {
                disableProperty().bindBidirectional(isFileSaved)
                action { controller.saveAs(PathChooser.showSaveAsFileDialog(primaryStage)) }
            }
            separator()
            item("Quit", createMnemonic("Q")) {
                action { controller.quit() }
            }
        }
        menu("Edit") {
            item("Undo", createMnemonic("Z")) {
                disableProperty().bindBidirectional(isUndoPossible)
                action { controller.undo() }
            }
            item("Redo", createMnemonic("Shift+N")) {
                disableProperty().bindBidirectional(isRedoPossible)
                action { controller.redo() }
            }
        }
        menu("Lists") {
            item("Anime List", createMnemonic("1")) {
                action { fire(ShowAnimeListTabRequest) }
            }
            item("Watch List", createMnemonic("2")) {
                action { fire(ShowWatchListTabRequest) }
            }
            item("Ignore List", createMnemonic("3")) {
                action { fire(ShowIgnoreListTabRequest) }
            }
        }
        menu("Find") {
            item("Anime", createMnemonic("5")) {
                disableProperty().bindBidirectional(isDisabledBecauseCacheIsNotYetPopulated)
                action { fire(ShowAnimeSearchTabRequest) }
            }
            item("Season", createMnemonic("6")) {
                disableProperty().bindBidirectional(isDisabledBecauseCacheIsNotYetPopulated)
                action { fire(ShowAnimeSeasonTabRequest) }
            }
            item("Inconsistencies", createMnemonic("7")) {
                isDisable = true
                action { fire(ShowInconsistenciesTabRequest) }
            }
            item("Recommendations", createMnemonic("8")) {
                isDisable = true
                action { fire(ShowRecommendationsTabRequest) }
            }
            item("Related Anime", createMnemonic("9")) {
                isDisable = false
                action { fire(ShowRelatedAnimeTabRequest) }
            }
        }
        menu("Help") {
            item("About", createMnemonic("H")).action { controller.help() }
        }
    }
}

fun createMnemonic(key: String): String {
    val initiator = if (com.sun.javafx.PlatformUtil.isMac()) "Meta" else "Ctrl"
    return "$initiator+$key"
}