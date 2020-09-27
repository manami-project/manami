package io.github.manamiproject.manami.gui.animelist

import javafx.scene.Parent
import tornadofx.*

class AnimeListWorkspace : View() {

    private val animelist: Animelist by inject()

    override val root: Parent = pane {
        hbox {
            button("Add Entry")
            animelist
        }
    }
}