package io.github.manami.gui.wrapper;

import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import io.github.manami.dto.events.AnimeListChangedEvent;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.FilterListController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;

/**
 * @author manami-project
 * @since 2.7.2
 */
@Named
public class FilterListControllerWrapper {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(FilterListControllerWrapper.class);

    private Tab filterTab;
    private FilterListController filterListController;


    /**
     * @since 2.7.2
     */
    private void init() {
        filterTab = new Tab(FilterListController.FILTER_LIST_TITLE);
        filterTab.setOnSelectionChanged(event -> {
            if (filterTab.isSelected()) {
                filterListController.showEntries();
            }
        });

        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("io/github/manami/gui/controller/filter_list_tab.fxml"));
            pane = (Pane) fxmlLoader.load();
            filterListController = fxmlLoader.getController();
            filterTab.setContent(pane);
        } catch (final Exception e) {
            LOG.error("An error occurred while trying to initialize filter list tab: ", e);
            showExceptionDialog(e);
        }
    }


    @Subscribe
    public void changeEvent(final OpenedFileChangedEvent event) {
        filterListController.clear();
    }


    @Subscribe
    @AllowConcurrentEvents
    public void changeEvent(final AnimeListChangedEvent event) {
        if (filterTab.isSelected()) {
            filterListController.showEntries();
        }
    }


    /**
     * @since 2.7.2
     */
    public void startRecommendedFilterEntrySearch() {
        filterListController.startRecommendedFilterEntrySearch();
    }


    /**
     * @since 2.7.2
     * @return the filterTab
     */
    public Tab getFilterTab() {
        if (filterTab == null) {
            init();
        }

        return filterTab;
    }
}
