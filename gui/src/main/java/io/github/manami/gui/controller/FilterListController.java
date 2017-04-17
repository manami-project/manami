package io.github.manami.gui.controller;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

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
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.gui.components.AnimeGuiComponentsListEntry;
import io.github.manami.gui.utility.ObservableQueue;
import io.github.manami.gui.wrapper.MainControllerWrapper;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

/**
 * Controller for adding and removing entries to the filter list.
 *
 * @author manami-project
 * @since 2.1.0
 */
public class FilterListController extends AbstractAnimeListController implements Observer {

    public final static String FILTER_LIST_TITLE = "Filter List";

    /** Instance of the application. */
    private final Manami app = Main.CONTEXT.getBean(Manami.class);

    /** Instance of the cache. */
    private final Cache cache = Main.CONTEXT.getBean(Cache.class);

    /** Contains all possible extractors. */
    private final ExtractorList extractors = Main.CONTEXT.getBean(ExtractorList.class);

    /** Instance of the service repository. */
    private final ServiceRepository serviceRepo = Main.CONTEXT.getBean(ServiceRepository.class);

    /** Instance of the main application. */
    private final CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);

    /** List of all actively running services. */
    private final ObservableQueue<AnimeRetrievalService> serviceList = new ObservableQueue<>();

    /** Root container. */
    @FXML
    private AnchorPane anchor;

    /** {@link GridPane} which shows the results. */
    @FXML
    private GridPane recomGridPane;

    /** {@link TextField} for adding a new entry. */
    @FXML
    private TextField txtUrl;

    /** Moving circle indicating a process. */
    @FXML
    private ProgressIndicator progressIndicator;

    /** Showing the amount of services running in the background. */
    @FXML
    private Label lblProgressMsg;

    private List<Anime> recommendedEntries;

    private Tab tab;


    /**
     * Fills the GUI with all the entries which are already in the database.
     *
     * @since 2.1.0
     */
    public void initialize() {
        recommendedEntries = newArrayList();

        serviceList.addListener((ListChangeListener<AnimeRetrievalService>) arg0 -> {
            final int size = serviceList.size();
            final String text = String.format("Preparing entries: %s", size);

            Platform.runLater(() -> {
                if (size == 0) {
                    progressIndicator.setVisible(false);
                    lblProgressMsg.setVisible(false);
                } else {
                    progressIndicator.setVisible(true);
                    lblProgressMsg.setText(text);
                    lblProgressMsg.setVisible(true);
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
        infoLinkList.stream().filter(infoLink -> infoLink.isValid()).forEach(this::addInfoLinkToFilterList);

        txtUrl.setText(EMPTY);
        showEntries();
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
    protected GridPane getGridPane() {
        return recomGridPane;
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
            final List<Anime> list = (ArrayList<Anime>) object;

            recommendedEntries.addAll(list);

            if (list.size() > 0) {
                final long numOfEntriesAdded = list.stream().filter(e -> !containsEntry(e.getInfoLink())).count();

                showEntries();

                if (numOfEntriesAdded > 0L) {
                    final String strEntry = (numOfEntriesAdded > 1) ? "entries" : "entry";
                    final String text = (numOfEntriesAdded > 1) ? "Found " + numOfEntriesAdded + " new anime which you might want to filter." : "A new anime was found which you might want to filter";
                    Platform.runLater(() -> Notifications.create().title("New recommended filter " + strEntry).text(text).hideAfter(Duration.seconds(6.0))
                            .onAction(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainController().new RecommendedFilterListEntryNotificationEventHandler()).showInformation());
                }
            }
        }
    }


    private void processAnimeRetrievalResult(final Anime anime) {
        final FilterEntry filterEntry = FilterEntry.valueOf(anime);

        if (filterEntry != null) {
            cmdService.executeCommand(new CmdAddFilterEntry(filterEntry, app));
        }

        serviceList.poll();

        if (!serviceList.isEmpty()) {
            serviceRepo.startService(serviceList.peek());
        }
    }


    @Override
    public void showEntries() {
        super.showEntries();
        Platform.runLater(() -> tab.setText(String.format("Filter List (%s)", recommendedEntries.size())));
    }


    /**
     * @since 2.7.0
     */
    public void startRecommendedFilterEntrySearch() {
        final List<FilterEntry> filterList = newArrayList(app.fetchFilterList());
        Collections.shuffle(filterList);
        serviceRepo.startService(new RelatedAnimeFinderService(cache, app, filterList, this));
    }


    /**
     * @since 2.7.2
     */
    public void clear() {
        serviceList.clear();
        recommendedEntries.clear();

        Platform.runLater(() -> {
            getGridPane().getChildren().clear();
        });

        clearComponentList();
        showEntries();
    }


    @Override
    protected ImageView getPictureComponent(final MinimalEntry anime) {
        final ImageView thumbnail = new ImageView(new Image(anime.getThumbnail(), true));
        thumbnail.setCache(true);
        return thumbnail;
    }


    @Override
    public AnimeGuiComponentsListEntry addWatchListButton(final AnimeGuiComponentsListEntry componentListEntry) {
        return componentListEntry;
    }


    @Override
    protected AnimeGuiComponentsListEntry addFilterListButton(final AnimeGuiComponentsListEntry componentListEntry) {
        super.addFilterListButton(componentListEntry);
        componentListEntry.getAddToFilterListButton().setOnAction(event -> {
            cmdService.executeCommand(new CmdAddFilterEntry(FilterEntry.valueOf(componentListEntry.getAnime()), app));
            recommendedEntries.remove(componentListEntry.getAnime());
            getComponentList().remove(componentListEntry.getAnime().getInfoLink());
            showEntries();
        });
        return componentListEntry;
    }


    @Override
    protected AnimeGuiComponentsListEntry addRemoveButton(final AnimeGuiComponentsListEntry componentListEntry) {
        super.addRemoveButton(componentListEntry);
        componentListEntry.getRemoveButton().setOnAction(event -> {
            recommendedEntries.remove(componentListEntry.getAnime());
            getComponentList().remove(componentListEntry.getAnime().getInfoLink());
            showEntries();
        });
        return componentListEntry;
    }


    @Override
    protected List<? extends MinimalEntry> getEntryList() {
        return recommendedEntries;
    }


    @Override
    boolean isInList(final InfoLink infoLink) {
        return infoLink.isValid() && app.filterEntryExists(infoLink);
    }


    /**
     * @since 2.7.0
     * @param infoLink
     * @return
     */
    public boolean containsEntry(final InfoLink infoLink) {
        if (!infoLink.isValid()) {
            return false;
        }

        for (final AnimeGuiComponentsListEntry element : getComponentList().values()) {
            if (element.getAnime().getInfoLink().getUrl().equalsIgnoreCase(infoLink.getUrl())) {
                return true;
            }
        }

        return false;
    }


    public void setTab(final Tab tab) {
        this.tab = tab;
    }
}
