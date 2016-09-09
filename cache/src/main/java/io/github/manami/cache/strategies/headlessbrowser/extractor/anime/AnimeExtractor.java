package io.github.manami.cache.strategies.headlessbrowser.extractor.anime;

import io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime.RelatedAnimeExtractor;

/**
 * @author manami-project
 * @since 2.3.0
 */
public interface AnimeExtractor extends AnimeSiteExtractor, RelatedAnimeExtractor {

    /**
     * Checks whether the current plugin can process the given link.
     *
     * @param url
     *            URL
     * @return True if the extractor is responsible.
     */
    boolean isResponsible(String url);
}
