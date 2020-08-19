package io.github.manamiproject.manami.gui.extensions

import javafx.scene.Node
import tornadofx.runLater

fun Node.focus(): Node {
    runLater { this.requestFocus() }
    return this
}