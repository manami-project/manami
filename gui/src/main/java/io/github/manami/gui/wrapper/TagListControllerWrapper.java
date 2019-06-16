package io.github.manami.gui.wrapper;

import static io.github.manami.gui.controller.TagListController.TAG_LIST_TITLE;
import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;

import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import io.github.manami.dto.events.AnimeListChangedEvent;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.TagListController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;


@Named
@Slf4j
public class TagListControllerWrapper {

    private Tab tagListTab;
    private TagListController tagListController;


    private void init() {
        tagListTab = new Tab(TAG_LIST_TITLE);

        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(new ClassPathResource("io/github/manami/gui/controller/tag_list_tab.fxml").getURL());
            pane = (Pane) fxmlLoader.load();
            tagListController = fxmlLoader.getController();
            tagListController.setTab(tagListTab);
            tagListTab.setContent(pane);
        } catch (final Exception e) {
            log.error("An error occurred while trying to initialize watch list tab: ", e);
            showExceptionDialog(e);
        }
    }


    @Subscribe
    public void openFileEvent(final OpenedFileChangedEvent event) {
        if (tagListController == null) {
            init();
        }

        tagListController.clear();
    }


    @Subscribe
    @AllowConcurrentEvents
    public void changeEvent(final AnimeListChangedEvent event) {
        if (tagListController == null) {
            init();
        }

        tagListController.synchronizeWithLists();
    }


    public Tab getTagListTab() {
        if (tagListTab == null) {
            init();
        }

        return tagListTab;
    }
}