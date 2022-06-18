package io.github.manamiproject.manami.gui.main

import io.github.manamiproject.manami.gui.animelist.AnimeListView
import io.github.manamiproject.manami.gui.animelist.ShowAnimeListTabRequest
import io.github.manamiproject.manami.gui.dashboard.DashboardView
import io.github.manamiproject.manami.gui.extensions.openTab
import io.github.manamiproject.manami.gui.ignorelist.IgnoreListView
import io.github.manamiproject.manami.gui.ignorelist.ShowIgnoreListTabRequest
import io.github.manamiproject.manami.gui.inconsistencies.InconsistenciesView
import io.github.manamiproject.manami.gui.inconsistencies.ShowInconsistenciesTabRequest
import io.github.manamiproject.manami.gui.migration.MetaDataProviderMigrationView
import io.github.manamiproject.manami.gui.migration.ShowMetaDataProviderMigrationViewTabRequest
import io.github.manamiproject.manami.gui.relatedanime.RelatedAnimeView
import io.github.manamiproject.manami.gui.relatedanime.ShowRelatedAnimeTabRequest
import io.github.manamiproject.manami.gui.search.anime.AnimeSearchView
import io.github.manamiproject.manami.gui.search.anime.ShowAnimeSearchTabRequest
import io.github.manamiproject.manami.gui.search.file.FileSearchView
import io.github.manamiproject.manami.gui.search.file.ShowFileSearchTabRequest
import io.github.manamiproject.manami.gui.search.season.AnimeSeasonView
import io.github.manamiproject.manami.gui.search.season.ShowAnimeSeasonTabRequest
import io.github.manamiproject.manami.gui.search.similaranime.ShowSimilarAnimeSearchTabRequest
import io.github.manamiproject.manami.gui.search.similaranime.SimilarAnimeSearchView
import io.github.manamiproject.manami.gui.watchlist.ShowWatchListTabRequest
import io.github.manamiproject.manami.gui.watchlist.WatchListView
import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*

class TabPaneView : View() {

    private val dashboardView: DashboardView by inject()
    private val animeListView: AnimeListView by inject()
    private val watchListView: WatchListView by inject()
    private val ignoreListView: IgnoreListView by inject()
    private val fileSearchView: FileSearchView by inject()
    private val animeSearchView: AnimeSearchView by inject()
    private val animeSeasonView: AnimeSeasonView by inject()
    private val relatedAnimeView: RelatedAnimeView by inject()
    private val inconsistenciesView: InconsistenciesView by inject()
    private val similarAnimeSearchView: SimilarAnimeSearchView by inject()
    private val metaDataProviderMigrationView: MetaDataProviderMigrationView by inject()

    private val tabPane = tabpane {
        hgrow = ALWAYS
        vgrow = ALWAYS
    }

    init {
        tabPane.openTab(title = "Dashboard", closeable = false) { add(dashboardView.root) }

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
        subscribe<ShowAnimeSearchTabRequest> {
            tabPane.openTab("Anime Search") { add(animeSearchView.root) }
        }
        subscribe<ShowAnimeSeasonTabRequest> {
            tabPane.openTab("Anime Season") { add(animeSeasonView.root) }
        }
        subscribe<ShowInconsistenciesTabRequest> {
            tabPane.openTab("Inconsistencies") { add(inconsistenciesView.root) }
        }
        subscribe<ShowRelatedAnimeTabRequest> {
            tabPane.openTab("Related Anime") { add(relatedAnimeView.root) }
        }
        subscribe<ShowSimilarAnimeSearchTabRequest> {
            tabPane.openTab("Similar Anime") { add(similarAnimeSearchView.root) }
        }
        subscribe<ShowMetaDataProviderMigrationViewTabRequest>() {
            tabPane.openTab("Meta Data Provider Migration") { add(metaDataProviderMigrationView.root) }
        }
    }

    override val root = tabPane
}