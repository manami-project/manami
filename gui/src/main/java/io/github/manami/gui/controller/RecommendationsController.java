package io.github.manami.gui.controller;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.config.Config;
import io.github.manami.core.services.RecommendationsRetrievalService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.events.AdvancedProgressState;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.gui.components.AnimeGuiComponentsListEntry;
import io.github.manami.gui.wrapper.MainControllerWrapper;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

import org.controlsfx.control.Notifications;

/**
 * @author manami project
 * @since 2.4.0
 */
public class RecommendationsController extends AbstractAnimeListController implements Observer {

    public static final String RECOMMENDATIONS_TAB_TITLE = "Recommendations";

    /** Instance of the service repository. */
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);

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
        btnCancel.setOnAction(event -> cancel());
    }


    private void showProgressControls(final boolean value) {
        Platform.runLater(() -> {
            vBoxProgress.setVisible(value);
            btnCancel.setVisible(value);
            btnStart.setVisible(!value);
        });
    }


    private void start() {
        service = new RecommendationsRetrievalService(Main.CONTEXT.getBean(Manami.class), Main.CONTEXT.getBean(Cache.class), this);
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
                Platform.runLater(() -> {
                    progressBar.setProgress(percent);
                    lblProgress.setText(String.format("Loading %s / %s", state.getDone(), all));
                });
                addEntryToGui(state.getAnime());
                showEntries();
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
            });
            return;
        }

        if (object instanceof Boolean) {
            showProgressControls(false);
            Platform.runLater(() -> Notifications.create().title("Recommendations finished").text("Finished search for recommendations.").hideAfter(Config.NOTIFICATION_DURATION)
                    .onAction(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainController().new RecommendationsNotificationEventHandler()).showInformation());
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
    public void showEntries() {
        Platform.runLater(() -> {
            updateChildren();
            checkGridPane();

            getGridPane().getChildren().clear();

            for (final AnimeGuiComponentsListEntry entry : getComponentList()) {
                final RowConstraints row = new RowConstraints();
                getGridPane().getRowConstraints().add(row);

                getGridPane().add(entry.getPictureComponent(), 0, getGridPane().getRowConstraints().size() - 1);
                GridPane.setMargin(entry.getPictureComponent(), new Insets(0.0, 0.0, 10.0, 0.0));

                getGridPane().add(entry.getTitleComponent(), 1, getGridPane().getRowConstraints().size() - 1);
                GridPane.setMargin(entry.getTitleComponent(), new Insets(0.0, 0.0, 10.0, 15.0));

                if (entry.getAddToFilterListButton() != null) {
                    getGridPane().add(entry.getAddToFilterListButton(), 2, getGridPane().getRowConstraints().size() - 1);
                    GridPane.setMargin(entry.getAddToFilterListButton(), new Insets(0.0, 0.0, 10.0, 20.0));
                }

                if (entry.getAddToWatchListButton() != null) {
                    getGridPane().add(entry.getAddToWatchListButton(), 3, getGridPane().getRowConstraints().size() - 1);
                    GridPane.setMargin(entry.getAddToWatchListButton(), new Insets(0.0, 0.0, 10.0, 20.0));
                }

                if (entry.getRemoveButton() != null) {
                    getGridPane().add(entry.getRemoveButton(), 4, getGridPane().getRowConstraints().size() - 1);
                    GridPane.setMargin(entry.getRemoveButton(), new Insets(0.0, 0.0, 10.0, 20.0));
                }
            }
        });
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
        });
        getComponentList().clear();
        showProgressControls(false);
    }
}
