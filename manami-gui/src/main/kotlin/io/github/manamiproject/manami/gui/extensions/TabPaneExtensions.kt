package io.github.manamiproject.manami.gui.extensions

import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import tornadofx.runLater
import tornadofx.select
import tornadofx.tab

/**
 * Adds a new [Tab] to the [TabPane] if there is currently no [Tab] with the given title and
 * selects it. If the [Tab] is already part of the [TabPane] it will be selected.
 */
fun TabPane.openTab(title: String, closeable: Boolean = true, content: Tab.() -> Unit = {}) {
    val isTabAlreadyOpened = this.tabs.find { it.text == title } != null

    if (!isTabAlreadyOpened) {
        runLater {
            this.tab(title) {
                isClosable = closeable
                also(content)
            }
        }
    }

    runLater {
        this.tabs.find { it.text == title }?.select()
    }
}