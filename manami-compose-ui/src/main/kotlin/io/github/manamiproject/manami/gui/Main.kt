package io.github.manamiproject.manami.gui

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import java.awt.Toolkit

fun main() = application {
    // Make the window fullscreen
    val screenSize = Toolkit.getDefaultToolkit().screenSize
    val screenWidth = screenSize.width.dp
    val screenHeight = screenSize.height.dp

    Window(
        onCloseRequest = { TODO() },
        title = "Manami",
        state = WindowState(size = DpSize(screenWidth, screenHeight)),
    ) {
        MenuBar {
            Menu("File") {
                Item(
                    text = "New",
                    shortcut = KeyShortcut(Key.N, meta = true),
                    onClick = { TODO() },
                )
                Item(
                    text = "Open",
                    shortcut = KeyShortcut(Key.O, meta = true),
                    onClick = { TODO() },
                )
                Separator()
                Item(
                    text = "Save",
                    shortcut = KeyShortcut(Key.S, meta = true),
                    onClick = { TODO() },
                )
                Item(
                    text = "Save As...",
                    shortcut = KeyShortcut(Key.S, shift = true, meta = true),
                    onClick = { TODO() },
                )
                Separator()
                Item(
                    text = "Quit",
                    shortcut = KeyShortcut(Key.Q, meta = true),
                    onClick = { exitApplication() },
                )
            }
            Menu("Edit") {
                Item(
                    text = "Undo",
                    shortcut = KeyShortcut(Key.Z, meta = true),
                    onClick = { TODO() },
                )
                Item(
                    text = "Redo",
                    shortcut = KeyShortcut(Key.Z, shift = true, meta = true),
                    onClick = { TODO() },
                )
                Separator()
                Item(
                    text = "Meta Data Provider Migration",
                    shortcut = KeyShortcut(Key.M, meta = true),
                    onClick = { TODO() },
                )
            }
            Menu("Lists") {
                Item(
                    text = "Anime List",
                    shortcut = KeyShortcut(Key.A, meta = true),
                    onClick = { TODO() },
                )
                Item(
                    text = "Watch List",
                    shortcut = KeyShortcut(Key.W, meta = true),
                    onClick = { TODO() },
                )
                Item(
                    text = "Ignore List",
                    shortcut = KeyShortcut(Key.I, meta = true),
                    onClick = { TODO() },
                )
            }
            Menu("Find") {
                Item(
                    text = "Anime",
                    shortcut = KeyShortcut(Key.One, meta = true),
                    onClick = { TODO() },
                )
                Item(
                    text = "Season",
                    shortcut = KeyShortcut(Key.Two, meta = true),
                    onClick = { TODO() },
                )
                Item(
                    text = "Similar Anime",
                    shortcut = KeyShortcut(Key.Three, meta = true),
                    onClick = { TODO() },
                )
                Separator()
                Item(
                    text = "Inconsistencies",
                    shortcut = KeyShortcut(Key.Four, meta = true),
                    onClick = { TODO() },
                )
                Item(
                    text = "Related Anime",
                    shortcut = KeyShortcut(Key.Five, meta = true),
                    onClick = { TODO() },
                )
            }
            Menu("Help") {
                Item("About", onClick = { TODO() })
            }
        }
    }
}