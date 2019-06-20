package io.github.manami.core.services;

import java.util.Optional;

import io.github.manami.cache.Cache;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;

/**
 * Retrieves an entity of an {@link Anime} by providing the info link URL.
 */
public class AnimeRetrievalService extends AbstractService<Void> implements BackgroundService {

    /** Instance of the {@link Cache}. */
    private final Cache cache;

    /**
     * URL of the info link for which an {@link Anime} entity is being fetched
     * from the cache.
     */
    private final InfoLink infoLink;


    /**
     * @param infoLink Info link URL of the anime.
     */
    public AnimeRetrievalService(final Cache cache, final InfoLink infoLink) {
        this.cache = cache;
        this.infoLink = infoLink;
    }


    @Override
    protected Void execute() {
        final Optional<Anime> anime = cache.fetchAnime(infoLink);

        if (anime.isPresent() && !isInterrupt()) {
            setChanged();
            notifyObservers(Boolean.TRUE);
            setChanged();
            notifyObservers(anime.get());
        }

        return null;
    }
}
