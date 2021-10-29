package io.github.manamiproject.manami.gui.components

import javafx.scene.Parent
import tornadofx.Fragment
import tornadofx.pane
import tornadofx.progressindicator

class ApplicationBlockedLoading : Fragment() {

    override val root: Parent = pane {
        progressindicator {
            progress = -1.0
        }
    }
}