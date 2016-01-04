package io.github.manami.gui.wrapper;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import io.github.manami.dto.events.AnimeListChangedEvent;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.WatchListController;
import io.github.manami.gui.utility.DialogLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

/**
 * @author manami-project
 * @since 2.8.0
 */
@Named
public class WatchListControllerWrapper {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(WatchListControllerWrapper.class);

    private Tab watchListTab;
    private WatchListController watchListController;


    /**
     * @since 2.7.2
     */
    private void init() {
        watchListTab = new Tab(WatchListController.WATCH_LIST_TITLE);
        watchListTab.setOnSelectionChanged(event -> {
            if (watchListTab.isSelected()) {
                watchListController.showEntries();
            }
        });

        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("io/github/manami/gui/controller/watch_list_tab.fxml"));
            pane = (Pane) fxmlLoader.load();
            watchListController = fxmlLoader.getController();
            watchListTab.setContent(pane);
        } catch (final Exception e) {
            LOG.error("An error occurred while trying to initialize watch list tab: ", e);
            DialogLibrary.showExceptionDialog(e);
        }
    }


    @Subscribe
    public void changeEvent(final OpenedFileChangedEvent event) {
        if (watchListController == null) {
            init();
        }

        watchListController.clear();
    }


    @Subscribe
    @AllowConcurrentEvents
    public void changeEvent(final AnimeListChangedEvent event) {
        if (watchListController == null) {
            init();
        }

        if (watchListTab.isSelected()) {
            watchListController.showEntries();
        }
    }


    /**
     * @since 2.7.2
     * @return the filterTab
     */
    public Tab getWatchListTab() {
        if (watchListTab == null) {
            init();
        }

        return watchListTab;
    }
}
