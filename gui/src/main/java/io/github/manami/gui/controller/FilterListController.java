package io.github.manami.gui.controller;

import io.github.manami.Main;
import io.github.manami.cache.Cache;
import io.github.manami.cache.extractor.anime.AnimeSiteExtractor;
import io.github.manami.cache.extractor.anime.ExtractorList;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdAddFilterEntry;
import io.github.manami.core.commands.CmdDeleteFilterEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.core.services.AnimeRetrievalService;
import io.github.manami.core.services.RelatedAnimeFinderService;
import io.github.manami.core.services.ServiceRepository;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.gui.components.AnimeGuiComponentsListEntry;
import io.github.manami.gui.components.Icons;
import io.github.manami.gui.wrapper.MainControllerWrapper;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Duration;

import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import com.google.common.collect.Lists;
import com.sun.javafx.collections.ObservableListWrapper;

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

    private RecommendationFilterListController recomController;

    /** List of all actively running services. */
    private final ObservableList<AnimeRetrievalService> serviceList = new ObservableListWrapper<>(new CopyOnWriteArrayList<>());

    /** Root container. */
    @FXML
    private AnchorPane anchor;

    /** {@link GridPane} which shows the results. */
    private GridPane filterListGridPane;

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

    @FXML
    private TitledPane filterListPane;

    @FXML
    private TitledPane recomListPane;

    @FXML
    private Pagination pagination;

    private final static int MAX_ENTRIES = 25;

    private ValidationSupport validationSupport;

    private final GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");


    /**
     * Fills the GUI with all the entries which are already in the database.
     *
     * @since 2.1.0
     */
    public void initialize() {
        validationSupport = new ValidationSupport();
        validationSupport.registerValidator(txtUrl, Validator.createRegexValidator("Info link must be a valid URL", "(http)s?(://).*", Severity.ERROR));

        filterListGridPane = new GridPane();
        pagination.setPageFactory(this::createPage);

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

        app.fetchFilterList().forEach(this::addEntryToGui);

        // recommendations
        recomController = new RecommendationFilterListController(recomGridPane, recomListPane);

        showEntries();
    }


    /**
     * Checks repeatedly for external changes and shows new entries if
     * necessary.
     *
     * @since 2.3.0
     */
    @Override
    public void showEntries() {
        Platform.runLater(() -> {
            updateChildren();
            recomController.showEntries();
            pagination.setPageFactory(this::createPage);
            pagination.setPageCount(Lists.partition(getComponentList(), MAX_ENTRIES).size());
            pagination.setCurrentPageIndex(pagination.getCurrentPageIndex());
            filterListPane.setText(String.format("%s (%s)", FILTER_LIST_TITLE, getComponentList().size()));
        });
    }


    /**
     * Adds a new entry to the filter list.
     *
     * @since 2.1.0
     */
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

        if (!app.filterEntryExists(url)) {
            final AnimeRetrievalService retrievalService = new AnimeRetrievalService(cache, url);
            retrievalService.setOnSucceeded(event -> {
                final FilterEntry anime = FilterEntry.valueOf((Anime) event.getSource().getValue());

                if (anime != null) {
                    cmdService.executeCommand(new CmdAddFilterEntry(anime, app));
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


    @Override
    protected GridPane getGridPane() {
        return filterListGridPane;
    }


    /**
     * @since 2.7.2
     * @param pageIndex
     * @return
     */
    public GridPane createPage(final int pageIndex) {
        final GridPane gridPane = new GridPane();
        if (getComponentList().size() > 0) {
            getComponentList().sort((a, b) -> Collator.getInstance().compare(a.getTitleComponent().getText(), b.getTitleComponent().getText()));
            final List<List<AnimeGuiComponentsListEntry>> partitions = Lists.partition(getComponentList(), MAX_ENTRIES);
            pagination.setPageCount(partitions.size());

            for (final AnimeGuiComponentsListEntry entry : partitions.get(pageIndex)) {
                final RowConstraints row = new RowConstraints();
                gridPane.getRowConstraints().add(row);
                gridPane.add(entry.getPictureComponent(), 0, gridPane.getRowConstraints().size() - 1);
                GridPane.setMargin(entry.getPictureComponent(), new Insets(0.0, 0.0, 10.0, 0.0));
                gridPane.add(entry.getTitleComponent(), 1, gridPane.getRowConstraints().size() - 1);
                gridPane.add(entry.getRemoveButton(), 2, gridPane.getRowConstraints().size() - 1);
                GridPane.setMargin(entry.getRemoveButton(), new Insets(0.0, 0.0, 0.0, 20.0));
            }
        }

        return gridPane;
    }


    @Override
    public void update(final Observable observer, final Object object) {
        if (object == null) {
            return;
        }

        // adds new Anime entry
        if (object instanceof ArrayList) {
            final ArrayList<Anime> list = (ArrayList<Anime>) object;

            if (list.size() > 0) {
                int numOfEntriesAdded = 0;
                for (final Anime element : list) {
                    if (!recomController.containsEntry(element.getInfoLink())) {
                        recomController.addEntryToGui(FilterEntry.valueOf(element));
                        recomController.showEntries();
                        numOfEntriesAdded++;
                    }
                }

                if (numOfEntriesAdded > 0) {
                    final String strEntry = (numOfEntriesAdded > 1) ? "entries" : "entry";
                    final String text = (numOfEntriesAdded > 1) ? "Found " + numOfEntriesAdded + " new animes which you might want to filter." : "A new anime was found which you might want to filter";
                    Platform.runLater(() -> Notifications.create().title("New recommended filter " + strEntry).text(text).hideAfter(Duration.seconds(6.0))
                            .onAction(Main.CONTEXT.getBean(MainControllerWrapper.class).getMainController().new RecommendedFilterListEntryNotificationEventHandler()).showInformation());
                }
            }
        }
    }


    /**
     * @since 2.7.0
     */
    public void startRecommendedFilterEntrySearch() {
        final List<FilterEntry> filterList = Lists.newArrayList(app.fetchFilterList());
        Collections.shuffle(filterList);
        serviceRepo.startService(new RelatedAnimeFinderService(cache, app, filterList, this));
    }


    /**
     * @since 2.7.2
     */
    public void clear() {
        Platform.runLater(() -> {
            recomController.getGridPane().getChildren().clear();
            getGridPane().getChildren().clear();
        });
        recomController.getComponentList().clear();
        getComponentList().clear();
        showEntries();
    }


    @Override
    public AnimeGuiComponentsListEntry addWatchListButton(final AnimeGuiComponentsListEntry componentListEntry) {
        return componentListEntry;
    }


    @Override
    protected AnimeGuiComponentsListEntry addFilterListButton(final AnimeGuiComponentsListEntry componentListEntry) {
        return componentListEntry;
    }


    @Override
    protected AnimeGuiComponentsListEntry addRemoveButton(final AnimeGuiComponentsListEntry componentListEntry) {
        final Button btnDelete = new Button("", Icons.createIconDelete());
        btnDelete.setTooltip(new Tooltip("delete from filter list"));

        componentListEntry.setRemoveButton(btnDelete);

        btnDelete.setOnAction(event -> {
            cmdService.executeCommand(new CmdDeleteFilterEntry(FilterEntry.valueOf(componentListEntry.getAnime()), app));
            getComponentList().remove(componentListEntry);
            showEntries();
        });

        return componentListEntry;
    }


    @Override
    protected List<? extends MinimalEntry> getEntryList() {
        return Main.CONTEXT.getBean(Manami.class).fetchFilterList();
    }


    @Override
    boolean isInList(final String infoLink) {
        return StringUtils.isNotBlank(infoLink) && app.filterEntryExists(infoLink);
    }
}
