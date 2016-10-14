package io.github.manami.cache.strategies;

import io.github.manami.dto.entities.InfoLink;

import java.util.Set;

public interface RelatedAnimeRetrieval {

    /**
     * Fetches all related animes for this specific anime (not recursively).
     * 
     * @param infoLink
     * @return A {@link Set} of all related animes or an empty {@link Set}, but
     *         never null.
     */
    Set<String> fetchRelatedAnimes(InfoLink infoLink);
}
