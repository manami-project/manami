package io.github.manamiproject.manami.gui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.github.manamiproject.manami.gui.tabs.TabBar
import io.github.manamiproject.manami.gui.theme.ManamiTheme
import io.github.manamiproject.manami.gui.theme.ThemeState

fun main() = application {
    val viewModel = MainViewModel.instance
    val isSaved by viewModel.isSaved.collectAsState()
    val isUndoPossible by viewModel.isUndoPossible.collectAsState()
    val isRedoPossible by viewModel.isRedoPossible.collectAsState()
    val windowTitle by viewModel.windowTitle.collectAsState()

    Window(
        onCloseRequest = { viewModel.quit() },
        title = windowTitle,
        state = WindowState(size = viewModel.windowSize()),
    ) {
        val mainWindow = this

        MenuBar {
            Menu("File") {
                Item(
                    text = "New",
                    shortcut = KeyShortcut(Key.N, meta = true),
                    onClick = { viewModel.new() }, // TODO 4.0.0: Check save status
                    enabled = true,
                )
                Item(
                    text = "Open",
                    shortcut = KeyShortcut(Key.O, meta = true),
                    onClick = { viewModel.open(mainWindow) }, // TODO 4.0.0: Check save status
                    enabled = true,
                )
                Separator()
                Item(
                    text = "Save",
                    shortcut = KeyShortcut(Key.S, meta = true),
                    onClick = { viewModel.save() }, // TODO 4.0.0: Check if you are working on an opened file otherwise delegate to saveAs
                    enabled = !isSaved,
                )
                Item(
                    text = "Save As...",
                    shortcut = KeyShortcut(Key.S, shift = true, meta = true),
                    onClick = { viewModel.saveAs(mainWindow) },
                    enabled = true,
                )
                Separator()
                Item(
                    text = "Quit",
                    shortcut = KeyShortcut(Key.Q, meta = true),
                    onClick = { viewModel.quit() }, // TODO 4.0.0: Check save status
                    enabled = true,
                )
            }
            Menu("Edit") {
                Item(
                    text = "Undo",
                    shortcut = KeyShortcut(Key.Z, meta = true),
                    onClick = { viewModel.undo() },
                    enabled = isUndoPossible,
                )
                Item(
                    text = "Redo",
                    shortcut = KeyShortcut(Key.Z, shift = true, meta = true),
                    onClick = { viewModel.redo() },
                    enabled = isRedoPossible,
                )
                Separator()
                Item(
                    text = "Meta Data Provider Migration",
                    shortcut = KeyShortcut(Key.M, meta = true),
                    onClick = { TODO() },
                    enabled = false, // TODO 4.0.0 bind any list containing entries
                )
            }
            Menu("Lists") {
                Item(
                    text = "Anime List",
                    shortcut = KeyShortcut(Key.A, meta = true),
                    onClick = { viewModel.openAnimeListTab() },
                    enabled = true,
                )
                Item(
                    text = "Watch List",
                    shortcut = KeyShortcut(Key.W, meta = true),
                    onClick = { viewModel.openWatchListTab() },
                    enabled = true,
                )
                Item(
                    text = "Ignore List",
                    shortcut = KeyShortcut(Key.I, meta = true),
                    onClick = { viewModel.openIgnoreListTab() },
                    enabled = true,
                )
            }
            Menu("Find") {
                Item(
                    text = "Anime",
                    shortcut = KeyShortcut(Key.One, meta = true),
                    onClick = { viewModel.openFindAnimeTab() },
                    enabled = true, // TODO 4.0.0 bind to cache being populated
                )
                Item(
                    text = "Season",
                    shortcut = KeyShortcut(Key.Two, meta = true),
                    onClick = { viewModel.openFindSeasonTab() },
                    enabled = true, // TODO 4.0.0 bind to cache being populated
                )
                Item(
                    text = "Similar Anime",
                    shortcut = KeyShortcut(Key.Three, meta = true),
                    onClick = { viewModel.openFindSimilarAnimeTab() },
                    enabled = true, // TODO 4.0.0 bind to cache being populated
                )
                Separator()
                Item(
                    text = "Inconsistencies",
                    shortcut = KeyShortcut(Key.Four, meta = true),
                    onClick = { viewModel.openFindInconsistenciesTab() },
                    enabled = false, // TODO 4.0.0 bind any list containing entries
                )
                Item(
                    text = "Related Anime",
                    shortcut = KeyShortcut(Key.Five, meta = true),
                    onClick = { viewModel.openFindRelatedAnimeTab() },
                    enabled = false, // TODO 4.0.0 bind to anime list containing entries
                )
            }
            Menu("View") {
                Item(
                    text = ThemeState.instance.caption,
                    onClick = { ThemeState.instance.toggle() },
                    enabled = true,
                )
            }
            Menu("Help") {
                Item(
                    text = "About",
                    onClick = { TODO() },
                    enabled = true,
                )
            }
        }
        ManamiTheme {
            Box( // unable to modify the background of the main window so painting a custom one is necessary
                modifier = Modifier
                    .fillMaxSize()
                    .background(ThemeState.instance.currentScheme.background)
            ) {
                TabBar()
            }
        }
    }
}