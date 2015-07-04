package io.github.manami.gui.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.config.Config;
import io.github.manami.core.services.RelatedAnimeFinderService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.gui.wrapper.MainControllerWrapper;
import org.controlsfx.control.Notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Controller for finding related animes. Opening as a new tab.
 *
 * @author manami project
 * @since 2.3.0
 */
public class RelatedAnimeController extends AbstractAnimeListController implements Observer {

    public static final String RELATED_ANIME_TAB_TITLE = "Related Animes";

    /** Application */
    private final Manami app = Main.CONTEXT.getBean(Manami.class);

    /** The corresponding background service. */
    private RelatedAnimeFinderService service;

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


    /**
     * Called from FXML when creating the Object.
     *
     * @since 2.3.0
     */
    public void initialize() {
        btnStart.setOnAction(event -> start());
        btnCancel.setOnAction(event -> cancel());
    }


    /**
     * Starts the service.
     *
     * @since 2.3.0
     */
    private void start() {
        service = new RelatedAnimeFinderService(Main.CONTEXT.getBean(Cache.class), app, app.fetchAnimeList(), this);
        showProgressControls(true);
        clearComponentList();
        serviceRepo.startService(service);
    }


    /**
     * Stops the service if necessary and resets the GUI.
     *
     * @since 2.3.0
     */
    public void cancel() {
        if (service != null) {
            service.cancel();
        }

        clear();
    }


    /**
     * Shows the progress components and hides the start button or the other way
     * round.
     *
     * @since 2.3.0
     * @param value
     *            Shows the progress components if the value is true and hides
     *            the start button.
     */
    private void showProgressControls(final boolean value) {
        Platform.runLater(() -> {
            vBoxProgress.setVisible(value);
            btnCancel.setVisible(value);
            btnStart.setVisible(!value);
        });
    }


    @Override
    public void update(final Observable observable, final Object object) {
        if (object == null) {
            return;
        }

        // it's an update of the progress
        if (object instanceof ProgressState) {
            final ProgressState state = (ProgressState) object;
            final int done = state.getDone();
            final int all = state.getTodo() + done;
            final double percent = ((done * 100.00) / all) / 100.00;

            Platform.runLater(() -> {
                progressBar.setProgress(percent);
                lblProgress.setText(String.format("%s / %s", state.getDone(), all));
            });
        }

        // adds new Anime entries
        if (object instanceof ArrayList) {
            final ArrayList<Anime> list = (ArrayList<Anime>) object;
            if (list.size() > 0) {
                list.forEach(this::addEntryToGui);
                showEntries();
            }
        }

        // Processing is done
        if (object instanceof Boolean) {
            showProgressControls(false);
            Platform.runLater(() -> Notifications.create().title("Search for related animes finished").text("Finished search for related animes.").hideAfter(Config.NOTIFICATION_DURATION)
                    .onAction(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainController().new RelatedAnimeNotificationEventHandler()).showInformation());
        }
    }


    @Override
    public void updateChildren() {
        Platform.runLater(() -> tab.setText(String.format("%s (%s)", RELATED_ANIME_TAB_TITLE, getComponentList().size())));
    }


    @Override
    protected GridPane getGridPane() {
        return gridPane;
    }


    public void setTab(final Tab tab) {
        this.tab = tab;
    }


    @Override
    protected List<? extends MinimalEntry> getEntryList() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    boolean isInList(final String infoLink) {
        // TODO Auto-generated method stub
        return false;
    }


    /**
     * @since 2.8.2
     */
    public void clear() {
        Platform.runLater(() -> {
            tab.setText(RELATED_ANIME_TAB_TITLE);
            gridPane.getChildren().clear();
            lblProgress.setText("Preparing");
            progressBar.setProgress(-1);
        });
        getComponentList().clear();
        showProgressControls(false);
    }
}
