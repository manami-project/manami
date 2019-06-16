package io.github.manami.gui.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.cache.strategies.headlessbrowser.extractor.ExtractorList;
import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeEntryExtractor;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdAddWatchListEntry;
import io.github.manami.core.commands.CmdDeleteWatchListEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.services.AnimeRetrievalService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.gui.utility.AnimeTableBuilder;
import io.github.manami.gui.utility.ImageCache;
import io.github.manami.gui.utility.ObservableQueue;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Queues.newConcurrentLinkedQueue;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class WatchListController implements Observer {

    public static final String WATCH_LIST_TITLE = "Watch List";

    private final Manami app = Main.CONTEXT.getBean(Manami.class);
    private final ExtractorList extractors = Main.CONTEXT.getBean(ExtractorList.class);
    private final Cache cache = Main.CONTEXT.getBean(Cache.class);
    private final CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);
    private final ImageCache imageCache = Main.CONTEXT.getBean(ImageCache.class);
    private final ObservableQueue<AnimeRetrievalService> serviceList = new ObservableQueue<>(newConcurrentLinkedQueue());
    private final Set<InfoLink> containedEntries = new HashSet<>();

    @FXML
    private TableView<WatchListEntry> contentTable;

    @FXML
    private TextField txtUrl;

    @FXML
    private HBox hBoxProgress;

    @FXML
    private Label lblProgressMsg;

    private Tab tab;


    /**
     * Fills the GUI with all the entries which are already in the database.
     *
     * @since 2.8.0
     */
    public void initialize() {
        new AnimeTableBuilder<>(contentTable)
                .withPicture(imageCache::loadThumbnail)
                .withTitleSortable(true)
                .withAddToWatchListButton(false)
                .withAddToFilterListButton(false)
                .withRemoveButton(false)
                .withDeleteButton((a) -> {
                    containedEntries.remove(a);
                    contentTable.getItems().remove(a);
                    cmdService.executeCommand(new CmdDeleteWatchListEntry(a, app));
                    return null;
                })
                .withListChangedEvent((a) -> {
                    Platform.runLater(() -> tab.setText(String.format("%s (%s)", WATCH_LIST_TITLE, contentTable.getItems().size())));
                    return null;
                });

        serviceList.addListener((ListChangeListener<AnimeRetrievalService>) arg0 -> {
            final int size = serviceList.size();
            final String text = String.format("Preparing entries: %s", size);

            Platform.runLater(() -> {
                if (size == 0) {
                    hBoxProgress.setVisible(false);
                } else {
                    hBoxProgress.setVisible(true);
                    lblProgressMsg.setText(text);
                }
            });
        });

        app.fetchWatchList().forEach(this::addEntryToGui);
    }

    private void addEntryToGui(WatchListEntry watchListEntry) {
        if (!containedEntries.contains(watchListEntry.getInfoLink())) {
            contentTable.getItems().add(watchListEntry);
            containedEntries.add(watchListEntry.getInfoLink());
        }
    }

    @FXML
    public void addEntry() {
        final List<String> urlList = Arrays.asList(txtUrl.getText().trim().split(" "));
        final List<InfoLink> infoLinkList = newArrayList();
        urlList.forEach(url -> infoLinkList.add(new InfoLink(url)));
        infoLinkList.stream().filter(InfoLink::isValid).forEach(this::addInfoLinkToWatchList);

        txtUrl.setText(EMPTY);
    }


    private void addInfoLinkToWatchList(final InfoLink infoLink) {
        final Optional<AnimeEntryExtractor> extractor = extractors.getAnimeEntryExtractor(infoLink);
        InfoLink normalizedInfoLink = null;

        if (extractor.isPresent()) {
            normalizedInfoLink = extractor.get().normalizeInfoLink(infoLink);
        }

        if (!app.watchListEntryExists(normalizedInfoLink)) {
            final AnimeRetrievalService retrievalService = new AnimeRetrievalService(cache, normalizedInfoLink);
            retrievalService.addObserver(this);
            serviceList.offer(retrievalService);

            if (serviceList.size() == 1) {
                retrievalService.start();
            }
        }
    }


    public void clear() {
        Platform.runLater(() -> {
            hBoxProgress.setVisible(false);
        });

        serviceList.clear();
        containedEntries.clear();
        contentTable.getItems().clear();
    }


    public void setTab(final Tab tab) {
        this.tab = tab;
    }


    @Override
    public void update(final Observable observable, final Object object) {
        if (observable == null || object == null) {
            return;
        }

        if (observable instanceof AnimeRetrievalService && object instanceof Anime) {
            final Optional<WatchListEntry> anime = WatchListEntry.valueOf((Anime) object);

            if (anime.isPresent()) {
                cmdService.executeCommand(new CmdAddWatchListEntry(anime.get(), app));
                addEntryToGui(anime.get()); // create GUI components
            }

            serviceList.poll();

            if (!serviceList.isEmpty()) {
                serviceRepo.startService(serviceList.peek());
            }
        }
    }

    public void synchronizeTableViewWithWatchList() {
        List<WatchListEntry> currentWatchList = app.fetchWatchList();

        currentWatchList.stream()
                .filter(e -> !containedEntries.contains(e.getInfoLink()))
                .forEach(e -> {
                    containedEntries.add(e.getInfoLink());
                    contentTable.getItems().add(e);
                });

        Map<InfoLink, WatchListEntry> mappedWatchListEntries = currentWatchList.stream()
                .collect(toMap(WatchListEntry::getInfoLink, (a) ->a));

        List<InfoLink> entriesToBeDeletedFromTable = containedEntries.stream()
                .filter(a -> !mappedWatchListEntries.containsKey(a))
                .collect(toList());

        entriesToBeDeletedFromTable.forEach(containedEntry -> {
                    containedEntries.remove(containedEntry);
                    Optional<WatchListEntry> entryInTable = contentTable.getItems()
                            .stream()
                            .filter(tableEntry -> tableEntry.getInfoLink().equals(containedEntry))
                            .findFirst();
                    entryInTable.ifPresent(contentTable.getItems()::remove);
                });
    }
}