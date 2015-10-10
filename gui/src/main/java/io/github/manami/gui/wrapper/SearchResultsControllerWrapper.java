package io.github.manami.gui.wrapper;

import io.github.manami.dto.events.SearchResultEvent;
import io.github.manami.gui.DialogLibrary;
import io.github.manami.gui.controller.SearchResultsController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

/**
 * @author manami-project
 * @since 2.9.0
 */
@Named
public class SearchResultsControllerWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(SearchResultsControllerWrapper.class);

    private Tab searchResultTab;
    private SearchResultsController searchResultController;


    /**
     * @since 2.7.2
     */
    private void init() {
        searchResultTab = new Tab(SearchResultsController.SEARCH_RESULTS_TAB_TITLE);
        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("io/github/manami/gui/controller/search_results_tab.fxml"));
            pane = (Pane) fxmlLoader.load();
            searchResultTab.setContent(pane);
            searchResultController = fxmlLoader.getController();
        } catch (final Exception e) {
            LOG.error("An error occurred while trying to initialize search result tab: ", e);
            DialogLibrary.showExceptionDialog(e);
        }
    }


    /**
     * @since 2.9.0
     * @return the search result tab
     */
    public Tab getSearchResultsTab() {
        if (searchResultTab == null) {
            init();
        }

        return searchResultTab;
    }


    /**
     * @since 2.9.0
     * @param event
     *            Event which is fired when a file is opened.
     */
    @Subscribe
    public void searchResultEvent(final SearchResultEvent event) {
        Platform.runLater(() -> {
            if (searchResultController == null) {
                init();
            }

            searchResultController.showResults(event);
        });
    }
}
