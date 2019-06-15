package io.github.manami.gui.controller;

import org.controlsfx.control.Notifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.services.RelatedAnimeFinderService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.dto.entities.Anime;
import io.github.manami.gui.utility.AnimeTableBuilder;
import io.github.manami.gui.utility.ImageCache;
import io.github.manami.gui.wrapper.MainControllerWrapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static io.github.manami.core.config.Config.NOTIFICATION_DURATION;


public class RelatedAnimeController implements Observer {

    public static final String RELATED_ANIME_TAB_TITLE = "Related Anime";

    private final Manami app = Main.CONTEXT.getBean(Manami.class);
    private final ImageCache imageCache = Main.CONTEXT.getBean(ImageCache.class);
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);

    private RelatedAnimeFinderService service;

    @FXML
    private TableView<Anime> contentTable;

    @FXML
    private HBox hBoxProgress;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label lblProgress;

    @FXML
    private Button btnStart;

    private Tab tab;


    /**
     * Called from FXML when creating the Object.
     *
     * @since 2.3.0
     */
    public void initialize() {
        new AnimeTableBuilder(contentTable)
                .withPicture(imageCache::loadPicture)
                .withTitleSortable(true)
                .withAddToWatchListButton(true)
                .withAddToFilterListButton(true)
                .withRemoveButton(true)
                .withListChangedEvent((a) -> updateTabTitle());


        btnStart.setOnAction(event -> start());
    }

    public void start() {
        service = new RelatedAnimeFinderService(Main.CONTEXT.getBean(Cache.class), app, app.fetchAnimeList(), this);
        showProgressControls(true);
        contentTable.getItems().clear();
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
            hBoxProgress.setVisible(value);
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
                lblProgress.setText(String.format("%s / %s", done, all));
            });
        }

        // adds new Anime entries
        if (object instanceof ArrayList) {
            final List<Anime> list = (ArrayList<Anime>) object;
            if (list.size() > 0) {
                contentTable.getItems().addAll(list);
            }
        }

        // Processing is done
        if (object instanceof Boolean) {
            showProgressControls(false);
            Platform.runLater(() -> Notifications.create()
                    .title("Search for related anime finished")
                    .text("Finished search for related anime.")
                    .hideAfter(NOTIFICATION_DURATION)
                    .onAction(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainController().new RelatedAnimeNotificationEventHandler()).showInformation());
        }
    }


    private Void updateTabTitle() {
        Platform.runLater(() -> tab.setText(String.format("%s (%s)", RELATED_ANIME_TAB_TITLE, contentTable.getItems().size())));
        return null;
    }


    public void setTab(final Tab tab) {
        this.tab = tab;
    }


    public void clear() {
        Platform.runLater(() -> {
            tab.setText(RELATED_ANIME_TAB_TITLE);
            lblProgress.setText("Preparing");
            progressBar.setProgress(-1);
        });
        contentTable.getItems().clear();
        showProgressControls(false);
    }
}
