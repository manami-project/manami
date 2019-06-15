package io.github.manami.gui.controller;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;

import org.controlsfx.control.Notifications;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.cache.strategies.headlessbrowser.extractor.ExtractorList;
import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeEntryExtractor;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdAddFilterEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.services.AnimeRetrievalService;
import io.github.manami.core.services.RelatedAnimeFinderService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.InfoLink;
import io.github.manami.gui.utility.AnimeTableBuilder;
import io.github.manami.gui.utility.ImageCache;
import io.github.manami.gui.utility.ObservableQueue;
import io.github.manami.gui.wrapper.MainControllerWrapper;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Duration;


public class FilterListController implements Observer {

    public final static String FILTER_LIST_TITLE = "Filter List";

    private final Manami app = Main.CONTEXT.getBean(Manami.class);
    private final Cache cache = Main.CONTEXT.getBean(Cache.class);
    private final ImageCache imageCache = Main.CONTEXT.getBean(ImageCache.class);
    private final ExtractorList extractors = Main.CONTEXT.getBean(ExtractorList.class);
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);
    private final CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);

    private final ObservableQueue<AnimeRetrievalService> serviceList = new ObservableQueue<>();
    private final Set<InfoLink> containedEntries = new HashSet<>();

    @FXML
    private TableView<Anime> contentTable;

    @FXML
    private HBox hBoxProgress;

    @FXML
    private TextField txtUrl;

    @FXML
    private Label lblProgressMsg;

    private Tab tab;


    /**
     * Fills the GUI with all the entries which are already in the database.
     *
     * @since 2.1.0
     */
    public void initialize() {
        new AnimeTableBuilder(contentTable)
                .withPicture(imageCache::loadThumbnail)
                .withTitleSortable(true)
                .withAddToWatchListButton(false)
                .withAddToFilterListButton(true)
                .withRemoveButton(true)
                .withListChangedEvent((a) -> updateTabTitle());

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
    }


    /**
     * Adds a new entry to the filter list.
     *
     * @since 2.1.0
     */
    @FXML
    public void addEntry() {
        final List<String> urlList = Arrays.asList(txtUrl.getText().trim().split(" "));
        final List<InfoLink> infoLinkList = newArrayList();
        urlList.forEach(url -> infoLinkList.add(new InfoLink(url)));
        infoLinkList.stream().filter(InfoLink::isValid).forEach(this::addInfoLinkToFilterList);

        txtUrl.setText(EMPTY);
    }


    private void addInfoLinkToFilterList(final InfoLink infoLink) {
        final Optional<AnimeEntryExtractor> extractor = extractors.getAnimeEntryExtractor(infoLink);
        InfoLink normalizedInfoLink = null;

        if (extractor.isPresent()) {
            normalizedInfoLink = extractor.get().normalizeInfoLink(infoLink);
        }

        if (!app.filterEntryExists(normalizedInfoLink)) {
            final AnimeRetrievalService retrievalService = new AnimeRetrievalService(cache, normalizedInfoLink);
            retrievalService.addObserver(this);
            serviceList.offer(retrievalService);

            if (serviceList.size() == 1) {
                serviceRepo.startService(retrievalService);
            }
        }
    }


    @Override
    public void update(final Observable observable, final Object object) {
        if (observable == null || object == null) {
            return;
        }

        if (observable instanceof AnimeRetrievalService && object instanceof Anime) {
            processAnimeRetrievalResult((Anime) object);
        }

        // adds new Anime entry
        if (object instanceof ArrayList) {
            final List<Anime> list = ((ArrayList<Anime>) object).stream()
                    .filter(e -> e.getInfoLink().isValid())
                    .filter(e -> !containedEntries.contains(e.getInfoLink()))
                    .collect(toList());

            if (list.size() > 0) {
                contentTable.getItems().addAll(list);
                containedEntries.addAll(list.stream().map(Anime::getInfoLink).collect(toSet()));
                final int numOfEntriesAdded = list.size();

                if (numOfEntriesAdded > 0) {
                    final String strEntry = (numOfEntriesAdded > 1) ? "entries" : "entry";
                    final String text = (numOfEntriesAdded > 1) ? "Found " + numOfEntriesAdded + " new anime which you might want to filter." : "A new anime was found which you might want to filter";
                    Platform.runLater(() -> Notifications.create()
                            .title("New recommended filter " + strEntry)
                            .text(text).hideAfter(Duration.seconds(6.0))
                            .onAction(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainController().new RecommendedFilterListEntryNotificationEventHandler()).showInformation());
                }
            }
        }
    }


    private void processAnimeRetrievalResult(final Anime anime) {
        final Optional<FilterEntry> filterEntry = FilterEntry.valueOf(anime);

        filterEntry.ifPresent(entry -> cmdService.executeCommand(new CmdAddFilterEntry(entry, app)));

        serviceList.poll();

        if (!serviceList.isEmpty()) {
            serviceRepo.startService(serviceList.peek());
        }
    }


    private Void updateTabTitle() {
        Platform.runLater(() -> tab.setText(String.format("Filter List (%s)", contentTable.getItems().size())));
        return null;
    }


    public void startRecommendedFilterEntrySearch() {
        final List<FilterEntry> filterList = newArrayList(app.fetchFilterList());
        shuffle(filterList, new SecureRandom());
        shuffle(filterList, new SecureRandom());
        shuffle(filterList, new SecureRandom());
        shuffle(filterList, new SecureRandom());
        serviceRepo.startService(new RelatedAnimeFinderService(cache, app, filterList, this));
    }


    public void clear() {
        serviceList.clear();
        contentTable.getItems().clear();
        containedEntries.clear();
    }

    public void setTab(final Tab tab) {
        this.tab = tab;
    }
}
