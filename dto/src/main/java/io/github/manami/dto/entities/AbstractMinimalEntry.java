package io.github.manami.dto.entities;

import org.apache.commons.lang3.StringUtils;

/**
 * @author manami-project
 * @since 2.7.3
 */
public abstract class AbstractMinimalEntry implements MinimalEntry {

    /** Placeholder image in case no image is available. */
    public static final String NO_IMG = "http://cdn.myanimelist.net/images/na_series.gif";

    /** Placeholder image in case no image is available, thumbnail size. */
    public static final String NO_IMG_THUMB = "http://cdn.myanimelist.net/images/qm_50.gif";

    private String title;
    private String thumbnail;
    private String infoLink;


    /**
     * @since 2.7.0
     * @return the title
     */
    @Override
    public String getTitle() {
        return title;
    }


    /**
     * @since 2.7.0
     * @param title
     *            the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }


    /**
     * @return the thumbnail
     */
    @Override
    public String getThumbnail() {
        return StringUtils.isNotBlank(thumbnail) ? thumbnail : NO_IMG_THUMB;
    }


    /**
     * @param thumbnail
     *            the thumbnail to set
     */
    public void setThumbnail(final String thumbnail) {
        this.thumbnail = thumbnail;
    }


    /**
     * @since 2.7.0
     * @return the infoLink
     */
    @Override
    public String getInfoLink() {
        return infoLink;
    }


    /**
     * @since 2.7.0
     * @param infoLink
     *            the infoLink to set
     */
    public void setInfoLink(final String infoLink) {
        this.infoLink = infoLink;
    }


    @Override
    public String toString() {
        return new StringBuilder().append("AbstractMinimalEntry [title=").append(title).append(", thumbnail=").append(thumbnail).append(", infoLink=").append(infoLink).append("]").toString();
    }
}
