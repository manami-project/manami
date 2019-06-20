package io.github.manami.cache.strategies.headlessbrowser.extractor;

import io.github.manami.dto.entities.InfoLink;

public interface AnimeExtractor {

    /**
     * Checks whether the current plugin can process the given link.
     *
     * @param infoLink URL
     * @return True if the extractor is responsible.
     */
    boolean isResponsible(InfoLink infoLink);


    /**
     * Normalizes a link if necessary.
     *
     * @param infoLink URL
     * @return Normalized URL.
     */
    InfoLink normalizeInfoLink(InfoLink infoLink);
}
