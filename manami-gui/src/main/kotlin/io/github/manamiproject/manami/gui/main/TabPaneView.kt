package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.gui.animelist.AnimelistView
import io.github.manamiproject.manami.gui.animelist.ShowAnimeListTabRequest
import io.github.manamiproject.manami.gui.extensions.openTab
import io.github.manamiproject.manami.gui.ignorelist.IgnoreListView
import io.github.manamiproject.manami.gui.ignorelist.ShowIgnoreListTabRequest
import io.github.manamiproject.manami.gui.relatedanime.RelatedAnimeView
import io.github.manamiproject.manami.gui.relatedanime.ShowRelatedAnimeTabRequest
import io.github.manamiproject.manami.gui.search.file.FileSearchView
import io.github.manamiproject.manami.gui.search.file.ShowFileSearchTabRequest
import io.github.manamiproject.manami.gui.search.season.AnimeSeasonView
import io.github.manamiproject.manami.gui.search.season.ShowAnimeSeasonTabRequest
import io.github.manamiproject.manami.gui.watchlist.ShowWatchListTabRequest
import io.github.manamiproject.manami.gui.watchlist.WatchListView
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class TabPaneView : View() {

    private val animeListView: AnimelistView by inject()
    private val watchListView: WatchListView by inject()
    private val ignoreListView: IgnoreListView by inject()
    private val fileSearchView: FileSearchView by inject()
    private val animeSeasonView: AnimeSeasonView by inject()
    private val relatedAnimeView: RelatedAnimeView by inject()

    init {
        subscribe<ShowAnimeListTabRequest> {
            tabPane.openTab("Anime List") { add(animeListView.root) }
        }
        subscribe<ShowWatchListTabRequest> {
            tabPane.openTab("Watch List") { add(watchListView.root) }
        }
        subscribe<ShowIgnoreListTabRequest> {
            tabPane.openTab("Ignore List") { add(ignoreListView.root) }
        }
        subscribe<ShowFileSearchTabRequest> {
            tabPane.openTab("Search") { add(fileSearchView.root) }
        }
        subscribe<ShowAnimeSeasonTabRequest> {
            tabPane.openTab("Anime Season") { add(animeSeasonView.root) }
        }
        subscribe<ShowRelatedAnimeTabRequest> {
            tabPane.openTab("Related Anime") { add(relatedAnimeView.root) }
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