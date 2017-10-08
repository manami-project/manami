package io.github.manami.gui.controller;

import static com.google.common.collect.Lists.newArrayList;
import static io.github.manami.gui.components.Icons.createIconRemove;
import static io.github.manami.gui.utility.DialogLibrary.showExceptionDialog;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.awt.Desktop;
import java.net.URI;
import java.text.Collator;
import java.util.List;
import java.util.Optional;

import io.github.manami.Main;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdDeleteFilterEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.events.SearchResultEvent;
import io.github.manami.gui.components.AnimeGuiComponentsListEntry;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;

/**
 * @author manami-project
 * @since 2.9.0
 */
@Slf4j
public class SearchResultsController {

    public static final String SEARCH_RESULTS_TAB_TITLE = "Search Results";
    public static final String TITLED_TAB_PANE_TITLE = "Search results from:";

    /** Instance of the application. */
    private final Manami app = Main.CONTEXT.getBean(Manami.class);

    /** Instance of the main application. */
    private final CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);

    @FXML
    private TitledPane animeListTitledPane;

    @FXML
    private TitledPane filterListTitledPane;

    @FXML
    private TitledPane watchListTitledPane;

    @FXML
    private GridPane animeListGridPane;

    @FXML
    private GridPane filterListGridPane;

    @FXML
    private GridPane watchListGridPane;

    @FXML
    private Label lblHeading;

    /** List of all GUI components. */
    private List<AnimeGuiComponentsListEntry> animeListComponents;
    private List<AnimeGuiComponentsListEntry> filterListComponents;
    private List<AnimeGuiComponentsListEntry> watchListComponents;
    private SearchResultEvent event;
    private boolean isPaneAlreadyExpaned;


    /**
     * @since 2.9.0
     * @param event
     */
    public void showResults(final SearchResultEvent event) {
        if (event == null) {
            return;
        }

        this.event = event;
        isPaneAlreadyExpaned = false;

        lblHeading.setText(String.format("%s for \"%s\"", SEARCH_RESULTS_TAB_TITLE, event.getSearchString()));

        createAnimeListEntries();
        createFilterListEntries();
        createWatchListEntries();
    }


    private void createWatchListEntries() {
        watchListComponents = newArrayList();
        event.getWatchListSearchResultList().forEach(element -> watchListComponents.add(new AnimeGuiComponentsListEntry(element, getPictureComponent(element), getTitleComponent(element))));
        showEntries(watchListGridPane, watchListComponents);
        watchListTitledPane.setText(String.format("%s watch list (%d)", TITLED_TAB_PANE_TITLE, watchListComponents.size()));

        boolean isExpandWatchListPane = false;
        if (!isPaneAlreadyExpaned) {
            isPaneAlreadyExpaned = watchListComponents.size() > 0;
            isExpandWatchListPane = isPaneAlreadyExpaned;
        }

        watchListTitledPane.setExpanded(isExpandWatchListPane);
    }


    private void createFilterListEntries() {
        filterListComponents = newArrayList();

        event.getFilterListSearchResultList().forEach(element -> {
            final AnimeGuiComponentsListEntry animeGuiComponentsListEntry = new AnimeGuiComponentsListEntry(element, getPictureComponent(element), getTitleComponent(element));

            final Button removeButton = new Button(EMPTY, createIconRemove());
            removeButton.setTooltip(new Tooltip("remove"));

            removeButton.setOnAction(event -> {
                final Optional<FilterEntry> filterEntry = FilterEntry.valueOf(element);

                if (filterEntry.isPresent()) {

                    cmdService.executeCommand(new CmdDeleteFilterEntry(filterEntry.get(), app));
                    filterListComponents.remove(animeGuiComponentsListEntry);
                    updateFilterListTitle();
                    showEntries(filterListGridPane, filterListComponents);
                }
            });

            animeGuiComponentsListEntry.setRemoveButton(removeButton);
            filterListComponents.add(animeGuiComponentsListEntry);
        });

        updateFilterListTitle();
        showEntries(filterListGridPane, filterListComponents);

        boolean isExpandFilterListPane = false;
        if (!isPaneAlreadyExpaned) {
            isPaneAlreadyExpaned = filterListComponents.size() > 0;
            isExpandFilterListPane = isPaneAlreadyExpaned;
        }

        filterListTitledPane.setExpanded(isExpandFilterListPane);
    }


    private void updateFilterListTitle() {
        filterListTitledPane.setText(String.format("%s filter list (%d)", TITLED_TAB_PANE_TITLE, filterListComponents.size()));
    }


    private void createAnimeListEntries() {
        animeListComponents = newArrayList();
        event.getAnimeListSearchResultList().forEach(element -> animeListComponents.add(new AnimeGuiComponentsListEntry(element, getPictureComponent(element), getTitleComponent(element))));
        showEntries(animeListGridPane, animeListComponents);
        animeListTitledPane.setText(String.format("%s anime list (%d)", TITLED_TAB_PANE_TITLE, animeListComponents.size()));
        isPaneAlreadyExpaned = animeListComponents.size() > 0;
        animeListTitledPane.setExpanded(isPaneAlreadyExpaned);
    }


    /**
     * Adds all {@link AnimeGuiComponentsListEntry}s to the {@link GridPane}.
     *
     * @since 2.1.3
     */
    private void showEntries(final GridPane gridPane, final List<AnimeGuiComponentsListEntry> guiComponentList) {
        guiComponentList.sort((a, b) -> Collator.getInstance().compare(a.getTitleComponent().getText(), b.getTitleComponent().getText()));

        gridPane.getChildren().clear();

        for (final AnimeGuiComponentsListEntry entry : guiComponentList) {
            final RowConstraints row = new RowConstraints();
            gridPane.getRowConstraints().add(row);

            gridPane.add(entry.getPictureComponent(), 0, gridPane.getRowConstraints().size() - 1);
            GridPane.setMargin(entry.getPictureComponent(), new Insets(0.0, 0.0, 10.0, 0.0));

            gridPane.add(entry.getTitleComponent(), 1, gridPane.getRowConstraints().size() - 1);
            GridPane.setMargin(entry.getTitleComponent(), new Insets(0.0, 0.0, 10.0, 15.0));

            if (entry.getRemoveButton() != null) {
                gridPane.add(entry.getRemoveButton(), 2, gridPane.getRowConstraints().size() - 1);
                GridPane.setMargin(entry.getRemoveButton(), new Insets(0.0, 0.0, 10.0, 15.0));
            }
        }
    }


    /**
     * Creates a GUI component for a picture.
     *
     * @param anime
     *            {@link Anime} to show.
     * @return GUI component with the {@link Anime}s picture.
     */
    protected ImageView getPictureComponent(final MinimalEntry anime) {
        final ImageView thumbnail = new ImageView(new Image(anime.getThumbnail(), true));
        thumbnail.setCache(true);
        return thumbnail;
    }


    /**
     * Creates a GUI component for the title.
     *
     * @param anime
     *            {@link Anime} to show.
     * @return A {@link Hyperlink} that has the title as text and the info link
     *         as target.
     */
    protected Hyperlink getTitleComponent(final MinimalEntry anime) {
        final Hyperlink title = new Hyperlink(anime.getTitle());
        title.setFont(Font.font(24.0));
        title.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
                try {
                    final Desktop desktop = java.awt.Desktop.getDesktop();
                    desktop.browse(new URI(anime.getInfoLink().getUrl()));
                } catch (final Exception e) {
                    log.error("An error occurred trying to open the infolink in the default browser: ", e);
                    showExceptionDialog(e);
                }
            }
        });
        return title;
    }
}
