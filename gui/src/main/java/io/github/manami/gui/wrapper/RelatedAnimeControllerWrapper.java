package io.github.manami.gui.wrapper;

import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.DialogLibrary;
import io.github.manami.gui.controller.RelatedAnimeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;

/**
 * @author manami project
 * @since 2.7.2
 */
@Named
public class RelatedAnimeControllerWrapper {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(RelatedAnimeControllerWrapper.class);

    private Tab relatedAnimeTab;
    private RelatedAnimeController relatedAnimeController;


    private void init() {
        relatedAnimeTab = new Tab(RelatedAnimeController.RELATED_ANIME_TAB_TITLE);
        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("io/github/manami/gui/controller/related_animes_tab.fxml"));
            pane = (Pane) fxmlLoader.load();
            relatedAnimeTab.setContent(pane);
            relatedAnimeController = fxmlLoader.getController();
            relatedAnimeController.setTab(relatedAnimeTab);
        } catch (final Exception e) {
            LOG.error("An error occurred while trying to initialize related animes tab: ", e);
            DialogLibrary.showExceptionDialog(e);
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
     * @param event Event which is fired when a file is opened.
     */
    @Subscribe
    public void changeEvent(final OpenedFileChangedEvent event) {
        if (relatedAnimeController == null) {
            init();
        }

        relatedAnimeController.clear();
    }
}
