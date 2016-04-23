package io.github.manami.gui.controller;

import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static io.github.manami.gui.components.Icons.createIconFilterList;
import static io.github.manami.gui.components.Icons.createIconRemove;
import static io.github.manami.gui.components.Icons.createIconWatchList;
import static org.springframework.util.Assert.notNull;

import java.text.Collator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.manami.Main;
import io.github.manami.core.Manami;
import io.github.manami.core.commands.CmdAddFilterEntry;
import io.github.manami.core.commands.CmdAddWatchListEntry;
import io.github.manami.core.commands.CommandService;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.FilterEntry;
import io.github.manami.dto.entities.MinimalEntry;
import io.github.manami.dto.entities.WatchListEntry;
import io.github.manami.gui.components.AnimeGuiComponentsListEntry;
import io.github.manami.gui.utility.HyperlinkBuilder;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;

/**
 * Abstract class if an anime result list. The list entries can be customized by
 * overriding the creation methods.
 *
 * @author manami-project
 * @since 2.3.0
 */
public abstract class AbstractAnimeListController {

    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAnimeListController.class);

    /** Application */
    private final Manami app = Main.CONTEXT.getBean(Manami.class);

    /** Instance of the main application. */
    final private CommandService cmdService = Main.CONTEXT.getBean(CommandService.class);

    /** List of all GUI components. */
    private final List<AnimeGuiComponentsListEntry> componentList;

    /**
     * {@link GridPane} containing all the {@link AnimeGuiComponentsListEntry}s
     */
    private GridPane animeGuiComponentsGridPane;


    /**
     * Constructor.
     */
    public AbstractAnimeListController() {
        componentList = newCopyOnWriteArrayList();
    }


    /**
     * Gets the {@link GridPane} from the implementing class.
     */
    protected void checkGridPane() {
        if (animeGuiComponentsGridPane == null) {
            animeGuiComponentsGridPane = getGridPane();
        }
        notNull(animeGuiComponentsGridPane, "GridPane cannot be null");
    }


    /**
     * Adds all {@link AnimeGuiComponentsListEntry}s to the {@link GridPane}.
     *
     * @since 2.1.3
     */
    public void showEntries() {
        Platform.runLater(() -> {
            updateChildren();
            checkGridPane();
            sortComponentEntries();
            animeGuiComponentsGridPane.getChildren().clear();

            for (final AnimeGuiComponentsListEntry entry : componentList) {
                animeGuiComponentsGridPane.getRowConstraints().add(new RowConstraints());
                final int currentRowNumber = animeGuiComponentsGridPane.getRowConstraints().size() - 1;

                animeGuiComponentsGridPane.add(entry.getPictureComponent(), 0, currentRowNumber);
                animeGuiComponentsGridPane.add(entry.getTitleComponent(), 1, currentRowNumber);

                final HBox hbButtons = new HBox();
                hbButtons.setSpacing(10.0);
                hbButtons.setAlignment(Pos.CENTER);

                if (entry.getAddToFilterListButton() != null) {
                    hbButtons.getChildren().add(entry.getAddToFilterListButton());
                }

                if (entry.getAddToWatchListButton() != null) {
                    hbButtons.getChildren().add(entry.getAddToWatchListButton());
                }

                if (entry.getRemoveButton() != null) {
                    hbButtons.getChildren().add(entry.getRemoveButton());
                }

                animeGuiComponentsGridPane.add(hbButtons, 2, currentRowNumber);
            }
        });
    }


    /**
     * @since 2.10.0
     */
    protected void sortComponentEntries() {
        componentList.sort((a, b) -> Collator.getInstance().compare(a.getTitleComponent().getText(), b.getTitleComponent().getText()));
    }


    /**
     * @since 2.8.0
     * @return List of {@link MinimalEntry}s which is represented as a list of
     *         GUI components.
     */
    protected abstract List<? extends MinimalEntry> getEntryList();


    /**
     * @since 2.8.0
     */
    protected void updateChildren() {
        getEntryList().forEach(this::updateComponentIfNecessary);
    }


    private void updateComponentIfNecessary(final MinimalEntry entry) {
        boolean isAlreadyShown = false;
        MinimalEntry componentEntry = null;
        int componentEntryIndex = -1;

        if (entry == null) {
            return;
        }

        // search for entries which are missing
        for (int index = 0; index < getComponentList().size(); index++) {
            componentEntry = getComponentList().get(index).getAnime();
            if (entry.getInfoLink().equalsIgnoreCase(componentEntry.getInfoLink())) {
                isAlreadyShown = true;
                componentEntryIndex = index;
                break;
            }
        }

        if (!isAlreadyShown) {
            addEntryToGui(entry);
            return;
        }

        // Did the thumbnail change?
        if (componentEntry != null && !componentEntry.getThumbnail().equalsIgnoreCase(entry.getThumbnail())) {
            getComponentList().remove(componentEntryIndex);
            addEntryToGui(entry);
            return;
        }

        // search for entries which are meant to be removed
        for (int index = 0; index < getComponentList().size(); index++) {
            final AnimeGuiComponentsListEntry component = getComponentList().get(index);
            if (!isInList(component.getAnime().getInfoLink())) {
                getComponentList().remove(index);
            }
        }
    }


    /**
     * @since 2.8.0
     * @param infoLink
     *            URL of the info link.
     * @return true if the entry represented by it's info link is already in the
     *         list.
     */
    abstract boolean isInList(String infoLink);


    /**
     * Adds a new entry to the GUI.
     *
     * @since 2.1.0
     * @param anime
     *            {@link Anime} to show.
     */
    protected void addEntryToGui(final MinimalEntry anime) {
        checkGridPane();

        AnimeGuiComponentsListEntry componentListEntry = new AnimeGuiComponentsListEntry(anime, getPictureComponent(anime), getTitleComponent(anime));
        componentListEntry = addFilterListButton(componentListEntry);
        componentListEntry = addWatchListButton(componentListEntry);
        componentListEntry = addRemoveButton(componentListEntry);

        componentList.add(componentListEntry);
    }


    /**
     * Creates a {@link Button} which adds an entry to the filter list. It is
     * necessary to do it this way, because the action for the {@link Button}
     * has to reference reference the component entry itself.
     *
     * @param componentListEntry
     *            {@link AnimeGuiComponentsListEntry} which needs this button.
     * @return The {@link AnimeGuiComponentsListEntry} which was enriched by the
     *         {@link Button}.
     */
    protected AnimeGuiComponentsListEntry addFilterListButton(final AnimeGuiComponentsListEntry componentListEntry) {
        final Button btnAddToFilterList = new Button("", createIconFilterList());
        btnAddToFilterList.setTooltip(new Tooltip("add entry to filter list"));

        componentListEntry.setAddToFilterListButton(btnAddToFilterList);

        btnAddToFilterList.setOnAction(event -> {
            cmdService.executeCommand(new CmdAddFilterEntry(FilterEntry.valueOf(componentListEntry.getAnime()), app));
            getComponentList().remove(componentListEntry);
            showEntries();
        });

        return componentListEntry;
    }


    /**
     * Creates a {@link Button} which adds an entry to the watch list. It is
     * necessary to do it this way, because the action for the {@link Button}
     * has to reference reference the component entry itself.
     *
     * @param componentListEntry
     *            {@link AnimeGuiComponentsListEntry} which needs this button.
     * @return The {@link AnimeGuiComponentsListEntry} which was enriched by the
     *         {@link Button}.
     */
    protected AnimeGuiComponentsListEntry addWatchListButton(final AnimeGuiComponentsListEntry componentListEntry) {
        final Button btnAddToWatchlist = new Button("", createIconWatchList());
        btnAddToWatchlist.setTooltip(new Tooltip("add to watch list"));

        componentListEntry.setAddToWatchListButton(btnAddToWatchlist);

        btnAddToWatchlist.setOnAction(event -> {
            cmdService.executeCommand(new CmdAddWatchListEntry(WatchListEntry.valueOf(componentListEntry.getAnime()), app));
            getComponentList().remove(componentListEntry);
            showEntries();
        });

        return componentListEntry;
    }


    /**
     * Creates a {@link Button} which removes an entry from the current view.
     *
     * @param componentListEntry
     *            {@link AnimeGuiComponentsListEntry} which needs this button.
     * @return The {@link AnimeGuiComponentsListEntry} which was enriched by the
     *         {@link Button}.
     */
    protected AnimeGuiComponentsListEntry addRemoveButton(final AnimeGuiComponentsListEntry componentListEntry) {
        final Button removeButton = new Button("", createIconRemove());
        removeButton.setTooltip(new Tooltip("remove"));

        componentListEntry.setRemoveButton(removeButton);

        removeButton.setOnAction(event -> {
            componentList.remove(componentListEntry);
            showEntries();
        });

        return componentListEntry;
    }


    /**
     * Creates a GUI component for a picture.
     *
     * @param anime
     *            {@link Anime} to show.
     * @return GUI component with the {@link Anime}s picture.
     */
    protected ImageView getPictureComponent(final MinimalEntry anime) {
        String picture;

        if (anime instanceof Anime) {
            picture = ((Anime) anime).getPicture();
        } else {
            picture = anime.getThumbnail();
        }

        final ImageView thumbnail = new ImageView(new Image(picture, true));
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
        final Hyperlink title = HyperlinkBuilder.buildFrom(anime.getTitle(), anime.getInfoLink());
        title.setFont(Font.font(24.0));
        return title;
    }


    /**
     * Clears the component list.
     *
     * @since 2.4.0
     */
    protected void clearComponentList() {
        componentList.clear();
    }


    /**
     * @return the componentList
     */
    protected List<AnimeGuiComponentsListEntry> getComponentList() {
        return componentList;
    }


    /**
     * Getter for the {@link GridPane} of the implementing class.
     *
     * @return The actual instance of the {@link GridPane} being used.
     */
    protected abstract GridPane getGridPane();
}
