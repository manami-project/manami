package io.github.manami.core.services;

import io.github.manami.cache.Cache;
import io.github.manami.dto.entities.Anime;
import io.github.manami.dto.entities.InfoLink;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Retrieves an entity of an {@link Anime} by providing the info link URL.
 *
 * @author manami-project
 * @since 2.2.0
 */
public class AnimeRetrievalService extends Service<Anime> {

    /** Instance of the {@link Cache}. */
    private final Cache cache;

    /**
     * URL of the info link for which an {@link Anime} entity is being fetched
     * from the cache.
     */
    private final InfoLink infoLink;


    /**
     * Constructor.
     *
     * @param infoLink
     *            Info link URL of the anime.
     */
    public AnimeRetrievalService(final Cache cache, final InfoLink infoLink) {
        this.cache = cache;
        this.infoLink = infoLink;
    }


    @Override
    protected Task<Anime> createTask() {
        return new Task<Anime>() {

            @Override
            protected Anime call() throws Exception {
                return cache.fetchAnime(infoLink).get();
            }
        };
    }
}
