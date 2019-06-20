package io.github.manami.dto.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@ToString
public abstract class AbstractMinimalEntry implements MinimalEntry {

    /** Placeholder image in case no image is available. */
    public static final String NO_IMG = "https://myanimelist.cdn-dena.com/images/na_series.gif";

    /** Placeholder image in case no image is available, thumbnail size. */
    public static final String NO_IMG_THUMB = "https://myanimelist.cdn-dena.com/images/qm_50.gif";

    @Getter
    @Setter
    private String title;

    @Setter
    private String thumbnail;

    /**
     * Is never null.
     */
    @Getter
    @Setter
    private InfoLink infoLink = new InfoLink(EMPTY);


    /**
     * @return the thumbnail
     */
    @Override
    public String getThumbnail() {
        return isNotBlank(thumbnail) ? thumbnail : NO_IMG_THUMB;
    }
}
