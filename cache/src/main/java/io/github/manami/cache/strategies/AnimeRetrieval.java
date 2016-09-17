package io.github.manami.cache.strategies;

import java.util.Optional;

import io.github.manami.dto.entities.Anime;

public interface AnimeRetrieval {

    /**
     * Retrieves an anime.
     *
     * @since 2.0.0
     * @param url
     *            URL of the info link site.
     * @return Instance of an {@link Anime} corresponding to the link.
     */
    Optional<Anime> fetchAnime(final String url);
}
