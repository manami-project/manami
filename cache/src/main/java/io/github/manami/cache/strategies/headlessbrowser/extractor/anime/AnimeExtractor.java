package io.github.manami.cache.strategies.headlessbrowser.extractor.anime;

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
     * @param url
     *            URL
     * @return Normalized URL.
     */
    String normalizeInfoLink(String url);
}
