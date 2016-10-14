package io.github.manami.dto.entities;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author manami-project
 * @since 2.7.0
 */
public interface MinimalEntry {

    String getTitle();


    void setTitle(String title);


    String getThumbnail();


    void setThumbnail(String url);


    InfoLink getInfoLink();


    void setInfoLink(InfoLink infoLink);


    /**
     * Checks if a MinimalEntry entry is Valid
     *
     * @param anime
     * @return
     */
    static boolean isValidMinimalEntry(final MinimalEntry anime) {
        boolean ret = anime != null;

        if (!ret) {
            return ret;
        }

        ret &= isNotBlank(anime.getTitle());
        ret &= anime.getInfoLink() != null && anime.getInfoLink().isPresent();

        return ret;
    }
}
