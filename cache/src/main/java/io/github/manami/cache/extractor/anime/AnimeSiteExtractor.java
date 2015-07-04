package io.github.manami.cache.extractor.anime;

import io.github.manami.dto.entities.Anime;

/**
 * Extracts an {@link Anime} from a given prior downloaded website.
 *
 * @author manami project
 * @since 2.0.0
 */
public interface AnimeSiteExtractor {

    /**
     * Returns an instance of the requested {@link Anime} or null in case of an
     * invalid link.
     *
     * @since 2.0.0
     * @param Url
     *            URL
     * @param sitecontent
     *            Content of the info link website.
     * @return Object of type {@link Anime} with every information or null in
     *         case of an invalid link.
     */
    Anime extractAnimeEntry(String Url, String sitecontent);


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
