package io.github.manami.core.services;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import io.github.manami.cache.Cache;
import io.github.manami.dto.entities.Anime;

/**
 * Retrieves an entity of an {@link Anime} by providing the info link URL.
 *
 * @author manami project
 * @since 2.2.0
 */
public class AnimeRetrievalService extends Service<Anime> {

    /** Instance of the {@link Cache}. */
    private final Cache cache;

    /**
     * URL of the info link for which an {@link Anime} entity is being fetched
     * from the cache.
     */
    private final String url;


    /**
     * Constructor.
     *
     * @param url
     *            Info link URL of the anime.
     */
    public AnimeRetrievalService(final Cache cache, final String url) {
        this.cache = cache;
        this.url = url;
    }


    @Override
    protected Task<Anime> createTask() {
        return new Task<Anime>() {

            @Override
            protected Anime call() throws Exception {
                return cache.fetchAnime(url);
            }
        };
    }
}
