package io.github.manami.gui.wrapper;

import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import io.github.manami.core.config.Config;
import io.github.manami.dto.events.AnimeListChangedEvent;
import io.github.manami.dto.events.ApplicationContextStartedEvent;
import io.github.manami.dto.events.OpenedFileChangedEvent;
import io.github.manami.gui.controller.MainController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author manami-project
 * @since 2.7.2
 */
@Named
@Slf4j
public class MainControllerWrapper {

    /** The window's title. */
    public static final String APPNAME = "Manami";

    @Getter
    private MainController mainController;

    private final Config config;

    @Getter
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
            log.error("An error occurred while trying to initialize main controller: ", e);
            showExceptionDialog(e);
        }

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
        mainController.refreshEntriesInGui();
        mainController.checkGui();
    }


    @Subscribe
    public void changeEvent(final OpenedFileChangedEvent event) {
        if (config.getFile() != null) {
            Platform.runLater(() -> mainStage.setTitle(APPNAME + " - " + config.getFile().toString()));
        } else {
            Platform.runLater(() -> mainStage.setTitle(APPNAME));
        }
        mainController.checkGui();
    }


    /**
     * @since 2.9.0
     * @param isDirty
     */
    public void setDirty(final boolean isDirty) {
        if (isDirty) {
            if (config.getFile() != null) {
                mainStage.setTitle(String.format("%s - %s *", APPNAME, config.getFile().toString()));
            } else {
                mainStage.setTitle(String.format("%s *", APPNAME));
            }
        } else {
            if (config.getFile() != null) {
                mainStage.setTitle(String.format("%s - %s", APPNAME, config.getFile().toString()));
            } else {
                mainStage.setTitle(String.format("%s", APPNAME));
            }
        }
    }
}
