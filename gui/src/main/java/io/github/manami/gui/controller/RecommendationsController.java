package io.github.manami.gui.controller;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.config.Config;
import io.github.manami.core.services.RecommendationsRetrievalService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.events.AdvancedProgressState;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.gui.components.Icons;
import io.github.manami.gui.utility.DialogLibrary;
import io.github.manami.gui.wrapper.MainControllerWrapper;

import java.nio.file.Path;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import org.controlsfx.control.Notifications;

import com.google.common.collect.Lists;

/**
 * @author manami-project
 * @since 2.4.0
 */
public class RecommendationsController extends AbstractAnimeListController implements Observer {

    public static final String RECOMMENDATIONS_TAB_TITLE = "Recommendations";

    /** Instance of the service repository. */
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);

    private final Manami app = Main.CONTEXT.getBean(Manami.class);

    private final Cache cache = Main.CONTEXT.getBean(Cache.class);

    /** Container holding all the progress components. */
    @FXML
    private VBox vBoxProgress;

    /** Progress bar */
    @FXML
    private ProgressBar progressBar;

    /** Label showing how many entries have been processed. */
    @FXML
    private Label lblProgress;

    /** Button for starting the search. */
    @FXML
    private Button btnStart;

    /** Button to cancel the service. */
    @FXML
    private Button btnCancel;

    /** {@link GridPane} containing the results. */
    @FXML
    private GridPane gridPane;

    @FXML
    private Button btnExport;

    /** Instance of the tab in which the pane is being shown. */
    private Tab tab;

    /** Service instance. */
    private RecommendationsRetrievalService service;


    /**
     * Called from FXML when creating the Object.
     *
     * @since 2.4.0
     */
    public void initialize() {
        btnStart.setOnAction(event -> start());

        btnCancel.setGraphic(Icons.createIconCancel());
        btnCancel.setTooltip(new Tooltip("cancel"));
        btnCancel.setOnAction(event -> cancel());

        btnExport.setOnAction(event -> exportRecommendations());
        btnExport.setVisible(false);
    }


    /**
     * @since 2.10.0
     * @return
     */
    private void exportRecommendations() {
        final Path file = DialogLibrary.showExportDialog(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainStage(), DialogLibrary.JSON_FILTER);
        final List<Anime> exportList = Lists.newArrayList();

        getComponentList().forEach(entry -> exportList.add((Anime) entry.getAnime()));

        app.exportList(exportList, file);
    }


    private void showProgressControls(final boolean value) {
        Platform.runLater(() -> {
            vBoxProgress.setVisible(value);
            btnCancel.setVisible(value);
            btnStart.setVisible(!value);
        });
    }


    private void start() {
        service = new RecommendationsRetrievalService(app, cache, this);
        showProgressControls(true);
        clearComponentList();
        serviceRepo.startService(service);
    }


    /**
     * Stops the service if necessary and resets the GUI.
     *
     * @since 2.4.0
     */
    public void cancel() {
        if (service != null) {
            service.cancel();
        }

        clear();
    }


    @Override
    public void update(final Observable observable, final Object object) {
        if (object == null) {
            return;
        }

        if (object instanceof AdvancedProgressState) {
            final AdvancedProgressState state = (AdvancedProgressState) object;
            final int done = state.getDone();
            final int all = state.getTodo();
            final double percent = ((done * 100.00) / all) / 100.00;

            if (state.getAnime() != null) {
                addEntryToGui(state.getAnime());
                showEntries();
                Platform.runLater(() -> {
                    progressBar.setProgress(percent);
                    lblProgress.setText(String.format("Loading %s / %s", state.getDone(), all));
                    showExportButtonIfPossible();
                });
            }

            return;
        }

        // it's an update of the progress
        if (object instanceof ProgressState) {
            final ProgressState state = (ProgressState) object;
            final int done = state.getDone();
            final int all = state.getTodo();
            final double percent = ((done * 100.00) / all) / 100.00;

            Platform.runLater(() -> {
                progressBar.setProgress(percent);
                lblProgress.setText(String.format("Calculating %s / %s", state.getDone(), all));
                showExportButtonIfPossible();
            });
            return;
        }

        if (object instanceof Boolean) {
            showProgressControls(false);
            Platform.runLater(() -> Notifications.create().title("Recommendations finished").text("Finished search for recommendations.").hideAfter(Config.NOTIFICATION_DURATION)
                    .onAction(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainController().new RecommendationsNotificationEventHandler()).showInformation());
        }
    }


    private void showExportButtonIfPossible() {
        if (!getComponentList().isEmpty() && !btnExport.isVisible()) {
            btnExport.setVisible(true);
        }
    }


    @Override
    protected GridPane getGridPane() {
        return gridPane;
    }


    @Override
    public void updateChildren() {
        Platform.runLater(() -> tab.setText(String.format("%s (%s)", RECOMMENDATIONS_TAB_TITLE, getComponentList().size())));
    }


    public void setTab(final Tab tab) {
        this.tab = tab;
    }


    @Override
    protected void sortComponentEntries() {
        // don't do anything here. we need the entries in the order they arrive.
    }


    @Override
    protected List<? extends MinimalEntry> getEntryList() {
        // not needed for this controller
        return null;
    }


    @Override
    boolean isInList(final String infoLink) {
        // not needed for this controller
        return false;
    }


    /**
     * @since 2.8.2
     */
    public void clear() {
        Platform.runLater(() -> {
            tab.setText(RECOMMENDATIONS_TAB_TITLE);
            gridPane.getChildren().clear();
            lblProgress.setText("Preparing");
            progressBar.setProgress(-1);
            btnExport.setVisible(false);
        });
        getComponentList().clear();
        showProgressControls(false);
    }
}
