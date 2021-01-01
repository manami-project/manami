package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.gui.animelist.AnimelistView
import io.github.manamiproject.manami.gui.animelist.ShowAnimeListTabRequest
import io.github.manamiproject.manami.gui.extensions.openTab
import io.github.manamiproject.manami.gui.ignorelist.IgnoreListView
import io.github.manamiproject.manami.gui.ignorelist.ShowIgnoreListTabRequest
import io.github.manamiproject.manami.gui.search.SearchView
import io.github.manamiproject.manami.gui.search.ShowSearchTabRequest
import io.github.manamiproject.manami.gui.watchlist.ShowWatchListTabRequest
import io.github.manamiproject.manami.gui.watchlist.WatchListView
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class TabPaneView : View() {

    private val animelistView: AnimelistView by inject()
    private val watchListView: WatchListView by inject()
    private val ignoreListView: IgnoreListView by inject()
    private val searchView: SearchView by inject()

    init {
        subscribe<ShowAnimeListTabRequest> {
            tabPane.openTab("Anime List") { add(animelistView.root) }
        }

        subscribe<ShowWatchListTabRequest> {
            tabPane.openTab("Watch List") { add(watchListView.root) }
        }

        subscribe<ShowIgnoreListTabRequest> {
            tabPane.openTab("Ignore List") { add(ignoreListView.root) }
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