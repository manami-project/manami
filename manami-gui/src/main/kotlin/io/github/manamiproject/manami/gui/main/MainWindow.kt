package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.gui.extensions.focus
import javafx.event.EventHandler
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class MainWindow : View("Manami") {

    private val menuView: MenuView by inject()
    private val searchBoxView: SearchBoxView by inject()
    private val tabPaneView: TabPaneView by inject()
    private val quitController: QuitController by inject()

    init {
        this.primaryStage.isMaximized = true
        this.primaryStage.onCloseRequest = EventHandler { event -> quitController.quit(); event.consume() }
    }

    override val root = vbox {
        hgrow = ALWAYS
        vgrow = ALWAYS

        hbox {
            hgrow = ALWAYS

            add(menuView.root)
            add(searchBoxView.root)
        }

        add(tabPaneView.root)
    }.apply { focus() }
}