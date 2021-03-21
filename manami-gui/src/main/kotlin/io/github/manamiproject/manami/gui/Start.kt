package io.github.manamiproject.manami.gui

import io.github.manamiproject.manami.app.Manami
import io.github.manamiproject.manami.gui.main.MainWindowView
import tornadofx.*

class ManamiGui: App(MainWindowView::class)

internal val manamiInstance = Manami()

fun main(args: Array<String>) {
    launch<ManamiGui>(args)
}