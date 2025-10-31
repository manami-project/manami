package io.github.manamiproject.manami.gui

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    val viewModel = MainWindowViewModel.instance
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
                    onClick = { viewModel.new() },
                    enabled = true,
                )
                Item(
                    text = "Open",
                    shortcut = KeyShortcut(Key.O, meta = true),
                    onClick = { viewModel.open(mainWindow) },
                    enabled = true,
                )
                Separator()
                Item(
                    text = "Save",
                    shortcut = KeyShortcut(Key.S, meta = true),
                    onClick = { viewModel.save() },
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
                    onClick = { viewModel.quit() },
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
                    onClick = { TODO() },
                    enabled = false,
                )
                Item(
                    text = "Watch List",
                    shortcut = KeyShortcut(Key.W, meta = true),
                    onClick = { TODO() },
                    enabled = false,
                )
                Item(
                    text = "Ignore List",
                    shortcut = KeyShortcut(Key.I, meta = true),
                    onClick = { TODO() },
                    enabled = false,
                )
            }
            Menu("Find") {
                Item(
                    text = "Anime",
                    shortcut = KeyShortcut(Key.One, meta = true),
                    onClick = { TODO() },
                    enabled = true, // TODO 4.0.0 bind to cache being populated
                )
                Item(
                    text = "Season",
                    shortcut = KeyShortcut(Key.Two, meta = true),
                    onClick = { TODO() },
                    enabled = true, // TODO 4.0.0 bind to cache being populated
                )
                Item(
                    text = "Similar Anime",
                    shortcut = KeyShortcut(Key.Three, meta = true),
                    onClick = { TODO() },
                    enabled = true, // TODO 4.0.0 bind to cache being populated
                )
                Separator()
                Item(
                    text = "Inconsistencies",
                    shortcut = KeyShortcut(Key.Four, meta = true),
                    onClick = { TODO() }, // TODO 4.0.0 bind any list containing entries
                    enabled = false,
                )
                Item(
                    text = "Related Anime",
                    shortcut = KeyShortcut(Key.Five, meta = true),
                    onClick = { TODO() }, // TODO 4.0.0 bind to anime list containing entries
                    enabled = false,
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
    }
}