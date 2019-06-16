package io.github.manami.gui.controller;

import com.google.common.collect.Streams;

import org.controlsfx.control.Notifications;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.core.Manami;
import io.github.manami.core.services.RecommendationsRetrievalService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.core.services.events.AdvancedProgressState;
import io.github.manami.core.services.events.ProgressState;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
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

import static io.github.manami.core.config.Config.NOTIFICATION_DURATION;
import static io.github.manami.gui.utility.DialogLibrary.JSON_FILTER;
import static io.github.manami.gui.utility.DialogLibrary.showExportDialog;


public class RecommendationsController implements Observer {

    public static final String RECOMMENDATIONS_TAB_TITLE = "Recommendations";

    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);
    private final ImageCache imageCache = Main.CONTEXT.getBean(ImageCache.class);
    private final Manami app = Main.CONTEXT.getBean(Manami.class);
    private final Cache cache = Main.CONTEXT.getBean(Cache.class);
    private final Set<InfoLink> containedEntries = new HashSet<>();

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

    @FXML
    private Button btnExport;

    private Tab tab;
    private RecommendationsRetrievalService service;


    /**
     * Called from FXML when creating the Object.
     *
     * @since 2.4.0
     */
    public void initialize() {
        new AnimeTableBuilder<>(contentTable)
                .withPicture(imageCache::loadPicture)
                .withTitleSortable(false)
                .withAddToWatchListButton(true)
                .withAddToFilterListButton(true)
                .withRemoveButton(true)
                .withListChangedEvent((a) -> {
                    Platform.runLater(() -> tab.setText(String.format("Filter List (%s)", contentTable.getItems().size())));
                    return null;
                });
    }


    public void export() {
        final Path file = showExportDialog(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainStage(), JSON_FILTER);
        app.exportList(contentTable.getItems(), file);
    }


    private void showProgressControls(final boolean value) {
        Platform.runLater(() -> {
            hBoxProgress.setVisible(value);
            btnStart.setVisible(!value);
        });
    }


    public void start() {
        service = new RecommendationsRetrievalService(app, cache, this);
        showProgressControls(true);
        contentTable.getItems().clear();
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
                Anime anime = state.getAnime();

                if (!containedEntries.contains(anime.getInfoLink())) {
                    containedEntries.add(anime.getInfoLink());
                    contentTable.getItems().add(anime);
                }

                Platform.runLater(() -> {
                    progressBar.setProgress(percent);
                    lblProgress.setText(String.format("Loading %s / %s", done, all));
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
                lblProgress.setText(String.format("Calculating %s / %s", done, all));
            });

            showExportButtonIfPossible();

            return;
        }

        if (object instanceof Boolean) {
            showProgressControls(false);
            Platform.runLater(() -> Notifications.create()
                    .title("Recommendations finished")
                    .text("Finished search for recommendations.")
                    .hideAfter(NOTIFICATION_DURATION)
                    .onAction(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainController().new RecommendationsNotificationEventHandler()).showInformation());
        }
    }


    private void showExportButtonIfPossible() {
        if (!contentTable.getItems().isEmpty() && !btnExport.isVisible()) {
            Platform.runLater(() -> btnExport.setVisible(true));
        }
    }


    public void setTab(final Tab tab) {
        this.tab = tab;
    }


    public void clear() {
        Platform.runLater(() -> {
            tab.setText(RECOMMENDATIONS_TAB_TITLE);
            lblProgress.setText("Preparing");
            progressBar.setProgress(-1);
            btnExport.setVisible(false);
        });

        contentTable.getItems().clear();
        containedEntries.clear();
        showProgressControls(false);
    }


    public void synchronizeWithLists() {
        if (contentTable.getItems().isEmpty()) return;

        Streams.concat(
                app.fetchAnimeList().stream(),
                app.fetchWatchList().stream(),
                app.fetchFilterList().stream()
        )
                .map(MinimalEntry::getInfoLink)
                .filter(InfoLink::isValid)
                .forEach(e -> {
                    if (containedEntries.contains(e)) {
                        containedEntries.remove(e);
                        contentTable.getItems()
                                .stream()
                                .filter(tableEntry -> tableEntry.getInfoLink().equals(e))
                                .findFirst()
                                .ifPresent(contentTable.getItems()::remove);
                    }
                });
    }
}
