package io.github.manami.gui.wrapper;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import io.github.manami.core.config.Config;
import io.github.manami.dto.events.AnimeListChangedEvent;
import io.github.manami.dto.events.ApplicationContextStartedEvent;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.DialogLibrary;
import io.github.manami.gui.controller.MainController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author manami-project
 * @since 2.7.2
 */
@Named
public class MainControllerWrapper {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(MainControllerWrapper.class);

    /** The window's title. */
    public static final String APPNAME = "Manami";

    private MainController mainController;

    private Tab animeListTab;

    private final Config config;

    private Stage mainStage;


    @Inject
    public MainControllerWrapper(final Config config) {
        this.config = config;
    }


    /**
     * @since 2.7.2
     */
    private void init() {
        mainStage.setMaximized(true);
        mainStage.setTitle(APPNAME);

        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("io/github/manami/gui/controller/main.fxml"));
            final Parent pane = (Pane) fxmlLoader.load();
            mainStage.setScene(new Scene(pane));
            mainController = fxmlLoader.getController();
        } catch (final Exception e) {
            LOG.error("An error occurred while trying to initialize main controller: ", e);
            DialogLibrary.showExceptionDialog(e);
        }

        animeListTab = mainController.getTabAnimeList();
        mainStage.show();
    }


    @Subscribe
    public void changeEvent(final ApplicationContextStartedEvent event) {
        mainStage = event.getStage();
        init();
    }


    @Subscribe
    @AllowConcurrentEvents
    public void changeEvent(final AnimeListChangedEvent event) {
        if (animeListTab.isSelected()) {
            mainController.refreshEntriesInGui();
        }
        mainController.checkGui();
    }


    @Subscribe
    public void changeEvent(final OpenedFileChangedEvent event) {
        if (config.getFile() != null) {
            Platform.runLater(() -> mainStage.setTitle(MainControllerWrapper.APPNAME + " - " + config.getFile().toString()));
        } else {
            Platform.runLater(() -> mainStage.setTitle(MainControllerWrapper.APPNAME));
        }
        mainController.checkGui();
    }


    /**
     * @since 2.7.2
     * @return the mainStage
     */
    public Stage getMainStage() {
        return mainStage;
    }


    /**
     * @since 2.7.2
     * @return the mainController
     */
    public MainController getMainController() {
        return mainController;
    }
}
