package io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime;

import java.util.Set;

import io.github.manami.cache.strategies.headlessbrowser.extractor.anime.AnimeExtractor;

/**
 * Can extract links to related animes.
 *
 * @author manami-project
 * @since 2.3.0
 */
public interface RelatedAnimeExtractor extends AnimeExtractor {

    /**
     * Extracts links to related animes.
     *
     * @since 2.3.0
     * @return A list of URLs for related animes.
     */
    Set<String> extractRelatedAnimes(String url, String siteContent);
}
