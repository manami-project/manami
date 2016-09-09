package io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime;

import java.util.Set;

/**
 * Can extract links to related animes.
 *
 * @author manami-project
 * @since 2.3.0
 */
public interface RelatedAnimeExtractor {

    /**
     * Extracts links to related animes.
     *
     * @since 2.3.0
     * @return A list of URLs for related animes.
     */
    Set<String> extractRelatedAnimes();
}
