package io.github.manami.cache.strategies;

import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;

import java.util.Optional;

public interface AnimeRetrieval {

    /**
     * Retrieves an anime.
     *
     * @param infoLink URL of the info link site.
     * @return Instance of an {@link Anime} corresponding to the link.
     */
    Optional<Anime> fetchAnime(final InfoLink infoLink);
}
