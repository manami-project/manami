package io.github.manami.gui.wrapper;

import static io.github.manami.gui.controller.RelatedAnimeController.RELATED_ANIME_TAB_TITLE;
import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;

import javax.inject.Named;

import com.google.common.eventbus.Subscribe;

import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.RelatedAnimeController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

/**
 * @author manami-project
 * @since 2.7.2
 */
@Named
@Slf4j
public class RelatedAnimeControllerWrapper {

    private Tab relatedAnimeTab;
    private RelatedAnimeController relatedAnimeController;


    private void init() {
        relatedAnimeTab = new Tab(RELATED_ANIME_TAB_TITLE);
        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("io/github/manami/gui/controller/related_animes_tab.fxml"));
            pane = (Pane) fxmlLoader.load();
            relatedAnimeTab.setContent(pane);
            relatedAnimeController = fxmlLoader.getController();
            relatedAnimeController.setTab(relatedAnimeTab);
        } catch (final Exception e) {
            log.error("An error occurred while trying to initialize related animes tab: ", e);
            showExceptionDialog(e);
        }
    }


    /**
     * @return the relatedAnimeTab
     */
    public Tab getRelatedAnimeTab() {
        if (relatedAnimeTab == null) {
            init();
        }

        return relatedAnimeTab;
    }


    /**
     * @since 2.8.2
     * @param event
     *            Event which is fired when a file is opened.
     */
    @Subscribe
    public void changeEvent(final OpenedFileChangedEvent event) {
        if (relatedAnimeController == null) {
            init();
        }

        relatedAnimeController.clear();
    }
}
