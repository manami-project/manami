package io.github.manami.cache.strategies.headlessbrowser.extractor;

import io.github.manami.dto.entities.InfoLink;

/**
 * @author manami-project
 * @since 2.3.0
 */
public interface AnimeExtractor {

    /**
     * Checks whether the current plugin can process the given link.
     *
     * @param url
     *            URL
     * @return True if the extractor is responsible.
     */
    boolean isResponsible(String url);


    /**
     * Normalizes a link if necessary.
     *
     * @since 2.1.2
     * @param infoLink
     *            URL
     * @return Normalized URL.
     */
    InfoLink normalizeInfoLink(InfoLink infoLink);
}
