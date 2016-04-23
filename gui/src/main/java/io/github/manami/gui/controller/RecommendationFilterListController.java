package io.github.manami.gui.controller;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.gui.components.AnimeGuiComponentsListEntry;
import javafx.application.Platform;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;

/**
 * @author manami-project
 * @since 2.5.0
 */
public class RecommendationFilterListController extends AbstractAnimeListController {

    /** {@link GridPane} which shows the results. */
    private final GridPane recomGridPane;

    private final static String RECOM_ENTRIES_TITLE = "Recommended Filter List Entries";

    private final TitledPane recomListPane;


    public RecommendationFilterListController(final GridPane recomGridPane, final TitledPane recomListPane) {
        this.recomGridPane = recomGridPane;
        this.recomListPane = recomListPane;
    }


    @Override
    protected GridPane getGridPane() {
        return recomGridPane;
    }


    @Override
    public void updateChildren() {
        Platform.runLater(() -> recomListPane.setText(String.format("%s (%s)", RECOM_ENTRIES_TITLE, getComponentList().size())));
    }


    /**
     * @since 2.7.0
     * @param url
     * @return
     */
    public boolean containsEntry(final String url) {
        if (isBlank(url)) {
            return false;
        }

        for (final AnimeGuiComponentsListEntry element : getComponentList()) {
            if (element.getAnime().getInfoLink().equalsIgnoreCase(url)) {
                return true;
            }
        }

        return false;
    }


    @Override
    protected AnimeGuiComponentsListEntry addWatchListButton(final AnimeGuiComponentsListEntry componentListEntry) {
        return componentListEntry;
    }


    @Override
    protected List<? extends MinimalEntry> getEntryList() {
        return null;
    }


    @Override
    boolean isInList(final String infoLink) {
        return true;
    }
}
