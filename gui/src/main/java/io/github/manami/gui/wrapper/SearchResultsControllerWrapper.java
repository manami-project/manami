package io.github.manami.gui.wrapper;

import static io.github.manami.gui.controller.SearchResultsController.SEARCH_RESULTS_TAB_TITLE;
import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;

import javax.inject.Named;

import com.google.common.eventbus.Subscribe;

import io.github.manami.dto.events.SearchResultEvent;
import io.github.manami.gui.controller.SearchResultsController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

/**
 * @author manami-project
 * @since 2.9.0
 */
@Named
@Slf4j
public class SearchResultsControllerWrapper {

    private Tab searchResultTab;
    private SearchResultsController searchResultController;


    /**
     * @since 2.7.2
     */
    private void init() {
        searchResultTab = new Tab(SEARCH_RESULTS_TAB_TITLE);
        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("io/github/manami/gui/controller/search_results_tab.fxml"));
            pane = (Pane) fxmlLoader.load();
            searchResultTab.setContent(pane);
            searchResultController = fxmlLoader.getController();
        } catch (final Exception e) {
            log.error("An error occurred while trying to initialize search result tab: ", e);
            showExceptionDialog(e);
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
