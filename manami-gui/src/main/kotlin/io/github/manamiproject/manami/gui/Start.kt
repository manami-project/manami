package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.gui.main.MainWindow
import tornadofx.*

class ManamiGui: App(MainWindow::class)

internal val manamiInstance = Manami()

fun main(args: Array<String>) {
    launch<ManamiGui>(args)
}