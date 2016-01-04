package io.github.manami.gui.controller;

import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.events.SearchResultEvent;
import io.github.manami.gui.components.AnimeGuiComponentsListEntry;
import io.github.manami.gui.utility.DialogLibrary;

import java.awt.Desktop;
import java.net.URI;
import java.text.Collator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author manami-project
 * @since 2.9.0
 */
public class SearchResultsController {

    private static final Logger LOG = LoggerFactory.getLogger(SearchResultsController.class);

    public static final String SEARCH_RESULTS_TAB_TITLE = "Search Results";
    public static final String TITLED_TAB_PANE_TITLE = "Search results from:";

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

    /** List of all GUI components. */
    private List<AnimeGuiComponentsListEntry> componentList;


    /**
     * @since 2.9.0
     * @param event
     */
    public void showResults(final SearchResultEvent event) {
        boolean isPaneAlreadyExpaned = false;

        // anime list entries
        componentList = Lists.newArrayList();
        event.getAnimeListSearchResultList().forEach(element -> componentList.add(new AnimeGuiComponentsListEntry(element, getPictureComponent(element), getTitleComponent(element))));
        showEntries(animeListGridPane);
        animeListTitledPane.setText(String.format("%s anime list (%d)", TITLED_TAB_PANE_TITLE, componentList.size()));
        isPaneAlreadyExpaned = componentList.size() > 0;
        animeListTitledPane.setExpanded(isPaneAlreadyExpaned);

        // filter list entries
        componentList = Lists.newArrayList();
        event.getFilterListSearchResultList().forEach(element -> componentList.add(new AnimeGuiComponentsListEntry(element, getPictureComponent(element), getTitleComponent(element))));
        showEntries(filterListGridPane);
        filterListTitledPane.setText(String.format("%s filter list (%d)", TITLED_TAB_PANE_TITLE, componentList.size()));

        boolean expandFilterListPane = false;
        if (!isPaneAlreadyExpaned) {
            isPaneAlreadyExpaned = componentList.size() > 0;
            expandFilterListPane = isPaneAlreadyExpaned;
        }

        filterListTitledPane.setExpanded(expandFilterListPane);

        // watch list entries
        componentList = Lists.newArrayList();
        event.getWatchListSearchResultList().forEach(element -> componentList.add(new AnimeGuiComponentsListEntry(element, getPictureComponent(element), getTitleComponent(element))));
        showEntries(watchListGridPane);
        watchListTitledPane.setText(String.format("%s watch list (%d)", TITLED_TAB_PANE_TITLE, componentList.size()));

        boolean expandWatchListPane = false;
        if (!isPaneAlreadyExpaned) {
            isPaneAlreadyExpaned = componentList.size() > 0;
            expandWatchListPane = isPaneAlreadyExpaned;
        }

        watchListTitledPane.setExpanded(expandWatchListPane);
    }


    /**
     * Adds all {@link AnimeGuiComponentsListEntry}s to the {@link GridPane}.
     *
     * @since 2.1.3
     */
    private void showEntries(final GridPane gridPane) {
        componentList.sort((a, b) -> Collator.getInstance().compare(a.getTitleComponent().getText(), b.getTitleComponent().getText()));

        gridPane.getChildren().clear();

        for (final AnimeGuiComponentsListEntry entry : componentList) {
            final RowConstraints row = new RowConstraints();
            gridPane.getRowConstraints().add(row);

            gridPane.add(entry.getPictureComponent(), 0, gridPane.getRowConstraints().size() - 1);
            GridPane.setMargin(entry.getPictureComponent(), new Insets(0.0, 0.0, 10.0, 0.0));

            gridPane.add(entry.getTitleComponent(), 1, gridPane.getRowConstraints().size() - 1);
            GridPane.setMargin(entry.getTitleComponent(), new Insets(0.0, 0.0, 10.0, 15.0));
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
                    desktop.browse(new URI(anime.getInfoLink()));
                } catch (final Exception e) {
                    LOG.error("An error occurred trying to open the infolink in the default browser: ", e);
                    DialogLibrary.showExceptionDialog(e);
                }
            }
        });
        return title;
    }
}
