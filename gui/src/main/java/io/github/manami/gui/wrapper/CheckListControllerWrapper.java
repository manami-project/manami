package io.github.manami.gui.wrapper;

import com.google.common.eventbus.Subscribe;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.CheckListController;
import io.github.manami.gui.utility.DialogLibrary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

/**
 * @author manami-project
 * @since 2.7.2
 */
@Named
public class CheckListControllerWrapper {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(CheckListControllerWrapper.class);

    private Tab checkListTab;
    private CheckListController checkListController;


    /**
     * @since 2.7.2
     */
    private void init() {
        checkListTab = new Tab(CheckListController.CHECK_LIST_TAB_TITLE);
        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("io/github/manami/gui/controller/check_list_tab.fxml"));
            pane = (Pane) fxmlLoader.load();
            checkListTab.setContent(pane);
            checkListController = fxmlLoader.getController();
            checkListController.setTab(checkListTab);
        } catch (final Exception e) {
            LOG.error("An error occurred while trying to initialize check list tab: ", e);
            DialogLibrary.showExceptionDialog(e);
        }
    }


    /**
     * @since 2.7.2
     * @return the checkListTab
     */
    public Tab getCheckListTab() {
        if (checkListTab == null) {
            init();
        }

        return checkListTab;
    }


    /**
     * @since 2.8.2
     * @param event Event which is fired when a file is opened.
     */
    @Subscribe
    public void changeEvent(final OpenedFileChangedEvent event) {
        if (checkListController == null) {
            init();
        }

        checkListController.clear();
    }
}
