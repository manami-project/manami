package io.github.manami.dto.entities;

/**
 * @author manami-project
 * @since 2.7.0
 */
public class FilterEntry extends AbstractMinimalEntry {

    /**
     * Constructor awaiting all attributes.
     *
     * @since 2.7.0
     * @param title
     * @param thumbnail
     * @param infoLink
     */
    public FilterEntry(final String title, final String thumbnail, final InfoLink infoLink) {
        super.setTitle(title);
        super.setThumbnail(thumbnail);
        super.setInfoLink(infoLink);
    }


    /**
     * Constructor awaiting title and infoLink.
     *
     * @since 2.7.3
     * @param title
     * @param infoLink
     */
    public FilterEntry(final String title, final InfoLink infoLink) {
        super.setTitle(title);
        super.setInfoLink(infoLink);
    }


    /**
     * @since 2.7.0
     * @param anime
     * @return
     */
    public static FilterEntry valueOf(final MinimalEntry anime) {
        if (anime != null) {
            return new FilterEntry(anime.getTitle(), anime.getThumbnail(), anime.getInfoLink());
        }

        return null;
    }
}
