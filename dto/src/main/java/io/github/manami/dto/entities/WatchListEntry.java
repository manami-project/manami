package io.github.manami.dto.entities;

import java.util.Optional;

/**
 * @author manami-project
 * @since 2.7.0
 */
public class WatchListEntry extends AbstractMinimalEntry {

    /**
     * @since 2.7.3
     * @param title
     * @param infoLink
     */
    public WatchListEntry(final String title, final String thumbnail, final InfoLink infoLink) {
        super.setTitle(title);
        super.setThumbnail(thumbnail);
        super.setInfoLink(infoLink);
    }


    /**
     * @since 2.8.0
     * @param title
     * @param infoLink
     */
    public WatchListEntry(final String title, final InfoLink infoLink) {
        super.setTitle(title);
        super.setInfoLink(infoLink);
    }


    /**
     * @since 2.7.0
     * @param anime
     */
    public static Optional<WatchListEntry> valueOf(final MinimalEntry anime) {
        if (anime == null) {
            return Optional.empty();
        }

        return Optional.of(new WatchListEntry(anime.getTitle(), anime.getThumbnail(), anime.getInfoLink()));
    }
}
