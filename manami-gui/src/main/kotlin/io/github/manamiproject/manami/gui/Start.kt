package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.gui.main.MainWindow
import tornadofx.*

class ManamiGui: App(MainWindow::class)

fun main(args: Array<String>) {
    ManamiAccess()
    launch<ManamiGui>(args)
}