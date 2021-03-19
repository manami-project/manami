package io.github.manamiproject.manami.gui.components

import io.github.manamiproject.manami.gui.ImportFinishedGuiEvent
import javafx.scene.Parent
import tornadofx.Fragment
import tornadofx.pane
import tornadofx.progressindicator

class ApplicationBlockedLoading : Fragment() {

    init {
        subscribe<ImportFinishedGuiEvent> {
            close()
        }
    }

    override val root: Parent = pane {
        progressindicator {
            progress = -1.0
        }
    }
}