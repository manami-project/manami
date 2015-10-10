package io.github.manami.gui.components;

import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import io.github.manami.dto.entities.MinimalEntry;

/**
 * This represents a list entry. It's stands for a result containing the
 * picture, link with title and the possibility to remove the entry. This was
 * created so the lists don't need to render everything new whenever entries are
 * added or removed.
 *
 * @author manami-project
 * @since 2.1.3
 */
public class AnimeGuiComponentsListEntry {

    /** Image of the anime. */
    private final ImageView pictureComponent;

    /** Link containing the title and the info link. */
    private final Hyperlink titleComponent;

    /** Button to for adding an entry to the filter list. */
    private Button addToFilterListButton;

    /** Button to for adding an entry to the filter list. */
    private Button addToWatchListButton;

    /** Button to for removing from current view list. */
    private Button removeButton;

    /** Instance of the {@link MinimalEntry} which is being represented. */
    private final MinimalEntry anime;


    /**
     * @since 2.3.0
     * @param anime
     *            {@link MinimalEntry} which is being shown.
     * @param pictureComponent
     *            Picture of the {@link MinimalEntry} already wrapped in a JavaFX
     *            component.
     * @param titleComponent
     *            Hyperlink containing the title.
     */
    public AnimeGuiComponentsListEntry(final MinimalEntry anime, final ImageView pictureComponent, final Hyperlink titleComponent) {
        this.pictureComponent = pictureComponent;
        this.titleComponent = titleComponent;
        this.anime = anime;
    }


    /**
     * @since 2.1.3
     * @return the anime
     */
    public MinimalEntry getAnime() {
        return anime;
    }


    /**
     * @since 2.1.3
     * @return the pictureComponent
     */
    public ImageView getPictureComponent() {
        return pictureComponent;
    }


    /**
     * @since 2.1.3
     * @return the titleComponent
     */
    public Hyperlink getTitleComponent() {
        return titleComponent;
    }


    /**
     * @since 2.8.0
     * @return the addToFilterListButton
     */
    public Button getAddToFilterListButton() {
        return addToFilterListButton;
    }


    /**
     * @since 2.8.0
     * @param addToFilterListButton
     *            the addToFilterListButton to set
     */
    public void setAddToFilterListButton(final Button addToFilterListButton) {
        this.addToFilterListButton = addToFilterListButton;
    }


    /**
     * @since 2.8.0
     * @return the addToWatchListButton
     */
    public Button getAddToWatchListButton() {
        return addToWatchListButton;
    }


    /**
     * @since 2.8.0
     * @param addToWatchListButton
     *            the addToWatchListButton to set
     */
    public void setAddToWatchListButton(final Button addToWatchListButton) {
        this.addToWatchListButton = addToWatchListButton;
    }


    /**
     * @since 2.8.0
     * @return the removeButton
     */
    public Button getRemoveButton() {
        return removeButton;
    }


    /**
     * @since 2.8.0
     * @param removeButton
     *            the removeButton to set
     */
    public void setRemoveButton(final Button removeButton) {
        this.removeButton = removeButton;
    }
}
