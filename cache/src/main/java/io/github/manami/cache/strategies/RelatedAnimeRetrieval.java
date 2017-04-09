package io.github.manami.cache.strategies;

import java.util.Set;

import io.github.manami.dto.entities.InfoLink;

public interface RelatedAnimeRetrieval {

    /**
     * Fetches all related anime for this specific anime (not recursively).
     *
     * @param infoLink
     * @return A {@link Set} of all related anime or an empty {@link Set}, but
     *         never null.
     */
    Set<InfoLink> fetchRelatedAnime(InfoLink infoLink);
}
