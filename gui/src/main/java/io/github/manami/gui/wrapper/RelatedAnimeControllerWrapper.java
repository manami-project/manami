package io.github.manami.gui.wrapper;

import static io.github.manami.gui.controller.RelatedAnimeController.RELATED_ANIME_TAB_TITLE;
import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;

import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import io.github.manami.dto.events.AnimeListChangedEvent;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.RelatedAnimeController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

@Named
@Slf4j
public class RelatedAnimeControllerWrapper {

    private Tab relatedAnimeTab;
    private RelatedAnimeController relatedAnimeController;


    private void init() {
        relatedAnimeTab = new Tab(RELATED_ANIME_TAB_TITLE);
        Parent pane;
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(new ClassPathResource("io/github/manami/gui/controller/related_anime_tab.fxml").getURL());
            pane = (Pane) fxmlLoader.load();
            relatedAnimeTab.setContent(pane);
            relatedAnimeController = fxmlLoader.getController();
            relatedAnimeController.setTab(relatedAnimeTab);
        } catch (final Exception e) {
            log.error("An error occurred while trying to initialize related anime tab: ", e);
            showExceptionDialog(e);
        }
    }


    public Tab getRelatedAnimeTab() {
        if (relatedAnimeTab == null) {
            init();
        }

        return relatedAnimeTab;
    }


    @Subscribe
    public void openFileEvent(final OpenedFileChangedEvent event) {
        if (relatedAnimeController == null) {
            init();
        }

        relatedAnimeController.clear();
    }


    @Subscribe
    @AllowConcurrentEvents
    public void changeEvent(final AnimeListChangedEvent event) {
        if (relatedAnimeController == null) {
            init();
        }

        relatedAnimeController.synchronizeWithLists();
    }
}
