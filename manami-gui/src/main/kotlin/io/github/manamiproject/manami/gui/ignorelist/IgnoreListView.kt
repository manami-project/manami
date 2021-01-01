package io.github.manamiproject.manami.gui.ignorelist

import io.github.manamiproject.manami.app.models.IgnoreListEntry
import io.github.manamiproject.manami.gui.components.animeTable
import tornadofx.View
import tornadofx.pane

class IgnoreListView : View() {

    override val root = pane {
        animeTable<IgnoreListEntry> {
        }
    }
}