package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.gui.animelist.AnimeListWorkspace
import io.github.manamiproject.manami.gui.animelist.ShowAnimeListTabRequest
import io.github.manamiproject.manami.gui.extensions.openTab
import io.github.manamiproject.manami.gui.ignorelist.ShowIgnoreListTabRequest
import io.github.manamiproject.manami.gui.search.SearchView
import io.github.manamiproject.manami.gui.search.ShowSearchTabRequest
import io.github.manamiproject.manami.gui.watchlist.ShowWatchListTabRequest
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class TabPaneView : View() {

    private val animeListWorkspace: AnimeListWorkspace by inject()
    private val searchView: SearchView by inject()

    init {
        subscribe<ShowAnimeListTabRequest> {
            tabPane.openTab("Anime List") { add(animeListWorkspace.root) }
        }

        subscribe<ShowWatchListTabRequest> {
            tabPane.openTab("Watch List") { add(animeListWorkspace.root) }
        }

        subscribe<ShowIgnoreListTabRequest> {
            tabPane.openTab("Ignore List") { add(animeListWorkspace.root) }
        }

        subscribe<ShowSearchTabRequest> {
            tabPane.openTab("Search") { add(searchView.root) }
        }
    }

    private val tabPane = tabpane {
        hgrow = ALWAYS
        vgrow = ALWAYS

        tab("Dashboard") {
            isClosable = false

            vbox {
            }
        }
    }

    override val root = tabPane
}