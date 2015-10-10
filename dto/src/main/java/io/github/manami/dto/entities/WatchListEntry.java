package io.github.manami.dto.entities;

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
    public WatchListEntry(final String title, final String thumbnail, final String infoLink) {
        super.setTitle(title);
        super.setThumbnail(thumbnail);
        super.setInfoLink(infoLink);
    }


    /**
     * @since 2.8.0
     * @param title
     * @param infoLink
     */
    public WatchListEntry(final String title, final String infoLink) {
        super.setTitle(title);
        super.setInfoLink(infoLink);
    }


    /**
     * @since 2.7.0
     * @param anime
     */
    public static WatchListEntry valueOf(final MinimalEntry anime) {
        return new WatchListEntry(anime.getTitle(), anime.getThumbnail(), anime.getInfoLink());
    }
}
