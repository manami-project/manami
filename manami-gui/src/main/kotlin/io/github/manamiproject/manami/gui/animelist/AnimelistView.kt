package io.github.manamiproject.manami.gui.animelist

import io.github.manamiproject.manami.app.lists.animelist.AnimeListEntry
import tornadofx.View
import tornadofx.pane
import tornadofx.tableview

class AnimelistView : View() {

    override val root = pane {
        tableview<AnimeListEntry> {
        }
    }
}
