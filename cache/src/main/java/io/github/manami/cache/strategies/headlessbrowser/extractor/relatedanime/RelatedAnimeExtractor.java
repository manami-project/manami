package io.github.manami.cache.strategies.headlessbrowser.extractor.relatedanime;

import java.util.Set;

import io.github.manami.cache.strategies.headlessbrowser.extractor.AnimeExtractor;
import io.github.manami.dto.entities.InfoLink;

/**
 * Can extract links to related anime.
 *
 * @author manami-project
 * @since 2.3.0
 */
public interface RelatedAnimeExtractor extends AnimeExtractor {

    /**
     * Extracts links to related anime.
     *
     * @since 2.3.0
     * @return A list of URLs for related anime.
     */
    Set<InfoLink> extractRelatedAnime(String siteContent);
}
