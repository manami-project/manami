package io.github.manami.gui.components;

import io.github.manami.dto.entities.MinimalEntry;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;

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
    @Getter
    private final ImageView pictureComponent;

    /** Link containing the title and the info link. */
    @Getter
    private final Hyperlink titleComponent;

    /** Button to for adding an entry to the filter list. */
    @Getter
    @Setter
    private Button addToFilterListButton;

    /** Button to for adding an entry to the filter list. */
    @Getter
    @Setter
    private Button addToWatchListButton;

    /** Button to for removing from current view list. */
    @Getter
    @Setter
    private Button removeButton;

    /** Instance of the {@link MinimalEntry} which is being represented. */
    @Getter
    private final MinimalEntry anime;


    /**
     * @since 2.3.0
     * @param anime
     *            {@link MinimalEntry} which is being shown.
     * @param pictureComponent
     *            Picture of the {@link MinimalEntry} already wrapped in a
     *            JavaFX component.
     * @param titleComponent
     *            Hyperlink containing the title.
     */
    public AnimeGuiComponentsListEntry(final MinimalEntry anime, final ImageView pictureComponent, final Hyperlink titleComponent) {
        this.pictureComponent = pictureComponent;
        this.titleComponent = titleComponent;
        this.anime = anime;
    }
}
