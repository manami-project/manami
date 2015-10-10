package io.github.manami.cache;

import io.github.manami.dto.entities.Anime;

/**
 * The cache is supposed to save raw html files from which the information can
 * be extracted at any time.
 *
 * @author manami-project
 * @since 2.0.0
 */
public interface Cache {

    /**
     * Retrieves an anime.
     *
     * @since 2.0.0
     * @param url
     *            URL of the info link site.
     * @return Instance of an {@link Anime} corresponding to the link.
     */
    Anime fetchAnime(final String url);
}
