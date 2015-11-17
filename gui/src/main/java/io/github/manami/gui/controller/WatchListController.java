package io.github.manami.gui.controller;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.cache.extractor.anime.AnimeSiteExtractor;
import io.github.manami.cache.extractor.anime.ExtractorList;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdAddWatchListEntry;
import io.github.manami.core.commands.CmdDeleteWatchListEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.services.AnimeRetrievalService;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.gui.components.AnimeGuiComponentsListEntry;
import io.github.manami.gui.components.Icons;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.sun.javafx.collections.ObservableListWrapper;

/**
 * @author manami-project
 * @since 2.8.0
 */
public class WatchListController extends AbstractAnimeListController {

    public static final String WATCH_LIST_TITLE = "Watch List";

    /** Instance of the application. */
    private final Manami app = Main.CONTEXT.getBean(Manami.class);

    /** Contains all possible extractors. */
    private final ExtractorList extractors = Main.CONTEXT.getBean(ExtractorList.class);

    /** Instance of the cache. */
    private final Cache cache = Main.CONTEXT.getBean(Cache.class);

    /** Instance of the main application. */
    private final CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);

    /** List of all actively running services. */
    private final ObservableList<AnimeRetrievalService> serviceList = new ObservableListWrapper<>(new CopyOnWriteArrayList<>());

    private ValidationSupport validationSupport;

    /** {@link TextField} for adding a new entry. */
    @FXML
    private TextField txtUrl;

    /** {@link GridPane} which shows the results. */
    @FXML
    private GridPane gridPane;

    /** Moving circle indicating a process. */
    @FXML
    private ProgressIndicator progressIndicator;

    /** Showing the amount of services running in the background. */
    @FXML
    private Label lblProgressMsg;


    /**
     * Fills the GUI with all the entries which are already in the database.
     *
     * @since 2.8.0
     */
    public void initialize() {
        validationSupport = new ValidationSupport();
        validationSupport.registerValidator(txtUrl, Validator.createRegexValidator("Info link must be a valid URL", "(http)s?(://).*", Severity.ERROR));

        serviceList.addListener((ListChangeListener<AnimeRetrievalService>) arg0 -> {
            final int size = serviceList.size();
            final String text = String.format("Preparing entries: %s", size);

            if (size == 0) {
                progressIndicator.setVisible(false);
                lblProgressMsg.setVisible(false);
            } else {
                progressIndicator.setVisible(true);
                lblProgressMsg.setText(text);
                lblProgressMsg.setVisible(true);
            }
        });

        app.fetchWatchList().forEach(this::addEntryToGui);
        showEntries();
    }


    @Override
    protected GridPane getGridPane() {
        return gridPane;
    }


    @FXML
    public void addEntry() {
        String url = txtUrl.getText().trim();

        if (validationSupport.getValidationResult().getErrors().size() > 0) {
            return;
        }

        final AnimeSiteExtractor extractor = extractors.getAnimeExtractor(url);

        if (extractor != null) {
            url = extractor.normalizeInfoLink(url);
        }

        if (!app.watchListEntryExists(url)) {
            final AnimeRetrievalService retrievalService = new AnimeRetrievalService(cache, url);
            retrievalService.setOnSucceeded(event -> {
                final WatchListEntry anime = WatchListEntry.valueOf((Anime) event.getSource().getValue());

                if (anime != null) {
                    cmdService.executeCommand(new CmdAddWatchListEntry(anime, app));
                    addEntryToGui(anime); // create GUI components
                }
                serviceList.remove(retrievalService);

                if (!serviceList.isEmpty()) {
                    serviceList.get(0).start();
                }
            });

            retrievalService.setOnCancelled(event -> serviceList.remove(retrievalService));
            retrievalService.setOnFailed(event -> serviceList.remove(retrievalService));
            serviceList.add(retrievalService);

            if (serviceList.size() == 1) {
                retrievalService.start();
            }
        }

        txtUrl.setText("");
        showEntries();
    }


    /**
     * @since 2.8.0
     */
    public void clear() {
        Platform.runLater(() -> gridPane.getChildren().clear());
        getComponentList().clear();
        showEntries();
    }


    @Override
    protected AnimeGuiComponentsListEntry addWatchListButton(final AnimeGuiComponentsListEntry componentListEntry) {
        return componentListEntry;
    }


    @Override
    protected AnimeGuiComponentsListEntry addRemoveButton(final AnimeGuiComponentsListEntry componentListEntry) {
        final Button removeButton = new Button("", Icons.createIconDelete());
        removeButton.setTooltip(new Tooltip("delete from watch list"));

        componentListEntry.setRemoveButton(removeButton);

        removeButton.setOnAction(event -> {
            cmdService.executeCommand(new CmdDeleteWatchListEntry(WatchListEntry.valueOf(componentListEntry.getAnime()), app));
            getComponentList().remove(componentListEntry);
            showEntries();
        });

        return componentListEntry;
    }


    @Override
    protected List<? extends MinimalEntry> getEntryList() {
        return Main.CONTEXT.getBean(Manami.class).fetchWatchList();
    }


    @Override
    boolean isInList(final String infoLink) {
        return StringUtils.isNotBlank(infoLink) && app.watchListEntryExists(infoLink);
    }
}
