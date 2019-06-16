package io.github.manami.gui.wrapper;

import static io.github.manami.gui.controller.FilterListController.FILTER_LIST_TITLE;
import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;

import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import io.github.manami.dto.events.AnimeListChangedEvent;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.FilterListController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;


@Named
@Slf4j
public class FilterListControllerWrapper {

    private Tab filterTab;
    private FilterListController filterListController;


    private void init() {
        filterTab = new Tab(FILTER_LIST_TITLE);

        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(new ClassPathResource("io/github/manami/gui/controller/filter_list_tab.fxml").getURL());
            pane = (Pane) fxmlLoader.load();
            filterListController = fxmlLoader.getController();
            filterTab.setContent(pane);
            filterListController.setTab(filterTab);
        } catch (final Exception e) {
            log.error("An error occurred while trying to initialize filter list tab: ", e);
            showExceptionDialog(e);
        }

    }


    @Subscribe
    public void openFileEvent(final OpenedFileChangedEvent event) {
        filterListController.clear();
    }


    @Subscribe
    @AllowConcurrentEvents
    public void changeEvent(final AnimeListChangedEvent event) {
        if (filterListController == null) {
            init();
        }

        filterListController.synchronizeWithLists();
    }


    public void startRecommendedFilterEntrySearch() {
        filterListController.startRecommendedFilterEntrySearch();
    }


    public Tab getFilterTab() {
        if (filterTab == null) {
            init();
        }

        return filterTab;
    }
}
