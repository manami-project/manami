package io.github.manami.gui.controller;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.manami.gui.components.Icons.createIconDelete;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.javafx.collections.ObservableListWrapper;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.cache.strategies.headlessbrowser.extractor.ExtractorList;
import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeEntryExtractor;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdAddWatchListEntry;
import io.github.manami.core.commands.CmdDeleteWatchListEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.services.AnimeRetrievalService;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.gui.components.AnimeGuiComponentsListEntry;
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
        final List<String> urlList = Arrays.asList(txtUrl.getText().trim().split(" "));
        final List<InfoLink> infoLinkList = newArrayList();
        urlList.forEach(url -> infoLinkList.add(new InfoLink(url)));
        infoLinkList.stream().filter(infoLink -> infoLink.isValid()).forEach(this::addInfoLinkToWatchList);

        txtUrl.setText(EMPTY);
        showEntries();
    }


    private void addInfoLinkToWatchList(final InfoLink infoLink) {
        final Optional<AnimeEntryExtractor> extractor = extractors.getAnimeEntryExtractor(infoLink);
        InfoLink normalizedInfoLink = null;

        if (extractor.isPresent()) {
            normalizedInfoLink = extractor.get().normalizeInfoLink(infoLink);
        }

        if (!app.watchListEntryExists(normalizedInfoLink)) {
            final AnimeRetrievalService retrievalService = new AnimeRetrievalService(cache, normalizedInfoLink);
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
    }


    /**
     * @since 2.8.0
     */
    public void clear() {
        Platform.runLater(() -> gridPane.getChildren().clear());
        clearComponentList();
        showEntries();
    }


    @Override
    protected AnimeGuiComponentsListEntry addWatchListButton(final AnimeGuiComponentsListEntry componentListEntry) {
        return componentListEntry;
    }


    @Override
    protected AnimeGuiComponentsListEntry addRemoveButton(final AnimeGuiComponentsListEntry componentListEntry) {
        final Button removeButton = new Button(EMPTY, createIconDelete());
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
    boolean isInList(final InfoLink infoLink) {
        return infoLink.isValid() && app.watchListEntryExists(infoLink);
    }
}
